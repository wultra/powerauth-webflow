/*
 * Copyright 2021 Wultra s.r.o.
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

package io.getlime.security.powerauth.app.nextstep.controller;

import io.getlime.core.rest.model.base.request.ObjectRequest;
import io.getlime.core.rest.model.base.response.ObjectResponse;
import io.getlime.security.powerauth.app.nextstep.exception.ObjectRequestValidator;
import io.getlime.security.powerauth.app.nextstep.service.OtpService;
import io.getlime.security.powerauth.lib.nextstep.model.exception.*;
import io.getlime.security.powerauth.lib.nextstep.model.request.*;
import io.getlime.security.powerauth.lib.nextstep.model.response.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * REST controller for OTP management.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@RestController
@RequestMapping("otp")
public class OtpController {

    private static final Logger logger = LoggerFactory.getLogger(OtpController.class);

    private final OtpService otpService;
    private final ObjectRequestValidator requestValidator;

    /**
     * REST controller constructor.
     * @param otpService OTP service.
     * @param requestValidator Request validator.
     */
    @Autowired
    public OtpController(OtpService otpService, ObjectRequestValidator requestValidator) {
        this.otpService = otpService;
        this.requestValidator = requestValidator;
    }

    /**
     * Initialize the request validator.
     * @param binder Data binder.
     */
    @InitBinder
    private void initBinder(WebDataBinder binder) {
        binder.setValidator(requestValidator);
    }

    /**
     * Create an OTP.
     * @param request Create OTP request.
     * @return Create OTP response.
     * @throws OtpDefinitionNotFoundException Thrown when OTP definition is not found.
     * @throws CredentialDefinitionNotFoundException Thrown when credential definition is not found.
     * @throws OperationNotFoundException Thrown when operation is not found.
     * @throws InvalidRequestException Thrown when request is invalid.
     * @throws OtpGenAlgorithmNotSupportedException Thrown when OTP generation algorithm is not supported.
     * @throws InvalidConfigurationException Thrown when Next Step configuration is invalid.
     * @throws OperationAlreadyFinishedException Thrown when operation is already finished.
     * @throws OperationAlreadyFailedException Thrown when operation is already failed.
     * @throws UserNotActiveException Thrown when user is not active.
     * @throws CredentialNotActiveException Thrown when credential is not active.
     * @throws CredentialNotFoundException Thrown when credential is not found.
     */
    @RequestMapping(method = RequestMethod.POST)
    public ObjectResponse<CreateOtpResponse> createOtp(@Valid @RequestBody ObjectRequest<CreateOtpRequest> request) throws OtpDefinitionNotFoundException, CredentialDefinitionNotFoundException, OperationNotFoundException, InvalidRequestException, OtpGenAlgorithmNotSupportedException, InvalidConfigurationException, OperationAlreadyFinishedException, OperationAlreadyFailedException, UserNotActiveException, CredentialNotActiveException, CredentialNotFoundException {
        CreateOtpResponse response = otpService.createOtp(request.getRequestObject());
        return new ObjectResponse<>(response);
    }

    /**
     * Create and send an OTP.
     * @param request Create and send OTP request.
     * @return Create and send OTP response.
     * @throws OtpDefinitionNotFoundException Thrown when OTP definition is not found.
     * @throws CredentialDefinitionNotFoundException Thrown when credential definition is not found.
     * @throws OperationNotFoundException Thrown when operation is not found.
     * @throws InvalidRequestException Thrown when request is invalid.
     * @throws OtpGenAlgorithmNotSupportedException Thrown when OTP generation algorithm is not supported.
     * @throws InvalidConfigurationException Thrown when Next Step configuration is invalid.
     * @throws OperationAlreadyFinishedException Thrown when operation is already finished.
     * @throws OperationAlreadyFailedException Thrown when operation is already failed.
     * @throws UserNotActiveException Thrown when user is not active.
     * @throws CredentialNotActiveException Thrown when credential is not active.
     * @throws CredentialNotFoundException Thrown when credential is not found.
     */
    @RequestMapping(value = "send", method = RequestMethod.POST)
    public ObjectResponse<CreateAndSendOtpResponse> createAndSendOtp(@Valid @RequestBody ObjectRequest<CreateAndSendOtpRequest> request) throws UserNotActiveException, CredentialNotActiveException, InvalidRequestException, CredentialDefinitionNotFoundException, OperationAlreadyFailedException, OtpGenAlgorithmNotSupportedException, InvalidConfigurationException, OperationAlreadyFinishedException, CredentialNotFoundException, OtpDefinitionNotFoundException, OperationNotFoundException {
        CreateAndSendOtpResponse response = otpService.createAndSendOtp(request.getRequestObject());
        return new ObjectResponse<>(response);
    }

    /**
     * Get OTP list for an operation.
     * @param request Get OTP list request.
     * @return Get OTP list response.
     * @throws OperationNotFoundException Thrown when operation is not found.
     */
    @RequestMapping(value = "list", method = RequestMethod.POST)
    public ObjectResponse<GetOtpListResponse> getOptList(@Valid @RequestBody ObjectRequest<GetOtpListRequest> request) throws OperationNotFoundException {
        GetOtpListResponse response = otpService.getOtpList(request.getRequestObject());
        return new ObjectResponse<>(response);
    }

    /**
     * Get OTP detail for an OTP or operation.
     * @param request Get OTP detail request.
     * @return Get OTP detail response.
     * @throws OperationNotFoundException Thrown when operation is not found.
     * @throws InvalidRequestException Thrown when request is invalid.
     * @throws OtpNotFoundException Thrown when OTP is not found.
     */
    @RequestMapping(value = "detail", method = RequestMethod.POST)
    public ObjectResponse<GetOtpDetailResponse> getOtpDetail(@Valid @RequestBody ObjectRequest<GetOtpDetailRequest> request) throws OperationNotFoundException, InvalidRequestException, OtpNotFoundException {
        GetOtpDetailResponse response = otpService.getOtpDetail(request.getRequestObject());
        return new ObjectResponse<>(response);
    }

    /**
     * Delete an OTP.
     * @param request Delete OTP request.
     * @return Delete OTP response.
     * @throws OtpNotFoundException Thrown when OTP is not found.
     * @throws InvalidRequestException Thrown when request is invalid.
     * @throws OperationNotFoundException Thrown when operation is not found.
     */
    @RequestMapping(value = "delete", method = RequestMethod.POST)
    public @ResponseBody ObjectResponse<DeleteOtpResponse> deleteOtp(@Valid @RequestBody ObjectRequest<DeleteOtpRequest> request) throws OtpNotFoundException, InvalidRequestException, OperationNotFoundException {
        DeleteOtpResponse response = otpService.deleteOtp(request.getRequestObject());
        return new ObjectResponse<>(response);
    }

}
