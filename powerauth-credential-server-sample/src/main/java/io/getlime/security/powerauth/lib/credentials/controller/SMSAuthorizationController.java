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
package io.getlime.security.powerauth.lib.credentials.controller;

import io.getlime.core.rest.model.base.request.ObjectRequest;
import io.getlime.core.rest.model.base.response.ObjectResponse;
import io.getlime.security.powerauth.lib.credentials.configuration.CredentialStoreConfiguration;
import io.getlime.security.powerauth.lib.credentials.exception.SMSAuthorizationFailedException;
import io.getlime.security.powerauth.lib.credentials.exception.SMSAuthorizationMessageInvalidException;
import io.getlime.security.powerauth.lib.credentials.model.request.CreateSMSAuthorizationRequest;
import io.getlime.security.powerauth.lib.credentials.model.request.VerifySMSAuthorizationRequest;
import io.getlime.security.powerauth.lib.credentials.model.response.CreateSMSAuthorizationResponse;
import io.getlime.security.powerauth.lib.credentials.repository.SMSAuthorizationRepository;
import io.getlime.security.powerauth.lib.credentials.repository.model.entity.SMSAuthorizationEntity;
import io.getlime.security.powerauth.lib.credentials.validation.CreateSMSAuthorizationRequestValidator;
import org.joda.time.DateTime;
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
import java.security.SecureRandom;
import java.util.Date;
import java.util.UUID;

/**
 * Controller class which handles SMS OTP authorization.
 *
 * @author Roman Strobl, roman.strobl@lime-company.eu
 */
@Controller
public class SMSAuthorizationController {

    private SMSAuthorizationRepository smsAuthorizationRepository;
    private CredentialStoreConfiguration credentialStoreConfiguration;

    public SMSAuthorizationController(SMSAuthorizationRepository smsAuthorizationRepository, CredentialStoreConfiguration credentialStoreConfiguration) {
        this.smsAuthorizationRepository = smsAuthorizationRepository;
        this.credentialStoreConfiguration = credentialStoreConfiguration;
    }

    /**
     * Create a new SMS OTP authorization message.
     *
     * @param request Request data.
     * @return Response with message ID.
     */
    @RequestMapping(value = "/sms/create", method = RequestMethod.POST)
    public @ResponseBody
    ObjectResponse<CreateSMSAuthorizationResponse> create(@RequestBody ObjectRequest<CreateSMSAuthorizationRequest> request) throws MethodArgumentNotValidException {
        CreateSMSAuthorizationRequest createSMSAuthorizationRequest = request.getRequestObject();

        // input validation is handled by CreateSMSAuthorizationRequestValidator
        // validation is invoked manually because of the generified Request object
        CreateSMSAuthorizationRequestValidator validator = new CreateSMSAuthorizationRequestValidator();
        BeanPropertyBindingResult result = new BeanPropertyBindingResult(createSMSAuthorizationRequest, "createSMSAuthorizationRequest");
        ValidationUtils.invokeValidator(validator, createSMSAuthorizationRequest, result);
        if (result.hasErrors()) {
            // getEnclosingMethod() on new object returns a reference to current method
            MethodParameter methodParam = new MethodParameter(new Object() {
            }.getClass().getEnclosingMethod(), 0);
            throw new MethodArgumentNotValidException(methodParam, result);
        }

        // messageId is generated as random UUID, it can be overriden to provide a real message identification
        String messageId = UUID.randomUUID().toString();

        // authorization code generation - to be updated based on application requirements
        String authorizationCode = String.valueOf((new SecureRandom()).nextInt(90000000) + 10000000);

        // update names of operationData JSON fields if necessary
        BigDecimal amount = createSMSAuthorizationRequest.getOperationData().get("amount").decimalValue();
        String currency = createSMSAuthorizationRequest.getOperationData().get("currency").textValue();
        String account = createSMSAuthorizationRequest.getOperationData().get("account").textValue();

        // TODO - i18n and usage of text template
        String messageText = "Authorization code for payment of " + amount + " " + currency + " to account " + account + " is " + authorizationCode + ".";

        SMSAuthorizationEntity smsEntity = new SMSAuthorizationEntity();
        smsEntity.setMessageId(messageId);
        smsEntity.setUserId(createSMSAuthorizationRequest.getUserId());
        smsEntity.setOperationName(createSMSAuthorizationRequest.getOperationName());
        smsEntity.setOperationData(createSMSAuthorizationRequest.getOperationData().toString());
        smsEntity.setAuthorizationCode(authorizationCode);
        smsEntity.setMessageText(messageText);
        smsEntity.setVerifyRequestCount(0);
        smsEntity.setTimestampCreated(new Date());
        smsEntity.setTimestampExpires(new DateTime().plusSeconds(credentialStoreConfiguration.getSmsOtpExpirationTime()).toDate());
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
    @RequestMapping(value = "/sms/verify", method = RequestMethod.POST)
    public @ResponseBody
    ObjectResponse verify(@RequestBody ObjectRequest<VerifySMSAuthorizationRequest> request) throws SMSAuthorizationMessageInvalidException, SMSAuthorizationFailedException {
        VerifySMSAuthorizationRequest verifyRequest = request.getRequestObject();
        String messageId = verifyRequest.getMessageId();
        SMSAuthorizationEntity smsEntity = smsAuthorizationRepository.findOne(messageId);
        if (smsEntity == null) {
            throw new SMSAuthorizationMessageInvalidException("sms_authorization.invalid_message");
        }
        // increase number of verification tries and save entity
        smsEntity.setVerifyRequestCount(smsEntity.getVerifyRequestCount() + 1);
        smsAuthorizationRepository.save(smsEntity);

        if (smsEntity.getAuthorizationCode() == null || smsEntity.getAuthorizationCode().isEmpty()) {
            throw new SMSAuthorizationMessageInvalidException("sms_authorization.invalid_code");
        }
        if (smsEntity.isExpired()) {
            throw new SMSAuthorizationFailedException("sms_authorization.expired");
        }
        if (smsEntity.getTimestampVerified().after(smsEntity.getTimestampCreated())) {
            throw new SMSAuthorizationFailedException("sms_authorization.already_verified");
        }
        if (smsEntity.getVerifyRequestCount() > credentialStoreConfiguration.getSmsOtpMaxVerifyTriesPerMessage()) {
            throw new SMSAuthorizationFailedException("sms_authorization.max_tries_exceeded");
        }
        String authorizationCodeExpected = smsEntity.getAuthorizationCode();
        String authorizationCodeActual = verifyRequest.getAuthorizationCode();
        if (!authorizationCodeActual.equals(authorizationCodeExpected)) {
            throw new SMSAuthorizationFailedException("sms_authorization.failed");
        }

        // SMS OTP authorization succeeded when this line is reached, update entity verification status
        smsEntity.setVerified(true);
        smsEntity.setTimestampVerified(new Date());
        smsAuthorizationRepository.save(smsEntity);

        // no actual data sent - ObjectResponse is empty
        return new ObjectResponse();
    }


}
