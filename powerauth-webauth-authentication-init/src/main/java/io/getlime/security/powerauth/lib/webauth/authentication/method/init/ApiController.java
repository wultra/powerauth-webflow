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
package io.getlime.security.powerauth.lib.webauth.authentication.method.init;

import io.getlime.security.powerauth.lib.webauth.authentication.controller.AuthMethodController;
import io.getlime.security.powerauth.lib.webauth.authentication.exception.AuthStepException;
import io.getlime.security.powerauth.lib.webauth.authentication.method.init.model.request.InitOperationRequest;
import io.getlime.security.powerauth.lib.webauth.authentication.method.init.model.response.InitOperationResponse;
import io.getlime.security.powerauth.lib.nextstep.model.entity.AuthStep;
import io.getlime.security.powerauth.lib.nextstep.model.entity.KeyValueParameter;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthStepResult;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;

/**
 * Controller that handles the initialization of the authentication flow.
 *
 * @author Petr Dvorak, petr@lime-company.eu
 */
@Controller
public class ApiController extends AuthMethodController<InitOperationRequest, InitOperationResponse, AuthStepException> {

    /**
     * Initialize a new authentication flow, by creating an operation. In case operation ID is already
     * included in the request, it initializes context with the operation with this ID and starts authentication
     * step sequence.
     *
     * @param request Authentication initialization request.
     * @return Authentication initialization response.
     */
    @RequestMapping(value = "/api/init", method = RequestMethod.POST)
    public @ResponseBody InitOperationResponse register(@RequestBody InitOperationRequest request) {
        if (request.getOperationId() == null) {
            String operationName = "login";
            String operationData = null;
            List<KeyValueParameter> params = new ArrayList<>();
            return initiateOperationWithName(operationName, operationData, params, new AuthResponseProvider() {

                @Override
                public InitOperationResponse doneAuthentication() {
                    return completeOperationResponse();
                }

                @Override
                public InitOperationResponse failedAuthentication() {
                    return failedOperationResponse(null);
                }

                @Override
                public InitOperationResponse continueAuthentication(String operationId, List<AuthStep> steps) {
                    return continueOperationResponse(operationId, steps);
                }
            });
        } else {
            try {
                return buildAuthorizationResponse(request, new AuthResponseProvider() {

                    @Override
                    public InitOperationResponse doneAuthentication() {
                        return completeOperationResponse();
                    }

                    @Override
                    public InitOperationResponse failedAuthentication() {
                        return failedOperationResponse(null);
                    }

                    @Override
                    public InitOperationResponse continueAuthentication(String operationId, List<AuthStep> steps) {
                        return continueOperationResponse(operationId, steps);
                    }
                });
            } catch (AuthStepException e) {
                return failedOperationResponse(e.getMessage());
            }
        }

    }

    private InitOperationResponse failedOperationResponse(String message) {
        InitOperationResponse registrationResponse = new InitOperationResponse();
        registrationResponse.setResult(AuthStepResult.FAILED);
        registrationResponse.setOperationId(message);
        return registrationResponse;
    }

    private InitOperationResponse completeOperationResponse() {
        InitOperationResponse registrationResponse = new InitOperationResponse();
        registrationResponse.setResult(AuthStepResult.CONFIRMED);
        registrationResponse.setOperationId(null);
        return registrationResponse;
    }

    private InitOperationResponse continueOperationResponse(String operationId, List<AuthStep> steps) {
        InitOperationResponse registrationResponse = new InitOperationResponse();
        registrationResponse.setResult(AuthStepResult.CONFIRMED);
        registrationResponse.setOperationId(operationId);
        registrationResponse.getNext().addAll(steps);
        return registrationResponse;
    }

}
