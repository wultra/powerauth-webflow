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
import io.getlime.security.powerauth.app.webflow.i18n.I18NService;
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
import io.getlime.security.powerauth.lib.webflow.authentication.mtoken.exception.ActivationNotActiveException;
import io.getlime.security.powerauth.lib.webflow.authentication.mtoken.exception.ActivationNotAvailableException;
import io.getlime.security.powerauth.lib.webflow.authentication.mtoken.model.request.MobileTokenAuthenticationRequest;
import io.getlime.security.powerauth.lib.webflow.authentication.mtoken.model.response.MobileTokenAuthenticationResponse;
import io.getlime.security.powerauth.lib.webflow.authentication.mtoken.model.response.MobileTokenInitResponse;
import io.getlime.security.powerauth.lib.webflow.authentication.mtoken.service.WebSocketMessageService;
import io.getlime.security.powerauth.lib.webflow.authentication.service.AuthMethodQueryService;
import io.getlime.security.powerauth.soap.spring.client.PowerAuthServiceClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.AbstractMessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Online mobile token authentication controller.
 *
 * @author Petr Dvorak, petr@lime-company.eu
 */
@Controller
@RequestMapping(value = "/api/auth/token/web")
public class MobileTokenOnlineController extends AuthMethodController<MobileTokenAuthenticationRequest, MobileTokenAuthenticationResponse, AuthStepException> {

    private static final String PUSH_MESSAGE_TITLE = "title";
    private static final String PUSH_MESSAGE_SUBTITLE = "subtitle";
    private static final String PUSH_MESSAGE_AUTH_STEP_FINISHED_TITLE = "AUTH_STEP_FINISHED";
    private static final String PUSH_MESSAGE_SOUND = "default";

    private final PushServerClient pushServerClient;
    private final WebSocketMessageService webSocketMessageService;
    private final AuthMethodQueryService authMethodQueryService;
    private final PowerAuthServiceClient powerAuthServiceClient;
    private final I18NService i18nService;

    /**
     * Controller constructor.
     *
     * @param pushServerClient PowerAuth 2.0 Push server client.
     * @param webSocketMessageService Web Socket message service.
     * @param authMethodQueryService Authentication method query service.
     * @param powerAuthServiceClient PowerAuth 2.0 client.
     * @param i18nService I18N service.
     */
    @Autowired
    public MobileTokenOnlineController(PushServerClient pushServerClient, WebSocketMessageService webSocketMessageService, AuthMethodQueryService authMethodQueryService, PowerAuthServiceClient powerAuthServiceClient, I18NService i18nService) {
        this.pushServerClient = pushServerClient;
        this.webSocketMessageService = webSocketMessageService;
        this.authMethodQueryService = authMethodQueryService;
        this.powerAuthServiceClient = powerAuthServiceClient;
        this.i18nService = i18nService;
    }

    /**
     * Authenticate using online mobile token.
     * @param request Online mobile token authentication rquest.
     * @return User ID.
     * @throws AuthStepException Thrown when authentication fails.
     */
    @Override
    protected String authenticate(MobileTokenAuthenticationRequest request) throws AuthStepException {
        final GetOperationDetailResponse operation = getOperation();
        final List<OperationHistory> history = operation.getHistory();
        for (OperationHistory h : history) {
            if (AuthMethod.POWERAUTH_TOKEN.equals(h.getAuthMethod())
                    && !AuthResult.FAILED.equals(h.getAuthResult())) {
                return operation.getUserId();
            }
        }
        return null;
    }

    /**
     * Get current authentication method.
     * @return Current authentication method.
     */
    @Override
    protected AuthMethod getAuthMethodName() {
        return AuthMethod.POWERAUTH_TOKEN;
    }

    /**
     * Initialize push message.
     * @return Initialization response.
     * @throws NextStepServiceException Thrown when communication with Next Step server fails.
     * @throws AuthStepException Thrown when authentication fails.
     */
    @RequestMapping(value = "/init", method = RequestMethod.POST)
    public @ResponseBody MobileTokenInitResponse initPushMessage() throws NextStepServiceException, AuthStepException {
        final GetOperationDetailResponse operation = getOperation();

        final MobileTokenInitResponse initResponse = new MobileTokenInitResponse();
        initResponse.setWebSocketId(webSocketMessageService.generateWebSocketId(operation.getOperationId()));

        String activationId;
        Long applicationId;
        try {
            activationId = getActivationId(operation);
        } catch (ActivationNotAvailableException e) {
            initResponse.setResult(AuthStepResult.AUTH_FAILED);
            initResponse.setMessage("pushMessage.noActivation");
            return initResponse;
        }

        try {
            applicationId = getApplicationId(activationId);
        } catch (ActivationNotActiveException e) {
            initResponse.setResult(AuthStepResult.AUTH_FAILED);
            initResponse.setMessage("pushMessage.activationNotActive");
            return initResponse;
        }

        try {
            final PushMessage message = createAuthStepInitPushMessage(operation, activationId);
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

    /**
     * Perform online mobile token authentication. Method can be called repeatedly to verify current authentication status.
     *
     * @param request Online mobile token authentication request.
     * @return Authentication result.
     * @throws AuthStepException Thrown when authentication fails.
     */
    @RequestMapping(value = "/authenticate", method = RequestMethod.POST)
    public @ResponseBody MobileTokenAuthenticationResponse checkOperationStatus(@RequestBody MobileTokenAuthenticationRequest request) throws AuthStepException {

        final GetOperationDetailResponse operation = getOperation();

        if (operation.isExpired()) {
            // handle operation expiration
            // remove WebSocket session, it is expired
            clearCurrentBrowserSession();
            webSocketMessageService.removeWebSocketSession(operation.getOperationId());
            final MobileTokenAuthenticationResponse response = new MobileTokenAuthenticationResponse();
            response.setResult(AuthStepResult.AUTH_FAILED);
            response.setMessage("operation.timeout");
            sendAuthStepFinishedPushMessage(operation, response.getMessage());
            return response;
        }

        if (AuthResult.DONE.equals(operation.getResult())) {
            authenticateCurrentBrowserSession();
            webSocketMessageService.removeWebSocketSession(operation.getOperationId());
            final MobileTokenAuthenticationResponse response = new MobileTokenAuthenticationResponse();
            response.setResult(AuthStepResult.CONFIRMED);
            response.getNext().addAll(operation.getSteps());
            response.setMessage("authentication.success");
            sendAuthStepFinishedPushMessage(operation, response.getMessage());
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
                sendAuthStepFinishedPushMessage(operation, response.getMessage());
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
                sendAuthStepFinishedPushMessage(operation, response.getMessage());
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
                sendAuthStepFinishedPushMessage(operation, response.getMessage());
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
            response.setMessage("operation.methodNotAvailable");
            sendAuthStepFinishedPushMessage(operation, response.getMessage());
            return response;
        }

        // WebSocket session can not be removed yet - authentication is in progress
        final MobileTokenAuthenticationResponse response = new MobileTokenAuthenticationResponse();
        response.setResult(AuthStepResult.AUTH_FAILED);
        response.setMessage("authentication.fail");
        return response;
    }

    /**
     * Cancel operation.
     * @return Object response.
     * @throws AuthStepException Thrown when operation could not be canceled.
     */
    @RequestMapping(value = "/cancel", method = RequestMethod.POST)
    public @ResponseBody MobileTokenAuthenticationResponse cancelAuthentication() throws AuthStepException {
        try {
            GetOperationDetailResponse operation = getOperation();
            cancelAuthorization(operation.getOperationId(), null, OperationCancelReason.UNKNOWN, null);
            final MobileTokenAuthenticationResponse response = new MobileTokenAuthenticationResponse();
            response.setResult(AuthStepResult.CANCELED);
            response.setMessage("operation.canceled");
            sendAuthStepFinishedPushMessage(operation, response.getMessage());
            return response;
        } catch (NextStepServiceException e) {
            final MobileTokenAuthenticationResponse response = new MobileTokenAuthenticationResponse();
            response.setResult(AuthStepResult.AUTH_FAILED);
            response.setMessage(e.getMessage());
            return response;
        }
    }

    /**
     * Send push message when authentication step is finished.
     * @param operation Operation.
     * @param statusMessage Status message.
     */
    private void sendAuthStepFinishedPushMessage(GetOperationDetailResponse operation, String statusMessage) {
        try {
            String activationId = getActivationId(operation);
            PushMessage message = createAuthStepFinishedPushMessage(operation, activationId, statusMessage);
            Long applicationId = getApplicationId(activationId);
            pushServerClient.sendPushMessage(applicationId, message);
        } catch (Exception ex) {
            // Exception which occurs when push message is sent is not critical, return regular response.
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Error occurred in Mobile Token API component", ex);
        }
    }

    /**
     * Resolve activation ID from operation.
     * @param operation Operation.
     * @return Activation ID.
     * @throws NextStepServiceException Throw when communication with Next Step service fails.
     * @throws ActivationNotAvailableException Thrown when activation is not configured.
     */
    private String getActivationId(GetOperationDetailResponse operation) throws NextStepServiceException, ActivationNotAvailableException {
        String configuredActivationId = authMethodQueryService.getActivationIdForMobileTokenAuthMethod(operation.getUserId());
        if (configuredActivationId == null || configuredActivationId.isEmpty()) {
            throw new ActivationNotAvailableException();
        }
        return configuredActivationId;
    }

    /**
     * Resolve application ID from activation ID.
     * @param activationId Activation ID.
     * @return Application ID.
     * @throws ActivationNotActiveException Thrown when activation is not active.
     */
    private Long getApplicationId(String activationId) throws ActivationNotActiveException {
        GetActivationStatusResponse activationStatusResponse = powerAuthServiceClient.getActivationStatus(activationId);
        if (activationStatusResponse.getActivationStatus() != ActivationStatus.ACTIVE) {
            throw new ActivationNotActiveException();
        }
        return activationStatusResponse.getApplicationId();
    }

    /**
     * Create an initial push message.
     * @param operation Operation.
     * @return Push message.
     */
    private PushMessage createAuthStepInitPushMessage(GetOperationDetailResponse operation, String activationId) {
        PushMessage message = new PushMessage();
        message.setUserId(operation.getUserId());
        message.setActivationId(activationId);
        message.getAttributes().setPersonal(true);
        message.getAttributes().setEncrypted(true);

        final OperationFormData formData = operation.getFormData();

        PushMessageBody body = new PushMessageBody();
        if (formData != null) {
            body.setTitle(formData.getTitle().getValue());
            body.setBody(formData.getMessage().getValue());
        } else {
            AbstractMessageSource messageSource = i18nService.getMessageSource();
            String[] operationData = new String[]{operation.getOperationData()};
            body.setTitle(messageSource.getMessage("push.confirmOperation", null, LocaleContextHolder.getLocale()));
            body.setBody(messageSource.getMessage("push.data", operationData, LocaleContextHolder.getLocale()));
        }
        body.setSound(PUSH_MESSAGE_SOUND);
        body.setCategory(operation.getOperationName());

        message.setBody(body);
        return message;
    }

    /**
     * Create cancel push message.
     * @param operation Operation.
     * @return Push message.
     */
    private PushMessage createAuthStepFinishedPushMessage(GetOperationDetailResponse operation, String activationId, String statusMessage) {
        PushMessage message = new PushMessage();
        message.setUserId(operation.getUserId());
        message.setActivationId(activationId);
        message.getAttributes().setPersonal(true);
        message.getAttributes().setEncrypted(true);
        message.getAttributes().setSilent(true);

        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put(PUSH_MESSAGE_TITLE, PUSH_MESSAGE_AUTH_STEP_FINISHED_TITLE);
        dataMap.put(PUSH_MESSAGE_SUBTITLE, statusMessage);

        PushMessageBody body = new PushMessageBody();
        body.setExtras(dataMap);
        body.setCategory(operation.getOperationName());

        message.setBody(body);
        return message;
    }

}
