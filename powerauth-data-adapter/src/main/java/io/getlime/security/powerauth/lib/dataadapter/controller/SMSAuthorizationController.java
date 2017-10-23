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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.ValidationUtils;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

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
    private DataAdapter dataAdapter;

    @Autowired
    public SMSAuthorizationController(SMSPersistenceService smsPersistenceService, CreateSMSAuthorizationRequestValidator requestValidator, DataAdapter dataAdapter) {
        this.smsPersistenceService = smsPersistenceService;
        this.requestValidator = requestValidator;
        this.dataAdapter = dataAdapter;
    }

    /**
     * Create a new SMS OTP authorization message.
     *
     * @param request Request data.
     * @return Response with message ID.
     */
    @RequestMapping(value = "create", method = RequestMethod.POST)
    public @ResponseBody ObjectResponse<CreateSMSAuthorizationResponse> create(@RequestBody ObjectRequest<CreateSMSAuthorizationRequest> request) throws MethodArgumentNotValidException, SMSAuthorizationFailedException {
        CreateSMSAuthorizationRequest smsRequest = request.getRequestObject();
        // input validation is handled by CreateSMSAuthorizationRequestValidator
        // validation is invoked manually because of the generified Request object
        BeanPropertyBindingResult result = new BeanPropertyBindingResult(request, "createSMSAuthorizationRequest");
        ValidationUtils.invokeValidator(requestValidator, request, result);
        if (result.hasErrors()) {
            // getEnclosingMethod() on new object returns a reference to current method
            MethodParameter methodParam = new MethodParameter(new Object() {
            }.getClass().getEnclosingMethod(), 0);
            throw new MethodArgumentNotValidException(methodParam, result);
        }
        SMSAuthorizationEntity smsEntity = smsPersistenceService.createAuthorizationSMS(smsRequest.getUserId(),
                smsRequest.getOperationId(), smsRequest.getOperationName(), smsRequest.getOperationFormData(), smsRequest.getLang());
        dataAdapter.sendAuthorizationSMS(smsEntity.getMessageText(), smsEntity.getUserId());
        CreateSMSAuthorizationResponse response = new CreateSMSAuthorizationResponse(smsEntity.getMessageId());
        return new ObjectResponse<>(response);
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
        smsPersistenceService.verifyAuthorizationSMS(verifyRequest.getMessageId(), verifyRequest.getAuthorizationCode());
        return new ObjectResponse();
    }

}
