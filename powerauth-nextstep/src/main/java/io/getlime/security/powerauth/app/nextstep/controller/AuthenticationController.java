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
import io.getlime.security.powerauth.app.nextstep.service.AuthenticationService;
import io.getlime.security.powerauth.lib.nextstep.model.exception.*;
import io.getlime.security.powerauth.lib.nextstep.model.request.CombinedAuthenticationRequest;
import io.getlime.security.powerauth.lib.nextstep.model.request.CredentialAuthenticationRequest;
import io.getlime.security.powerauth.lib.nextstep.model.request.OtpAuthenticationRequest;
import io.getlime.security.powerauth.lib.nextstep.model.response.CombinedAuthenticationResponse;
import io.getlime.security.powerauth.lib.nextstep.model.response.CredentialAuthenticationResponse;
import io.getlime.security.powerauth.lib.nextstep.model.response.OtpAuthenticationResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * REST controller for user authentication.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@RestController
@RequestMapping("auth")
public class AuthenticationController {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationController.class);

    private final AuthenticationService authenticationService;

    /**
     * REST controller constructor.
     * @param authenticationService Authentication service.
     */
    @Autowired
    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    /**
     * Authenticate with a credential.
     * @param request Credential authentication request.
     * @return Credential authentication response.
     * @throws InvalidRequestException Thrown when request is invalid.
     * @throws UserNotFoundException Thrown when user identity is not found.
     * @throws OperationNotFoundException Thrown when operation is not found.
     * @throws CredentialNotFoundException Thrown when credential is not found.
     * @throws CredentialDefinitionNotFoundException Thrown when credential definition is not found.
     * @throws InvalidConfigurationException Thrown when configuration is not found.
     * @throws OperationAlreadyFinishedException Thrown when operation is already finished.
     * @throws OperationAlreadyCanceledException Thrown when operation is already canceled.
     * @throws AuthMethodNotFoundException Thrown when authentication method is not found.
     * @throws OperationAlreadyFailedException Thrown when operation is already failed.
     * @throws OperationNotValidException Thrown when operation is not valid.
     * @throws EncryptionException Thrown when decryption fails.
     */
    @RequestMapping(value = "credential", method = RequestMethod.POST)
    public ObjectResponse<CredentialAuthenticationResponse> authenticateWithCredential(@Valid @RequestBody ObjectRequest<CredentialAuthenticationRequest> request) throws InvalidRequestException, UserNotFoundException, OperationNotFoundException, CredentialNotFoundException, CredentialDefinitionNotFoundException, InvalidConfigurationException, OperationAlreadyFinishedException, OperationAlreadyCanceledException, AuthMethodNotFoundException, OperationAlreadyFailedException, OperationNotValidException, EncryptionException {
        CredentialAuthenticationResponse response = authenticationService.authenticateWithCredential(request.getRequestObject());
        return new ObjectResponse<>(response);
    }

    /**
     * Authenticate with an OTP.
     * @param request OTP authentication request.
     * @return OTP authentication response.
     * @throws AuthMethodNotFoundException Thrown when authentication method is not found.
     * @throws InvalidRequestException Thrown when request is invalid.
     * @throws OperationAlreadyFailedException Thrown when operation is already failed.
     * @throws OperationAlreadyFinishedException Thrown when operation is already finished.
     * @throws InvalidConfigurationException Thrown when Next Step configuration is invalid.
     * @throws OperationAlreadyCanceledException Thrown when operation is already canceled.
     * @throws CredentialNotFoundException Thrown when credential is not found.
     * @throws OperationNotFoundException Throw when operation is not found.
     * @throws OtpNotFoundException Thrown when OTP is not found.
     * @throws OperationNotValidException Thrown when operation is not valid.
     * @throws EncryptionException Thrown when decryption fails.
     */
    @RequestMapping(value = "otp", method = RequestMethod.POST)
    public ObjectResponse<OtpAuthenticationResponse> authenticateWithOtp(@Valid @RequestBody ObjectRequest<OtpAuthenticationRequest> request) throws AuthMethodNotFoundException, InvalidRequestException, OperationAlreadyFailedException, OperationAlreadyFinishedException, InvalidConfigurationException, OperationAlreadyCanceledException, CredentialNotFoundException, OperationNotFoundException, OtpNotFoundException, OperationNotValidException, EncryptionException {
        OtpAuthenticationResponse response = authenticationService.authenticateWithOtp(request.getRequestObject());
        return new ObjectResponse<>(response);
    }

    /**
     * Authenticate with credential and OTP.
     * @param request Combined authentication request.
     * @return Combined authentication response.
     * @throws AuthMethodNotFoundException Thrown when authentication method is not found.
     * @throws InvalidConfigurationException Thrown when configuration is invalid.
     * @throws InvalidRequestException Thrown when request is invalid.
     * @throws UserNotFoundException Thrown when user identity is not found.
     * @throws OperationAlreadyFinishedException Thrown when operation is already finished.
     * @throws OperationAlreadyCanceledException Thrown when operation is already canceled.
     * @throws OperationAlreadyFailedException Thrown when operation is already failed.
     * @throws CredentialNotFoundException Thrown when credential is not found.
     * @throws OperationNotFoundException Thrown when operation is not found.
     * @throws OtpNotFoundException Thrown when OTP is not found.
     * @throws OperationNotValidException Thrown when operation is not valid.
     * @throws EncryptionException Thrown when decryption fails.
     */
    @RequestMapping(value = "combined", method = RequestMethod.POST)
    public ObjectResponse<CombinedAuthenticationResponse> authenticateCombined(@Valid @RequestBody ObjectRequest<CombinedAuthenticationRequest> request) throws AuthMethodNotFoundException, InvalidConfigurationException, InvalidRequestException, UserNotFoundException, OperationAlreadyFinishedException, OperationAlreadyCanceledException, OperationAlreadyFailedException, CredentialNotFoundException, OperationNotFoundException, OtpNotFoundException, OperationNotValidException, EncryptionException {
        CombinedAuthenticationResponse response = authenticationService.authenticateCombined(request.getRequestObject());
        return new ObjectResponse<>(response);
    }

}
