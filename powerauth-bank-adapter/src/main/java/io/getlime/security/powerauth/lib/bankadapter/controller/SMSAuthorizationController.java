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
package io.getlime.security.powerauth.lib.bankadapter.controller;

import io.getlime.core.rest.model.base.request.ObjectRequest;
import io.getlime.core.rest.model.base.response.ObjectResponse;
import io.getlime.security.powerauth.crypto.server.util.DataDigest;
import io.getlime.security.powerauth.lib.bankadapter.configuration.BankAdapterConfiguration;
import io.getlime.security.powerauth.lib.bankadapter.exception.SMSAuthorizationFailedException;
import io.getlime.security.powerauth.lib.bankadapter.exception.SMSAuthorizationMessageInvalidException;
import io.getlime.security.powerauth.lib.bankadapter.model.request.CreateSMSAuthorizationRequest;
import io.getlime.security.powerauth.lib.bankadapter.model.request.VerifySMSAuthorizationRequest;
import io.getlime.security.powerauth.lib.bankadapter.model.response.CreateSMSAuthorizationResponse;
import io.getlime.security.powerauth.lib.bankadapter.repository.SMSAuthorizationRepository;
import io.getlime.security.powerauth.lib.bankadapter.repository.model.entity.SMSAuthorizationEntity;
import io.getlime.security.powerauth.lib.bankadapter.service.OperationFormDataService;
import io.getlime.security.powerauth.lib.bankadapter.validation.CreateSMSAuthorizationRequestValidator;
import io.getlime.security.powerauth.lib.nextstep.model.entity.OperationAmountAttribute;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.ValidationUtils;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.math.BigDecimal;
import java.util.*;

/**
 * Controller class which handles SMS OTP authorization.
 *
 * @author Roman Strobl, roman.strobl@lime-company.eu
 */
@Controller
@RequestMapping("/api/auth/sms")
public class SMSAuthorizationController {

    // the authorization code length - number of digits
    private static final int AUTHORIZATION_CODE_LENGTH = 8;

    private SMSAuthorizationRepository smsAuthorizationRepository;
    private BankAdapterConfiguration bankAdapterConfiguration;
    private OperationFormDataService operationFormDataService;
    private CreateSMSAuthorizationRequestValidator requestValidator;

    @Autowired
    public SMSAuthorizationController(SMSAuthorizationRepository smsAuthorizationRepository, BankAdapterConfiguration bankAdapterConfiguration,
                                      OperationFormDataService operationFormDataService, CreateSMSAuthorizationRequestValidator requestValidator) {
        this.smsAuthorizationRepository = smsAuthorizationRepository;
        this.bankAdapterConfiguration = bankAdapterConfiguration;
        this.operationFormDataService = operationFormDataService;
        this.requestValidator = requestValidator;
    }

    /**
     * Create a new SMS OTP authorization message.
     *
     * @param request Request data.
     * @return Response with message ID.
     */
    @RequestMapping(value = "create", method = RequestMethod.POST)
    public @ResponseBody ObjectResponse<CreateSMSAuthorizationResponse> create(@RequestBody ObjectRequest<CreateSMSAuthorizationRequest> request) throws MethodArgumentNotValidException {
        CreateSMSAuthorizationRequest createSMSAuthorizationRequest = request.getRequestObject();

        // input validation is handled by CreateSMSAuthorizationRequestValidator
        // validation is invoked manually because of the generified Request object
        BeanPropertyBindingResult result = new BeanPropertyBindingResult(createSMSAuthorizationRequest, "createSMSAuthorizationRequest");
        ValidationUtils.invokeValidator(requestValidator, createSMSAuthorizationRequest, result);
        if (result.hasErrors()) {
            // getEnclosingMethod() on new object returns a reference to current method
            MethodParameter methodParam = new MethodParameter(new Object() {
            }.getClass().getEnclosingMethod(), 0);
            throw new MethodArgumentNotValidException(methodParam, result);
        }

        // messageId is generated as random UUID, it can be overriden to provide a real message identification
        String messageId = UUID.randomUUID().toString();

        // update names of operationData JSON fields if necessary
        OperationAmountAttribute amountAttribute = operationFormDataService.getAmount(createSMSAuthorizationRequest.getOperationFormData());
        BigDecimal amount = amountAttribute.getAmount();
        String currency = amountAttribute.getCurrency();
        String account = operationFormDataService.getAccount(createSMSAuthorizationRequest.getOperationFormData());

        // update localized SMS message text in resources
        final DataDigest.Result digestResult = generateAuthorizationCode(amount, currency, account);
        final String authorizationCode = digestResult.getDigest();
        final byte[] salt = digestResult.getSalt();
        String[] messageArgs = {amount.toPlainString(), currency, account, authorizationCode};
        String messageText = messageSource().getMessage("sms-otp.text", messageArgs, new Locale(createSMSAuthorizationRequest.getLang()));

        SMSAuthorizationEntity smsEntity = new SMSAuthorizationEntity();
        smsEntity.setMessageId(messageId);
        smsEntity.setOperationId(createSMSAuthorizationRequest.getOperationId());
        smsEntity.setUserId(createSMSAuthorizationRequest.getUserId());
        smsEntity.setOperationName(createSMSAuthorizationRequest.getOperationName());
        smsEntity.setAuthorizationCode(authorizationCode);
        smsEntity.setSalt(salt);
        smsEntity.setMessageText(messageText);
        smsEntity.setVerifyRequestCount(0);
        smsEntity.setTimestampCreated(new Date());
        smsEntity.setTimestampExpires(new DateTime().plusSeconds(bankAdapterConfiguration.getSmsOtpExpirationTime()).toDate());
        smsEntity.setTimestampVerified(null);
        smsEntity.setVerified(false);

        // store entity in database
        smsAuthorizationRepository.save(smsEntity);

        // Add here code to send the SMS OTP message to user identified by userId with messageText.

        CreateSMSAuthorizationResponse createSMSResponse = new CreateSMSAuthorizationResponse(messageId);
        return new ObjectResponse<>(createSMSResponse);
    }

    /**
     * Verify a SMS OTP authorization code.
     *
     * @param request Request data.
     * @return Authorization response.
     */
    @RequestMapping(value = "verify", method = RequestMethod.POST)
    public @ResponseBody ObjectResponse verify(@RequestBody ObjectRequest<VerifySMSAuthorizationRequest> request) throws SMSAuthorizationMessageInvalidException, SMSAuthorizationFailedException {
        VerifySMSAuthorizationRequest verifyRequest = request.getRequestObject();
        String messageId = verifyRequest.getMessageId();
        SMSAuthorizationEntity smsEntity = smsAuthorizationRepository.findOne(messageId);
        if (smsEntity == null) {
            throw new SMSAuthorizationMessageInvalidException("smsAuthorization.invalidMessage");
        }
        // increase number of verification tries and save entity
        smsEntity.setVerifyRequestCount(smsEntity.getVerifyRequestCount() + 1);
        smsAuthorizationRepository.save(smsEntity);

        if (smsEntity.getAuthorizationCode() == null || smsEntity.getAuthorizationCode().isEmpty()) {
            throw new SMSAuthorizationMessageInvalidException("smsAuthorization.invalidCode");
        }
        if (smsEntity.isExpired()) {
            throw new SMSAuthorizationFailedException("smsAuthorization.expired");
        }
        if (smsEntity.isVerified()) {
            throw new SMSAuthorizationFailedException("smsAuthorization.alreadyVerified");
        }
        if (smsEntity.getVerifyRequestCount() > bankAdapterConfiguration.getSmsOtpMaxVerifyTriesPerMessage()) {
            throw new SMSAuthorizationFailedException("smsAuthorization.maxAttemptsExceeded");
        }
        String authorizationCodeExpected = smsEntity.getAuthorizationCode();
        String authorizationCodeActual = verifyRequest.getAuthorizationCode();
        if (!authorizationCodeActual.equals(authorizationCodeExpected)) {
            throw new SMSAuthorizationFailedException("smsAuthorization.failed");
        }

        // SMS OTP authorization succeeded when this line is reached, update entity verification status
        smsEntity.setVerified(true);
        smsEntity.setTimestampVerified(new Date());
        smsAuthorizationRepository.save(smsEntity);

        // no actual data sent - ObjectResponse is empty
        return new ObjectResponse();
    }

    /**
     * Authorization code generation - to be updated based on application requirements.
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

    /**
     * Get MessageSource with i18n data for authorizations SMS messages.
     *
     * @return MessageSource.
     */
    @Bean
    private MessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasename("classpath:/static/resources/messages");
        messageSource.setDefaultEncoding("UTF-8");
        return messageSource;
    }
}
