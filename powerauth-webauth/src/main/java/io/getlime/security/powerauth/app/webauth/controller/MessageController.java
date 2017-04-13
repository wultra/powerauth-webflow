/*
 * Copyright 2016 Lime - HighTech Solutions s.r.o.
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
package io.getlime.security.powerauth.app.webauth.controller;

import io.getlime.security.powerauth.app.webauth.configuration.WebSocketConfiguration;
import io.getlime.security.powerauth.app.webauth.model.entity.WebSocketJsonMessage;
import io.getlime.security.powerauth.app.webauth.model.entity.authentication.AuthenticationRequest;
import io.getlime.security.powerauth.app.webauth.model.entity.authentication.DisplayLoginFormResponse;
import io.getlime.security.powerauth.app.webauth.model.entity.authorization.AuthorizationRequest;
import io.getlime.security.powerauth.app.webauth.model.entity.authorization.DisplayAuthorizationFormResponse;
import io.getlime.security.powerauth.app.webauth.model.entity.authorization.DisplayPaymentInfoResponse;
import io.getlime.security.powerauth.app.webauth.model.entity.messages.DisplayMessageResponse;
import io.getlime.security.powerauth.app.webauth.model.entity.messages.WebAuthMessageType;
import io.getlime.security.powerauth.app.webauth.model.entity.registration.ConfirmRegistrationResponse;
import io.getlime.security.powerauth.app.webauth.model.entity.registration.RegistrationRequest;
import io.getlime.security.powerauth.app.webauth.model.entity.registration.TerminateSessionAndRedirectResponse;
import io.getlime.security.powerauth.app.webauth.repository.SessionRepository;
import io.getlime.security.powerauth.app.webauth.repository.model.Session;
import io.getlime.security.powerauth.app.webauth.service.AuthenticationService;
import io.getlime.security.powerauth.app.webauth.service.NextMessageResolutionService;
import io.getlime.security.powerauth.app.webauth.service.NextStepService;
import io.getlime.security.powerauth.lib.credentials.model.AuthenticationResponse;
import io.getlime.security.powerauth.lib.credentials.model.AuthenticationStatus;
import io.getlime.security.powerauth.lib.nextstep.model.base.Response;
import io.getlime.security.powerauth.lib.nextstep.model.entity.KeyValueParameter;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthMethod;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthStepResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Roman Strobl
 */
@Controller
public class MessageController {

    private final SimpMessagingTemplate websocket;
    private final SessionRepository sessionRepository;
    private final AuthenticationService authService;
    private final NextStepService nextStepService;
    private final NextMessageResolutionService resolutionService;

    @Autowired
    public MessageController(SimpMessagingTemplate websocket, SessionRepository sessionRepository, AuthenticationService authService,
                             NextStepService nextStepService, NextMessageResolutionService resolutionService) {
        this.websocket = websocket;
        this.sessionRepository = sessionRepository;
        this.authService = authService;
        this.nextStepService = nextStepService;
        this.resolutionService = resolutionService;
    }

    private MessageHeaders createHeaders(String sessionId) {
        SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.create(SimpMessageType.MESSAGE);
        headerAccessor.setSessionId(sessionId);
        headerAccessor.setLeaveMutable(true);
        return headerAccessor.getMessageHeaders();
    }

    @MessageMapping("/registration")
    public void register(SimpMessageHeaderAccessor headerAccessor, RegistrationRequest message) throws Exception {
        System.out.println("Received registration message: " + message);
        if (message.getAction() == WebSocketJsonMessage.WebAuthAction.REGISTER) {
            String sessionId = headerAccessor.getSessionId();
            Session session = new Session(sessionId);
            sessionRepository.save(session);

            ConfirmRegistrationResponse registrationResponse = new ConfirmRegistrationResponse(sessionId);
            sendMessage(registrationResponse, sessionId);

            // UI test - walks through all UI screens with small delays, for development only
            if (message.getPerformUITest()) {
                // simulates redirect after the reply from the Next Server
                Thread.sleep(1000);
                String operationId = "40269145-d91f-4579-badd-c57fa1133239";

                DisplayLoginFormResponse displayLogin = new DisplayLoginFormResponse(sessionId, operationId, "Please sign in", false);
                sendMessage(displayLogin, sessionId);

                // simulates displaying the payment info
                Thread.sleep(2000);
                DisplayPaymentInfoResponse displayPayment = new DisplayPaymentInfoResponse(sessionId, operationId, new BigDecimal("103"), "CZK");
                sendMessage(displayPayment, sessionId);

                // simulates displaying the authorization form
                Thread.sleep(2000);
                DisplayAuthorizationFormResponse displayAuthorization = new DisplayAuthorizationFormResponse(sessionId, operationId);
                sendMessage(displayAuthorization, sessionId);

                // simulates displaying an information message
                Thread.sleep(2000);
                DisplayMessageResponse displayOKMessage = new DisplayMessageResponse(sessionId, WebAuthMessageType.INFORMATION, "Test OK message");
                sendMessage(displayOKMessage, sessionId);

                // simulates displaying an error message
                Thread.sleep(2000);
                DisplayMessageResponse displayErrorMessage = new DisplayMessageResponse(sessionId, WebAuthMessageType.ERROR, "Test error message");
                sendMessage(displayErrorMessage, sessionId);

                // simulates session termination without redirect
                /*Thread.sleep(2000);
                TerminateSessionResponse terminate = new TerminateSessionResponse(,
                        sessionId;
                sendMessage(terminates, sessionId);
                */

                // simulates session termination with redirect
                Thread.sleep(2000);
                TerminateSessionAndRedirectResponse terminateAndRedirect = new TerminateSessionAndRedirectResponse(sessionId, "./", 5);
                sendMessage(terminateAndRedirect, sessionId);
                sessionRepository.delete(session);
            } else {
                // responses are based on communication with user, also for development only
                // real communication will be initiated by OAuth authentication

                // for testing without Next Step server
                /*
                String operationId = "40269145-d91f-4579-badd-c57fa1133239";

                DisplayLoginFormResponse displayLogin = new DisplayLoginFormResponse(sessionId, operationId, false);
                this.websocket.convertAndSendToUser(
                        sessionId, WebSocketConfiguration.MESSAGE_PREFIX + "/authentication", displayLogin, createHeaders(sessionId));
                */

                // Testing of Next Step server
                String operationName = "payment";
                String operationData = "{\"amount\":100,\"currency\":\\\"CZK\",\"to\":\"CZ12000012345678901234\"}";
                List<KeyValueParameter> params = new ArrayList<>();
                KeyValueParameter param = new KeyValueParameter("risk", "0.639");
                params.add(param);
                Response<?> response = nextStepService.createOperation(operationName, operationData, params);
                WebSocketJsonMessage nextMessage = resolutionService.resolveNextMessage(response, null, sessionId);
                sendMessage(nextMessage, sessionId);
            }
        }
    }

    @MessageMapping("/authentication")
    public void authenticate(SimpMessageHeaderAccessor headerAccessor, AuthenticationRequest authenticationRequest) throws Exception {
        System.out.println("Received authentication message: " + authenticationRequest);
        String sessionId = headerAccessor.getSessionId();
        switch (authenticationRequest.getAction()) {
            case LOGIN_CONFIRM:
                String username = authenticationRequest.getUsername();
                char[] password = authenticationRequest.getPassword();
                if (username==null) {
                    username = "";
                }
                if (password==null) {
                    password = "".toCharArray();
                }
                // authenticate with the Credentials Server
                AuthenticationResponse responseCS = authService.authenticate(username, password);

                if (responseCS.getStatus() == AuthenticationStatus.SUCCESS) {
                    // for testing without Next Step server
                    /*
                    DisplayPaymentInfoResponse displayPayment = new DisplayPaymentInfoResponse(authenticationRequest.getSessionId(),
                            authenticationRequest.getOperationId(), new BigDecimal("1000"), "EUR");
                    sendMessage(displayPayment, sessionId);
                    */
                    List<KeyValueParameter> params = new ArrayList<>();
                    KeyValueParameter param = new KeyValueParameter("risk", "0.523");
                    params.add(param);
                    Response<?> responseNS = nextStepService.updateOperation(authenticationRequest.getOperationId(), username, AuthMethod.USERNAME_PASSWORD_AUTH,
                            AuthStepResult.CONFIRMED, params);
                    WebSocketJsonMessage nextMessage = resolutionService.resolveNextMessage(responseNS, responseCS.getStatus(), sessionId);
                    sendMessage(nextMessage, sessionId);
                } else {
                    // for testing without Next Step server
                    /*
                    DisplayMessageResponse displayErrorMessage = new DisplayMessageResponse(authenticationRequest.getSessionId(),
                            WebAuthMessageType.ERROR, "Login failed.");
                    sendMessage(displayErrorMessage, sessionId);
                    */
                    List<KeyValueParameter> params = new ArrayList<>();
                    KeyValueParameter param = new KeyValueParameter("risk", "0.523");
                    params.add(param);
                    Response<?> responseNS = nextStepService.updateOperation(authenticationRequest.getOperationId(), username, AuthMethod.USERNAME_PASSWORD_AUTH,
                            AuthStepResult.FAILED, params);
                    WebSocketJsonMessage nextMessage = resolutionService.resolveNextMessage(responseNS, responseCS.getStatus(), sessionId);
                    sendMessage(nextMessage, sessionId);
                }
                break;
            case LOGIN_CANCEL:
                // login canceled
                // for testing without Next Step server
                /*
                DisplayMessageResponse displayErrorMessage = new DisplayMessageResponse(authenticationRequest.getSessionId(),
                        WebAuthMessageType.ERROR, "Authentication has been canceled.");
                sendMessage(displayErrorMessage, sessionId);
                */
                List<KeyValueParameter> params = new ArrayList<>();
                KeyValueParameter param = new KeyValueParameter("risk", "0.523");
                params.add(param);
                Response<?> responseNS = nextStepService.updateOperation(authenticationRequest.getOperationId(), null, AuthMethod.USERNAME_PASSWORD_AUTH,
                        AuthStepResult.CANCELED, params);
                WebSocketJsonMessage nextMessage = resolutionService.resolveNextMessage(responseNS, null, sessionId);
                sendMessage(nextMessage, sessionId);
                break;
        }
    }

    @MessageMapping("/authorization")
    public void authorize(SimpMessageHeaderAccessor headerAccessor, AuthorizationRequest authorizationRequest) throws Exception {
        System.out.println("Received authorization message: " + authorizationRequest);
        String sessionId = headerAccessor.getSessionId();
        switch (authorizationRequest.getAction()) {
            case PAYMENT_CONFIRM:
                // authorization for the payment
                DisplayAuthorizationFormResponse displayAuth = new DisplayAuthorizationFormResponse(authorizationRequest.getSessionId(), authorizationRequest.getOperationId());
                sendMessage(displayAuth, sessionId);
                break;
            case PAYMENT_CANCEL:
                // payment canceled
                DisplayMessageResponse displayErrorMessage = new DisplayMessageResponse(authorizationRequest.getSessionId(),
                        WebAuthMessageType.ERROR, "Payment has been canceled.");
                sendMessage(displayErrorMessage, sessionId);
                break;
            case PAYMENT_AUTHORIZATION_CONFIRM:
                // fake authorization
                if (authorizationRequest.getAuthorizationCode() != null && authorizationRequest.getAuthorizationCode().equals("12345")) {
                    DisplayMessageResponse displayMessage = new DisplayMessageResponse(authorizationRequest.getSessionId(), WebAuthMessageType.INFORMATION,
                            "Payment has been authorized, you will be redirected to your bank...");
                    sendMessage(displayMessage, sessionId);
                    Thread.sleep(5000);
                    TerminateSessionAndRedirectResponse terminateAndRedirect = new TerminateSessionAndRedirectResponse(authorizationRequest.getSessionId(), "./", 0);
                    sendMessage(terminateAndRedirect, sessionId);
                    sessionRepository.delete(new Long(authorizationRequest.getSessionId()));
                } else {
                    // payment not authorized
                    DisplayMessageResponse displayUnauthorizedMessage = new DisplayMessageResponse(authorizationRequest.getSessionId(),
                            WebAuthMessageType.ERROR, "Payment authorization failed.");
                    sendMessage(displayUnauthorizedMessage, sessionId);
                    break;
                }
                break;
            case PAYMENT_AUTHORIZATION_CANCEL:
                // payment authorization canceled
                DisplayMessageResponse displayErrorMessageAuth = new DisplayMessageResponse(authorizationRequest.getSessionId(),
                        WebAuthMessageType.ERROR, "Payment authorization has been canceled.");
                sendMessage(displayErrorMessageAuth, sessionId);
                break;
        }
    }

    private void sendMessage(WebSocketJsonMessage message, String sessionId) {
        switch (message.getAction()) {
            case REGISTRATION_CONFIRM:
            case TERMINATE:
            case TERMINATE_REDIRECT:
                websocket.convertAndSendToUser(
                        sessionId, WebSocketConfiguration.MESSAGE_PREFIX + "/registration", message, createHeaders(sessionId));
                break;
            case DISPLAY_LOGIN_FORM:
                websocket.convertAndSendToUser(
                        sessionId, WebSocketConfiguration.MESSAGE_PREFIX + "/authentication", message, createHeaders(sessionId));
                break;
            case DISPLAY_PAYMENT_AUTHORIZATION_FORM:
            case DISPLAY_PAYMENT_INFO:
                websocket.convertAndSendToUser(
                        sessionId, WebSocketConfiguration.MESSAGE_PREFIX + "/authorization", message, createHeaders(sessionId));
                break;
            case DISPLAY_MESSAGE:
                websocket.convertAndSendToUser(
                        sessionId, WebSocketConfiguration.MESSAGE_PREFIX + "/messages", message, createHeaders(sessionId));
                break;
            default:
                throw new IllegalStateException("Invalid action: " + message.getAction());
        }

    }

}
