/*
 * Copyright 2017 Lime - HighTech Solutions s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.getlime.security.powerauth.lib.dataadapter.service;

import io.getlime.security.powerauth.crypto.server.util.DataDigest;
import io.getlime.security.powerauth.lib.dataadapter.configuration.DataAdapterConfiguration;
import io.getlime.security.powerauth.lib.dataadapter.exception.SMSAuthorizationFailedException;
import io.getlime.security.powerauth.lib.dataadapter.impl.service.OperationFormDataService;
import io.getlime.security.powerauth.lib.dataadapter.repository.SMSAuthorizationRepository;
import io.getlime.security.powerauth.lib.dataadapter.repository.model.entity.SMSAuthorizationEntity;
import io.getlime.security.powerauth.lib.nextstep.model.entity.OperationFormData;
import io.getlime.security.powerauth.lib.nextstep.model.entity.attribute.OperationAmountFieldAttribute;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

/**
 * Service class for generating SMS with OTP and their verification.
 *
 * @author Roman Strobl, roman.strobl@lime-company.eu
 */
@Service
public class SMSPersistenceService {

    private final SMSAuthorizationRepository smsAuthorizationRepository;
    private final OperationFormDataService operationFormDataService;
    private final DataAdapterConfiguration dataAdapterConfiguration;
    private final DataAdapterI18NService dataAdapterI18NService;

    /**
     * SMS persistence service constructor.
     * @param smsAuthorizationRepository SMS authorization repository.
     * @param operationFormDataService Operation form data service.
     * @param dataAdapterConfiguration Data adapter configuration.
     * @param dataAdapterI18NService Data adapter I18N service.
     */
    @Autowired
    public SMSPersistenceService(SMSAuthorizationRepository smsAuthorizationRepository, OperationFormDataService operationFormDataService, DataAdapterConfiguration dataAdapterConfiguration, DataAdapterI18NService dataAdapterI18NService) {
        this.smsAuthorizationRepository = smsAuthorizationRepository;
        this.operationFormDataService = operationFormDataService;
        this.dataAdapterConfiguration = dataAdapterConfiguration;
        this.dataAdapterI18NService = dataAdapterI18NService;
    }

    /**
     * Create an authorization SMS message with OTP.
     * @param userId User ID.
     * @param operationId Operation ID.
     * @param operationName Operation name.
     * @param formData Operation formData.
     * @param lang Language for message text.
     * @return Created entity with SMS message details.
     */
    public SMSAuthorizationEntity createAuthorizationSMS(String userId, String operationId, String operationName, OperationFormData formData, String lang) {
        // messageId is generated as random UUID, it can be overridden to provide a real message identification
        String messageId = UUID.randomUUID().toString();

        // update names of operationData JSON fields if necessary
        OperationAmountFieldAttribute amountAttribute = operationFormDataService.getAmount(formData);

        String[] messageArgs;
        String authorizationCode;
        byte[] salt;

        switch (operationName) {
            case "login": {
                final DataDigest.Result digestResult = generateAuthorizationCode(operationName);
                authorizationCode = digestResult.getDigest();
                salt = digestResult.getSalt();
                messageArgs = new String[]{authorizationCode};
                break;
            }
            case "authorize_payment": {
                BigDecimal amount = amountAttribute.getAmount();
                String currency = amountAttribute.getCurrency();
                String account = operationFormDataService.getAccount(formData);
                final DataDigest.Result digestResult = generateAuthorizationCode(amount, currency, account);
                authorizationCode = digestResult.getDigest();
                salt = digestResult.getSalt();
                messageArgs = new String[]{amount.toPlainString(), currency, account, authorizationCode};
                break;
            }
            default:
                throw new IllegalStateException("Unsupported operation: " + operationName);
        }

        String messageText = dataAdapterI18NService.messageSource().getMessage(operationName + ".smsText", messageArgs, new Locale(lang));

        SMSAuthorizationEntity smsEntity = new SMSAuthorizationEntity();
        smsEntity.setMessageId(messageId);
        smsEntity.setOperationId(operationId);
        smsEntity.setUserId(userId);
        smsEntity.setOperationName(operationName);
        smsEntity.setAuthorizationCode(authorizationCode);
        smsEntity.setSalt(salt);
        smsEntity.setMessageText(messageText);
        smsEntity.setVerifyRequestCount(0);
        smsEntity.setTimestampCreated(new Date());
        smsEntity.setTimestampExpires(new DateTime().plusSeconds(dataAdapterConfiguration.getSmsOtpExpirationTime()).toDate());
        smsEntity.setTimestampVerified(null);
        smsEntity.setVerified(false);

        // store entity in database
        smsAuthorizationRepository.save(smsEntity);

        return smsEntity;
    }

    /**
     * Verify an authorization code
     * @param messageId Message ID.
     * @param authorizationCode Authorization code.
     * @throws SMSAuthorizationFailedException Thrown when SMS authorization fails.
     */
    public void verifyAuthorizationSMS(String messageId, String authorizationCode) throws SMSAuthorizationFailedException {
        SMSAuthorizationEntity smsEntity = smsAuthorizationRepository.findOne(messageId);
        if (smsEntity == null) {
            throw new SMSAuthorizationFailedException("smsAuthorization.invalidMessage");
        }
        // increase number of verification tries and save entity
        smsEntity.setVerifyRequestCount(smsEntity.getVerifyRequestCount() + 1);
        smsAuthorizationRepository.save(smsEntity);

        final Integer remainingAttempts = dataAdapterConfiguration.getSmsOtpMaxVerifyTriesPerMessage() - smsEntity.getVerifyRequestCount();

        if (smsEntity.getAuthorizationCode() == null || smsEntity.getAuthorizationCode().isEmpty()) {
            SMSAuthorizationFailedException ex = new SMSAuthorizationFailedException("smsAuthorization.invalidCode");
            ex.setRemainingAttempts(remainingAttempts);
            throw ex;
        }
        if (smsEntity.isExpired()) {
            throw new SMSAuthorizationFailedException("smsAuthorization.expired");
        }
        if (smsEntity.isVerified()) {
            throw new SMSAuthorizationFailedException("smsAuthorization.alreadyVerified");
        }
        if (smsEntity.getVerifyRequestCount() > dataAdapterConfiguration.getSmsOtpMaxVerifyTriesPerMessage()) {
            throw new SMSAuthorizationFailedException("smsAuthorization.maxAttemptsExceeded");
        }
        String authorizationCodeExpected = smsEntity.getAuthorizationCode();
        if (!authorizationCode.equals(authorizationCodeExpected)) {
            SMSAuthorizationFailedException ex = new SMSAuthorizationFailedException("smsAuthorization.failed");
            ex.setRemainingAttempts(remainingAttempts);
            throw ex;
        }

        // SMS OTP authorization succeeded when this line is reached, update entity verification status
        smsEntity.setVerified(true);
        smsEntity.setTimestampVerified(new Date());
        smsAuthorizationRepository.save(smsEntity);
    }

    /**
     * Authorization code generation - login.
     *
     * @return Generated authorization code.
     */
    private DataDigest.Result generateAuthorizationCode(String operationName) {
        List<String> digestItems = new ArrayList<>();
        digestItems.add(operationName);
        return new DataDigest().generateDigest(digestItems);
    }

    /**
     * Authorization code generation - authorize_payment.
     *
     * @return Generated authorization code.
     */
    private DataDigest.Result generateAuthorizationCode(BigDecimal amount, String currency, String account) {
        List<String> digestItems = new ArrayList<>();
        digestItems.add(amount.toPlainString());
        digestItems.add(currency);
        digestItems.add(account);
        return new DataDigest().generateDigest(digestItems);
    }

}
