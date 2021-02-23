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

    @Autowired
    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @RequestMapping(value = "credential", method = RequestMethod.POST)
    public ObjectResponse<CredentialAuthenticationResponse> authenticationWithCredential(@RequestBody ObjectRequest<CredentialAuthenticationRequest> request) throws InvalidRequestException, UserNotFoundException, OperationNotFoundException, CredentialNotFoundException, CredentialNotActiveException, CredentialDefinitionNotFoundException, InvalidConfigurationException, OperationAlreadyFinishedException, OperationAlreadyCanceledException, AuthMethodNotFoundException, OperationAlreadyFailedException, UserNotActiveException, OperationNotValidException {
        // TODO - request validation
        CredentialAuthenticationResponse response = authenticationService.authenticationWithCredential(request.getRequestObject());
        return new ObjectResponse<>(response);
    }

    @RequestMapping(value = "otp", method = RequestMethod.POST)
    public ObjectResponse<OtpAuthenticationResponse> authenticationWithOtp(@RequestBody ObjectRequest<OtpAuthenticationRequest> request) throws AuthMethodNotFoundException, CredentialNotActiveException, InvalidRequestException, OperationAlreadyFailedException, OperationAlreadyFinishedException, InvalidConfigurationException, OperationAlreadyCanceledException, CredentialNotFoundException, OperationNotFoundException, OtpNotFoundException, UserNotActiveException, OperationNotValidException {
        // TODO - request validation
        OtpAuthenticationResponse response = authenticationService.authenticationWithOtp(request.getRequestObject());
        return new ObjectResponse<>(response);
    }

    @RequestMapping(value = "combined", method = RequestMethod.POST)
    public ObjectResponse<CombinedAuthenticationResponse> authenticationCombined(@RequestBody ObjectRequest<CombinedAuthenticationRequest> request) throws AuthMethodNotFoundException, InvalidConfigurationException, CredentialNotActiveException, InvalidRequestException, UserNotFoundException, OperationAlreadyFinishedException, CredentialDefinitionNotFoundException, OperationAlreadyCanceledException, OperationAlreadyFailedException, CredentialNotFoundException, OperationNotFoundException, OtpNotFoundException, UserNotActiveException, OperationNotValidException {
        // TODO - request validation
        CombinedAuthenticationResponse response = authenticationService.authenticationCombined(request.getRequestObject());
        return new ObjectResponse<>(response);
    }


}
