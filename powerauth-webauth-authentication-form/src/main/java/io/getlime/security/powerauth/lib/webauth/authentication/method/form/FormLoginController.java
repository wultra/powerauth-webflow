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

package io.getlime.security.powerauth.lib.webauth.authentication.method.form;

import io.getlime.core.rest.model.base.response.ObjectResponse;
import io.getlime.security.powerauth.lib.credentials.client.CredentialStoreClient;
import io.getlime.security.powerauth.lib.credentials.client.CredentialStoreClientErrorException;
import io.getlime.security.powerauth.lib.credentials.model.response.AuthenticationResponse;
import io.getlime.security.powerauth.lib.nextstep.client.NextStepServiceException;
import io.getlime.security.powerauth.lib.nextstep.model.entity.AuthStep;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthMethod;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthResult;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthStepResult;
import io.getlime.security.powerauth.lib.nextstep.model.response.UpdateOperationResponse;
import io.getlime.security.powerauth.lib.webauth.authentication.controller.AuthMethodController;
import io.getlime.security.powerauth.lib.webauth.authentication.exception.AuthStepException;
import io.getlime.security.powerauth.lib.webauth.authentication.method.form.model.request.UsernamePasswordAuthenticationRequest;
import io.getlime.security.powerauth.lib.webauth.authentication.method.form.model.response.UsernamePasswordAuthenticationResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * Controller for username / password authentication step.
 *
 * @author Petr Dvorak, petr@lime-company.eu
 */
@Controller
@RequestMapping(value = "/api/auth/form")
public class FormLoginController extends AuthMethodController<UsernamePasswordAuthenticationRequest, UsernamePasswordAuthenticationResponse, AuthStepException> {

    @Autowired
    private CredentialStoreClient credentialStoreClient;

    @Override
    protected String authenticate(UsernamePasswordAuthenticationRequest request) throws AuthStepException {
        try {
            final ObjectResponse<AuthenticationResponse> authenticateResponse = credentialStoreClient.authenticate(request.getUsername(), request.getPassword());
            AuthenticationResponse responseObject = authenticateResponse.getResponseObject();
            return responseObject.getUserId();
        } catch (CredentialStoreClientErrorException e) {
            try {
                // User was not authenticated by credential store - fail authorization to count the number of failures and make it possible
                // to switch to an alternate authentication method in case it is available.
                // Fix #72: Do not include incomplete login attempts when counting number of failed authentication requests
                if ("login.authenticationFailed".equals(e.getError().getMessage())) {
                    UpdateOperationResponse response = failAuthorization(getOperation().getOperationId(), null, null);
                    if (response.getResult() == AuthResult.FAILED) {
                        // FAILED result instead of CONTINUE means the authentication method is failed
                        throw new AuthStepException("authentication.maxAttemptsExceeded", e);
                    }
                }
            } catch (NextStepServiceException e2) {
                throw new AuthStepException(e2.getError().getMessage(), e2);
            }
            throw new AuthStepException(e.getError().getMessage(), e);
        }
    }

    @Override
    protected AuthMethod getAuthMethodName() {
        return AuthMethod.USERNAME_PASSWORD_AUTH;
    }

    /**
     * Handle the user authentication based on username and password.
     * @param request Authentication request using username and password.
     * @return Authentication response.
     */
    @RequestMapping(value = "/authenticate", method = RequestMethod.POST)
    public @ResponseBody UsernamePasswordAuthenticationResponse authenticateHandler(@RequestBody UsernamePasswordAuthenticationRequest request) {
        try {
            return buildAuthorizationResponse(request, new AuthResponseProvider() {

                @Override
                public UsernamePasswordAuthenticationResponse doneAuthentication(String userId) {
                    authenticateCurrentBrowserSession();
                    final UsernamePasswordAuthenticationResponse response = new UsernamePasswordAuthenticationResponse();
                    response.setResult(AuthStepResult.CONFIRMED);
                    response.setMessage("authentication.success");
                    return response;
                }

                @Override
                public UsernamePasswordAuthenticationResponse failedAuthentication(String userId, String failedReason) {
                    clearCurrentBrowserSession();
                    final UsernamePasswordAuthenticationResponse response = new UsernamePasswordAuthenticationResponse();
                    response.setResult(AuthStepResult.AUTH_FAILED);
                    response.setMessage(failedReason);
                    return response;
                }

                @Override
                public UsernamePasswordAuthenticationResponse continueAuthentication(String operationId, String userId, List<AuthStep> steps) {
                    final UsernamePasswordAuthenticationResponse response = new UsernamePasswordAuthenticationResponse();
                    response.setResult(AuthStepResult.CONFIRMED);
                    response.setMessage("authentication.success");
                    response.getNext().addAll(steps);
                    return response;
                }
            });
        } catch (AuthStepException e) {
            final UsernamePasswordAuthenticationResponse response = new UsernamePasswordAuthenticationResponse();
            response.setResult(AuthStepResult.AUTH_FAILED);
            response.setMessage(e.getMessage());
            return response;
        }

    }

}
