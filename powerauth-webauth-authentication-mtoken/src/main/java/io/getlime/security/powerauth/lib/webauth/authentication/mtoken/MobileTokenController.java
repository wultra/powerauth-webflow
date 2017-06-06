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

package io.getlime.security.powerauth.lib.webauth.authentication.mtoken;

import io.getlime.security.powerauth.crypto.lib.enums.PowerAuthSignatureTypes;
import io.getlime.security.powerauth.lib.nextstep.model.entity.AuthStep;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthMethod;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthStepResult;
import io.getlime.security.powerauth.lib.nextstep.model.response.GetOperationDetailResponse;
import io.getlime.security.powerauth.lib.webauth.authentication.controller.AuthMethodController;
import io.getlime.security.powerauth.lib.webauth.authentication.exception.AuthStepException;
import io.getlime.security.powerauth.lib.webauth.authentication.mtoken.model.request.MobileTokenAuthenticationRequest;
import io.getlime.security.powerauth.lib.webauth.authentication.mtoken.model.request.MobileTokenSignRequest;
import io.getlime.security.powerauth.lib.webauth.authentication.mtoken.model.response.MobileTokenAuthenticationResponse;
import io.getlime.security.powerauth.lib.webauth.authentication.mtoken.model.response.MobileTokenSignResponse;
import io.getlime.security.powerauth.rest.api.base.authentication.PowerAuthApiAuthentication;
import io.getlime.security.powerauth.rest.api.model.base.PowerAuthApiRequest;
import io.getlime.security.powerauth.rest.api.model.base.PowerAuthApiResponse;
import io.getlime.security.powerauth.rest.api.spring.annotation.PowerAuth;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * @author Petr Dvorak, petr@lime-company.eu
 */
@Controller
@RequestMapping(value = "/api/auth/token")
public class MobileTokenController extends AuthMethodController<MobileTokenAuthenticationRequest, MobileTokenAuthenticationResponse, AuthStepException> {

    @Override
    protected String authenticate(MobileTokenAuthenticationRequest request) throws AuthStepException {
        final GetOperationDetailResponse operation = getOperation();
        final List<AuthStep> steps = operation.getSteps();
        for (AuthStep step: steps) {
            if (AuthMethod.POWERAUTH_TOKEN.equals(step.getAuthMethod())) {
                return null;
            }
        }
        return operation.getUserId();
    }

    @Override
    protected AuthMethod getAuthMethodName() {
        return AuthMethod.POWERAUTH_TOKEN;
    }

    @RequestMapping(value = "/authenticate", method = RequestMethod.POST)
    public @ResponseBody MobileTokenAuthenticationResponse checkOperationStatus(@RequestBody MobileTokenAuthenticationRequest request) {
        try {
            return buildAuthorizationResponse(request, new AuthResponseProvider() {

                @Override
                public MobileTokenAuthenticationResponse doneAuthentication() {
                    final MobileTokenAuthenticationResponse response = new MobileTokenAuthenticationResponse();
                    response.setResult(AuthStepResult.CONFIRMED);
                    response.setMessage("User was successfully authenticated.");
                    return response;
                }

                @Override
                public MobileTokenAuthenticationResponse failedAuthentication() {
                    final MobileTokenAuthenticationResponse response = new MobileTokenAuthenticationResponse();
                    response.setResult(AuthStepResult.FAILED);
                    response.setMessage("Authentication failed.");
                    return response;
                }

                @Override
                public MobileTokenAuthenticationResponse continueAuthentication(String operationId, List<AuthStep> steps) {
                    final MobileTokenAuthenticationResponse response = new MobileTokenAuthenticationResponse();
                    response.setResult(AuthStepResult.CONFIRMED);
                    response.setMessage("User was successfully authenticated.");
                    response.getNext().addAll(steps);
                    return response;
                }
            });
        } catch (AuthStepException e) {
            final MobileTokenAuthenticationResponse response = new MobileTokenAuthenticationResponse();
            response.setResult(AuthStepResult.FAILED);
            response.setMessage(e.getMessage());
            return response;
        }
    }

    @RequestMapping(value = "/operation/list", method = RequestMethod.POST)
    @PowerAuth(resourceId = "/operation/list", signatureType = { PowerAuthSignatureTypes.POSSESSION })
    public @ResponseBody PowerAuthApiResponse<? extends Object> getOperationList(
            @RequestBody PowerAuthApiRequest<String> request,
            PowerAuthApiAuthentication apiAuthentication) {

        if (apiAuthentication != null && apiAuthentication.getUserId() != null) {
            String userId = apiAuthentication.getUserId();
            // TODO: Improve model handling - see #20
            final List<GetOperationDetailResponse> operationList = getOperationListForUser(userId);
            return new PowerAuthApiResponse<List<GetOperationDetailResponse>>(PowerAuthApiResponse.Status.OK, operationList);
        } else {
            return new PowerAuthApiResponse<>(PowerAuthApiResponse.Status.ERROR, null);
        }
    }

    @RequestMapping(value = "/operation/authorize", method = RequestMethod.POST)
    @PowerAuth(resourceId = "/operation/authorize")
    public @ResponseBody PowerAuthApiResponse<MobileTokenSignResponse> verifySignature(
            @RequestBody PowerAuthApiRequest<MobileTokenSignRequest> request,
            PowerAuthApiAuthentication apiAuthentication) {

        if (apiAuthentication != null && apiAuthentication.getUserId() != null) {
            String userId = apiAuthentication.getUserId();
            String operationId = request.getRequestObject().getId();

            //TODO: Mark operation as signed

            MobileTokenSignResponse response = new MobileTokenSignResponse();
            return new PowerAuthApiResponse<>(PowerAuthApiResponse.Status.OK, response);
        } else {
            return new PowerAuthApiResponse<>(PowerAuthApiResponse.Status.ERROR, null);
        }
    }

}
