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

package io.getlime.security.powerauth.lib.webflow.authentication.mtoken.controller;

import io.getlime.core.rest.model.base.response.ObjectResponse;
import io.getlime.core.rest.model.base.response.Response;
import io.getlime.powerauth.soap.ActivationStatus;
import io.getlime.powerauth.soap.GetActivationStatusResponse;
import io.getlime.push.client.PushServerClient;
import io.getlime.push.client.PushServerClientException;
import io.getlime.push.model.entity.PushMessage;
import io.getlime.push.model.entity.PushMessageBody;
import io.getlime.push.model.entity.PushMessageSendResult;
import io.getlime.security.powerauth.lib.nextstep.model.entity.OperationFormData;
import io.getlime.security.powerauth.lib.nextstep.model.entity.OperationHistory;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthMethod;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthResult;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthStepResult;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.OperationCancelReason;
import io.getlime.security.powerauth.lib.nextstep.model.exception.NextStepServiceException;
import io.getlime.security.powerauth.lib.nextstep.model.response.GetOperationDetailResponse;
import io.getlime.security.powerauth.lib.webflow.authentication.controller.AuthMethodController;
import io.getlime.security.powerauth.lib.webflow.authentication.exception.AuthStepException;
import io.getlime.security.powerauth.lib.webflow.authentication.mtoken.model.request.MobileTokenAuthenticationRequest;
import io.getlime.security.powerauth.lib.webflow.authentication.mtoken.model.response.MobileTokenAuthenticationResponse;
import io.getlime.security.powerauth.lib.webflow.authentication.mtoken.model.response.MobileTokenInitResponse;
import io.getlime.security.powerauth.lib.webflow.authentication.mtoken.service.WebSocketMessageService;
import io.getlime.security.powerauth.lib.webflow.authentication.service.AuthMethodQueryService;
import io.getlime.security.powerauth.soap.spring.client.PowerAuthServiceClient;
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
@RequestMapping(value = "/api/auth/token/web")
public class MobileTokenOnlineController extends AuthMethodController<MobileTokenAuthenticationRequest, MobileTokenAuthenticationResponse, AuthStepException> {

    private final PushServerClient pushServerClient;
    private final WebSocketMessageService webSocketMessageService;
    private final AuthMethodQueryService authMethodQueryService;
    private final PowerAuthServiceClient powerAuthServiceClient;


    @Autowired
    public MobileTokenOnlineController(PushServerClient pushServerClient, WebSocketMessageService webSocketMessageService, AuthMethodQueryService authMethodQueryService, PowerAuthServiceClient powerAuthServiceClient) {
        this.pushServerClient = pushServerClient;
        this.webSocketMessageService = webSocketMessageService;
        this.authMethodQueryService = authMethodQueryService;
        this.powerAuthServiceClient = powerAuthServiceClient;
    }

    @Override
    protected String authenticate(MobileTokenAuthenticationRequest request) throws AuthStepException {
        final GetOperationDetailResponse operation = getOperation();
        if (operation == null) {
            throw new AuthStepException("operation.notAvailable", new NullPointerException());
        }
        if (!isAuthMethodAvailable(operation)) {
            // when AuthMethod is disabled authenticate() call should always fail
            return null;
        }
        final List<OperationHistory> history = operation.getHistory();
        for (OperationHistory h : history) {
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
    public @ResponseBody MobileTokenInitResponse initPushMessage() throws NextStepServiceException {
        final GetOperationDetailResponse operation = getOperation();

        if (operation == null) {
            // when operation is no longer available (e.g. expired), auth method should fail
            final MobileTokenInitResponse response = new MobileTokenInitResponse();
            response.setResult(AuthStepResult.AUTH_METHOD_FAILED);
            response.setMessage("operation.notAvailable");
            return response;
        }

        if (!isAuthMethodAvailable(operation)) {
            // when AuthMethod is disabled, operation should fail
            final MobileTokenInitResponse response = new MobileTokenInitResponse();
            response.setResult(AuthStepResult.AUTH_FAILED);
            response.setMessage("method.disabled");
            return response;
        }

        final String userId = operation.getUserId();

        PushMessage message = new PushMessage();
        message.setUserId(userId);
        message.getAttributes().setPersonal(true);
        message.getAttributes().setEncrypted(true);

        final OperationFormData formData = operation.getFormData();

        PushMessageBody body = new PushMessageBody();
        if (formData != null) {
            body.setTitle(formData.getTitle());
            body.setBody(formData.getMessage());
        } else {
            //TODO: Localize the messages
            body.setTitle("Confirm operation");
            body.setBody("Data: " + operation.getOperationData());
        }
        body.setSound("default");
        body.setCategory(operation.getOperationName());

        message.setBody(body);

        final MobileTokenInitResponse initResponse = new MobileTokenInitResponse();
        initResponse.setWebSocketId(webSocketMessageService.generateWebSocketId(operation.getOperationId()));

        String configuredActivationId = authMethodQueryService.getActivationIdForMobileTokenAuthMethod(userId);

        // activationId is not configured, fail with error
        if (configuredActivationId == null || configuredActivationId.isEmpty()) {
            initResponse.setResult(AuthStepResult.AUTH_FAILED);
            initResponse.setMessage("pushMessage.noActivation");
            return initResponse;
        }

        // loading of activations
        GetActivationStatusResponse activationStatusResponse = powerAuthServiceClient.getActivationStatus(configuredActivationId);

        if (activationStatusResponse.getActivationStatus() != ActivationStatus.ACTIVE) {
            initResponse.setResult(AuthStepResult.AUTH_FAILED);
            initResponse.setMessage("pushMessage.activationNotActive");
            return initResponse;
        }

        Long applicationId = activationStatusResponse.getApplicationId();

        // applicationId could not be resolved, cannot send push message
        if (applicationId == null) {
            initResponse.setResult(AuthStepResult.AUTH_FAILED);
            initResponse.setMessage("pushMessage.noApplication");
            return initResponse;
        }

        try {
            final ObjectResponse<PushMessageSendResult> response = pushServerClient.sendPushMessage(applicationId, message);
            if (response.getStatus().equals(Response.Status.OK)) {
                initResponse.setResult(AuthStepResult.CONFIRMED);
            } else {
                initResponse.setResult(AuthStepResult.AUTH_FAILED);
                initResponse.setMessage("pushMessage.fail");
            }
        } catch (PushServerClientException ex) {
            initResponse.setResult(AuthStepResult.AUTH_FAILED);
            initResponse.setMessage("pushMessage.fail");
        }
        return initResponse;
    }

    @RequestMapping(value = "/authenticate", method = RequestMethod.POST)
    public @ResponseBody MobileTokenAuthenticationResponse checkOperationStatus(@RequestBody MobileTokenAuthenticationRequest request) {

        final GetOperationDetailResponse operation = getOperation();
        if (operation == null) {
            return operationNotAvailable();
        }

        if (operation.isExpired()) {
            // handle operation expiration
            // remove WebSocket session, it is expired
            clearCurrentBrowserSession();
            webSocketMessageService.removeWebSocketSession(operation.getOperationId());
            final MobileTokenAuthenticationResponse response = new MobileTokenAuthenticationResponse();
            response.setResult(AuthStepResult.AUTH_FAILED);
            response.setMessage("operation.timeout");
            return response;
        }

        if (AuthResult.DONE.equals(operation.getResult())) {
            authenticateCurrentBrowserSession();
            webSocketMessageService.removeWebSocketSession(operation.getOperationId());
            final MobileTokenAuthenticationResponse response = new MobileTokenAuthenticationResponse();
            response.setResult(AuthStepResult.CONFIRMED);
            response.getNext().addAll(operation.getSteps());
            response.setMessage("authentication.success");
            return response;
        }

        final List<OperationHistory> history = operation.getHistory();
        for (OperationHistory h : history) {
            // in case step was already confirmed, the authentication method has already succeeded
            if (AuthMethod.POWERAUTH_TOKEN == h.getAuthMethod() && AuthStepResult.CONFIRMED.equals(h.getRequestAuthStepResult())) {
                // remove WebSocket session, authorization is confirmed
                webSocketMessageService.removeWebSocketSession(operation.getOperationId());
                final MobileTokenAuthenticationResponse response = new MobileTokenAuthenticationResponse();
                response.setResult(AuthStepResult.CONFIRMED);
                response.getNext().addAll(operation.getSteps());
                response.setMessage("authentication.success");
                return response;
            }
            // in case previous authentication lead to an authentication method failure, the authentication method has already failed
            if (AuthMethod.POWERAUTH_TOKEN == h.getAuthMethod() && AuthStepResult.AUTH_METHOD_FAILED.equals(h.getRequestAuthStepResult())) {
                // remove WebSocket session, authentication method is failed
                clearCurrentBrowserSession();
                webSocketMessageService.removeWebSocketSession(operation.getOperationId());
                final MobileTokenAuthenticationResponse response = new MobileTokenAuthenticationResponse();
                response.setResult(AuthStepResult.AUTH_METHOD_FAILED);
                response.getNext().addAll(operation.getSteps());
                response.setMessage("authentication.fail");
                return response;
            }
            // in case the authentication has been canceled, the authentication method is canceled
            if (AuthMethod.POWERAUTH_TOKEN.equals(h.getAuthMethod()) && AuthResult.FAILED.equals(h.getAuthResult()) && AuthStepResult.CANCELED.equals(h.getRequestAuthStepResult())) {
                // remove WebSocket session, operation is canceled
                clearCurrentBrowserSession();
                webSocketMessageService.removeWebSocketSession(operation.getOperationId());
                final MobileTokenAuthenticationResponse response = new MobileTokenAuthenticationResponse();
                response.setResult(AuthStepResult.CANCELED);
                response.setMessage("operation.canceled");
                return response;
            }
        }
        // otherwise authentication is still pending and waits for user action

        // the check for disabled method needs to be done after operation history is verified - the operation can be already moved to the next step
        if (!isAuthMethodAvailable(operation)) {
            // when AuthMethod is disabled, operation should fail
            clearCurrentBrowserSession();
            webSocketMessageService.removeWebSocketSession(operation.getOperationId());
            final MobileTokenAuthenticationResponse response = new MobileTokenAuthenticationResponse();
            response.setResult(AuthStepResult.AUTH_FAILED);
            response.setMessage("method.disabled");
            return response;
        }

        // WebSocket session can not be removed yet - authentication is in progress
        final MobileTokenAuthenticationResponse response = new MobileTokenAuthenticationResponse();
        response.setResult(AuthStepResult.AUTH_FAILED);
        response.setMessage("authentication.fail");
        return response;
    }

    @RequestMapping(value = "/cancel", method = RequestMethod.POST)
    public @ResponseBody MobileTokenAuthenticationResponse cancelAuthentication() {
        try {
            GetOperationDetailResponse operation = getOperation();
            if (operation == null) {
                return operationNotAvailable();
            }
            cancelAuthorization(operation.getOperationId(), null, OperationCancelReason.UNKNOWN, null);
            final MobileTokenAuthenticationResponse response = new MobileTokenAuthenticationResponse();
            response.setResult(AuthStepResult.CANCELED);
            response.setMessage("operation.canceled");
            return response;
        } catch (NextStepServiceException e) {
            final MobileTokenAuthenticationResponse response = new MobileTokenAuthenticationResponse();
            response.setResult(AuthStepResult.AUTH_FAILED);
            response.setMessage(e.getMessage());
            return response;
        }
    }

    private MobileTokenAuthenticationResponse operationNotAvailable() {
        // when operation is no longer available (e.g. expired), auth method should fail
        final MobileTokenAuthenticationResponse response = new MobileTokenAuthenticationResponse();
        response.setResult(AuthStepResult.AUTH_METHOD_FAILED);
        response.setMessage("operation.notAvailable");
        return response;
    }

}
