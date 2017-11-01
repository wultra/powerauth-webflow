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
package io.getlime.security.powerauth.lib.dataadapter.controller;

import io.getlime.core.rest.model.base.request.ObjectRequest;
import io.getlime.core.rest.model.base.response.ObjectResponse;
import io.getlime.security.powerauth.lib.dataadapter.api.DataAdapter;
import io.getlime.security.powerauth.lib.dataadapter.exception.SMSAuthorizationFailedException;
import io.getlime.security.powerauth.lib.dataadapter.impl.validation.CreateSMSAuthorizationRequestValidator;
import io.getlime.security.powerauth.lib.dataadapter.model.request.CreateSMSAuthorizationRequest;
import io.getlime.security.powerauth.lib.dataadapter.model.request.VerifySMSAuthorizationRequest;
import io.getlime.security.powerauth.lib.dataadapter.model.response.CreateSMSAuthorizationResponse;
import io.getlime.security.powerauth.lib.dataadapter.repository.model.entity.SMSAuthorizationEntity;
import io.getlime.security.powerauth.lib.dataadapter.service.SMSPersistenceService;
import io.getlime.security.powerauth.lib.nextstep.model.entity.OperationFormData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * Controller class which handles SMS OTP authorization.
 *
 * @author Roman Strobl, roman.strobl@lime-company.eu
 */
@Controller
@RequestMapping("/api/auth/sms")
public class SMSAuthorizationController {

    private final SMSPersistenceService smsPersistenceService;
    private final CreateSMSAuthorizationRequestValidator requestValidator;
    private final DataAdapter dataAdapter;

    @Autowired
    public SMSAuthorizationController(SMSPersistenceService smsPersistenceService, CreateSMSAuthorizationRequestValidator requestValidator, DataAdapter dataAdapter) {
        this.smsPersistenceService = smsPersistenceService;
        this.requestValidator = requestValidator;
        this.dataAdapter = dataAdapter;
    }


    /**
     * Initializes the request validator.
     * @param binder Data binder.
     */
    @InitBinder
    private void initBinder(WebDataBinder binder) {
        binder.setValidator(requestValidator);
    }

    /**
     * Create a new SMS OTP authorization message.
     *
     * @param request Request data.
     * @param result BindingResult for input validation.
     * @return Response with message ID.
     */
    @RequestMapping(value = "create", method = RequestMethod.POST)
    public @ResponseBody ObjectResponse<CreateSMSAuthorizationResponse> create(@Valid @RequestBody ObjectRequest<CreateSMSAuthorizationRequest> request, BindingResult result) throws MethodArgumentNotValidException, SMSAuthorizationFailedException {
        if (result.hasErrors()) {
            // getEnclosingMethod() on new object returns a reference to current method
            MethodParameter methodParam = new MethodParameter(new Object(){}.getClass().getEnclosingMethod(),0);
            throw new MethodArgumentNotValidException(methodParam, result);
        }
        CreateSMSAuthorizationRequest smsRequest = request.getRequestObject();

        // Create authorization SMS and persist it.
        SMSAuthorizationEntity smsEntity = createAuthorizationSMS(smsRequest);

        // Send SMS with generated text to target user.
        String userId = smsEntity.getUserId();
        String messageId = smsEntity.getMessageId();
        String messageText = smsEntity.getMessageText();
        dataAdapter.sendAuthorizationSMS(userId, messageText);

        // Create response.
        CreateSMSAuthorizationResponse response = new CreateSMSAuthorizationResponse(messageId);
        return new ObjectResponse<>(response);
    }

    /**
     * Validates the request and sends SMS.
     * @param smsRequest Create SMS request.
     * @return SMS entity.
     */
    private SMSAuthorizationEntity createAuthorizationSMS(@Valid CreateSMSAuthorizationRequest smsRequest) {
        String userId = smsRequest.getUserId();
        String operationId = smsRequest.getOperationId();
        String operationName = smsRequest.getOperationName();
        OperationFormData formData = smsRequest.getOperationFormData();
        String lang = smsRequest.getLang();
        return smsPersistenceService.createAuthorizationSMS(userId, operationId, operationName, formData, lang);
    }

    /**
     * Verify a SMS OTP authorization code.
     *
     * @param request Request data.
     * @return Authorization response.
     */
    @RequestMapping(value = "verify", method = RequestMethod.POST)
    public @ResponseBody ObjectResponse verify(@RequestBody ObjectRequest<VerifySMSAuthorizationRequest> request) throws SMSAuthorizationFailedException {
        VerifySMSAuthorizationRequest verifyRequest = request.getRequestObject();
        String messageId = verifyRequest.getMessageId();
        String authorizationCode = verifyRequest.getAuthorizationCode();
        // Verify authorization code.
        smsPersistenceService.verifyAuthorizationSMS(messageId, authorizationCode);
        return new ObjectResponse();
    }

}
