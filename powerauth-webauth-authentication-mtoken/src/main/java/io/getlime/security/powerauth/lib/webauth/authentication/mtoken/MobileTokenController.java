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

import io.getlime.push.client.MobilePlatform;
import io.getlime.push.client.PushServerClient;
import io.getlime.push.model.StatusResponse;
import io.getlime.push.model.entity.PushMessage;
import io.getlime.push.model.entity.PushMessageBody;
import io.getlime.security.powerauth.crypto.lib.enums.PowerAuthSignatureTypes;
import io.getlime.security.powerauth.lib.nextstep.client.NextStepServiceException;
import io.getlime.security.powerauth.lib.nextstep.model.entity.AuthStep;
import io.getlime.security.powerauth.lib.nextstep.model.entity.OperationHistory;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthMethod;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthResult;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthStepResult;
import io.getlime.security.powerauth.lib.nextstep.model.response.GetOperationDetailResponse;
import io.getlime.security.powerauth.lib.nextstep.model.response.UpdateOperationResponse;
import io.getlime.security.powerauth.lib.webauth.authentication.controller.AuthMethodController;
import io.getlime.security.powerauth.lib.webauth.authentication.exception.AuthStepException;
import io.getlime.security.powerauth.lib.webauth.authentication.mtoken.model.request.MobileTokenAuthenticationRequest;
import io.getlime.security.powerauth.lib.webauth.authentication.mtoken.model.request.MobileTokenPushRegisterRequest;
import io.getlime.security.powerauth.lib.webauth.authentication.mtoken.model.request.MobileTokenSignRequest;
import io.getlime.security.powerauth.lib.webauth.authentication.mtoken.model.response.MobileTokenAuthenticationResponse;
import io.getlime.security.powerauth.lib.webauth.authentication.mtoken.model.response.MobileTokenSignResponse;
import io.getlime.security.powerauth.rest.api.base.authentication.PowerAuthApiAuthentication;
import io.getlime.security.powerauth.rest.api.model.base.PowerAuthApiRequest;
import io.getlime.security.powerauth.rest.api.model.base.PowerAuthApiResponse;
import io.getlime.security.powerauth.rest.api.spring.annotation.PowerAuth;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private PushServerClient pushServerClient;

    @Override
    protected String authenticate(MobileTokenAuthenticationRequest request) throws AuthStepException {
        final GetOperationDetailResponse operation = getOperation();
        final List<OperationHistory> history = operation.getHistory();
        for (OperationHistory h: history) {
            if (AuthMethod.POWERAUTH_TOKEN.equals(h.getAuthMethod())
                    && !AuthResult.FAILED.equals(h.getAuthResult())) {
                return operation.getUserId();
            }
        }
        return null;
    }

    @Override
    protected AuthMethod getAuthMethodName() {
        return AuthMethod.POWERAUTH_TOKEN;
    }

    @RequestMapping(value = "/init", method = RequestMethod.POST)
    public @ResponseBody StatusResponse initPushMessage() {
        final GetOperationDetailResponse operation = getOperation();
        final String userId = operation.getUserId();

        PushMessage message = new PushMessage();
        message.setUserId(userId);

        PushMessageBody body = new PushMessageBody();
        body.setTitle("Potvrďte operaci");
        body.setBody(operation.getOperationData());
        body.setSound("default");

        message.setMessage(body);

        return pushServerClient.sendNotification(1L, message);
    }

    @RequestMapping(value = "/authenticate", method = RequestMethod.POST)
    public @ResponseBody MobileTokenAuthenticationResponse checkOperationStatus(@RequestBody MobileTokenAuthenticationRequest request) {
        try {
            String userId = authenticate(request);
            if (userId == null) {
                final GetOperationDetailResponse operation = getOperation();
                if (operation.isExpired()) {
                    // handle operation expiration
                    final MobileTokenAuthenticationResponse response = new MobileTokenAuthenticationResponse();
                    response.setResult(AuthStepResult.FAILED);
                    response.setMessage("authentication.timeout");
                    return response;
                }
                final MobileTokenAuthenticationResponse response = new MobileTokenAuthenticationResponse();
                response.setResult(AuthStepResult.FAILED);
                response.setMessage("authentication.fail");
                return response;
            }
            return buildAuthorizationResponse(request, new AuthResponseProvider() {

                @Override
                public MobileTokenAuthenticationResponse doneAuthentication(String userId) {
                    final MobileTokenAuthenticationResponse response = new MobileTokenAuthenticationResponse();
                    response.setResult(AuthStepResult.CONFIRMED);
                    response.setMessage("authentication.success");
                    return response;
                }

                @Override
                public MobileTokenAuthenticationResponse failedAuthentication(String userId, String failedReason) {
                    final MobileTokenAuthenticationResponse response = new MobileTokenAuthenticationResponse();
                    response.setResult(AuthStepResult.FAILED);
                    response.setMessage(failedReason);
                    return response;
                }

                @Override
                public MobileTokenAuthenticationResponse continueAuthentication(String operationId, String userId, List<AuthStep> steps) {
                    final MobileTokenAuthenticationResponse response = new MobileTokenAuthenticationResponse();
                    response.setResult(AuthStepResult.CONFIRMED);
                    response.setMessage("authentication.success");
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

    @RequestMapping(value="/push/register", method = RequestMethod.POST)
    @PowerAuth(resourceId = "/push/register", signatureType = { PowerAuthSignatureTypes.POSSESSION })
    public @ResponseBody String registerDevice(@RequestBody MobileTokenPushRegisterRequest request,
                                               PowerAuthApiAuthentication apiAuthentication) {

        // Get the values from the request
        String platform = request.getPlatform();
        String token = request.getToken();

        // Check if the context is authenticated - if it is, add activation ID.
        // This assures that the activation is assigned with a correct device.
        String activationId = null;
        if (apiAuthentication != null) {
            activationId = apiAuthentication.getActivationId();
        }

        // Register the device and return response
        MobilePlatform p = MobilePlatform.Android;
        if ("ios".equalsIgnoreCase(platform)) {
            p = MobilePlatform.iOS;
        }
        boolean result = pushServerClient.registerDevice(1L, token, p, activationId);
        if (result) {
            return "OK";
        } else {
            return "NOT_OK";
        }
    }

    @RequestMapping(value = "/operation/list", method = RequestMethod.POST)
    @PowerAuth(resourceId = "/operation/list", signatureType = { PowerAuthSignatureTypes.POSSESSION })
    public @ResponseBody PowerAuthApiResponse<List<GetOperationDetailResponse>> getOperationList(PowerAuthApiAuthentication apiAuthentication) {
        if (apiAuthentication != null && apiAuthentication.getUserId() != null) {
            String userId = apiAuthentication.getUserId();
            final List<GetOperationDetailResponse> operationList = getOperationListForUser(userId);
            return new PowerAuthApiResponse(PowerAuthApiResponse.Status.OK, PowerAuthApiResponse.Encryption.NONE, operationList);
        } else {
            return new PowerAuthApiResponse(PowerAuthApiResponse.Status.OK, PowerAuthApiResponse.Encryption.NONE, null);
        }
    }

    @RequestMapping(value = "/operation/authorize", method = RequestMethod.POST)
    @PowerAuth(resourceId = "/operation/authorize")
    public @ResponseBody PowerAuthApiResponse<MobileTokenSignResponse> verifySignature(
            @RequestBody PowerAuthApiRequest<MobileTokenSignRequest> request,
            PowerAuthApiAuthentication apiAuthentication) throws NextStepServiceException {

        if (apiAuthentication != null && apiAuthentication.getUserId() != null) {
            String userId = apiAuthentication.getUserId();
            String operationId = request.getRequestObject().getId();

            final GetOperationDetailResponse operation = getOperation(operationId);
            if (operation.getOperationData().equals(request.getRequestObject().getData())) {
                //TODO: Evaluate if userId comparison is needed (one in operation + one in auth object)
                final UpdateOperationResponse updateOperationResponse = authorize(operationId, userId);
                return new PowerAuthApiResponse<>(PowerAuthApiResponse.Status.OK, null);
            } else {
                return new PowerAuthApiResponse<>(PowerAuthApiResponse.Status.ERROR, null);
            }
        } else {
            return new PowerAuthApiResponse<>(PowerAuthApiResponse.Status.ERROR, null);
        }
    }

}
