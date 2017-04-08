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
import io.getlime.security.powerauth.app.webauth.model.entity.registration.RegistrationRequest;
import io.getlime.security.powerauth.app.webauth.model.entity.registration.ConfirmRegistrationResponse;
import io.getlime.security.powerauth.app.webauth.model.entity.registration.TerminateSessionAndRedirectResponse;
import io.getlime.security.powerauth.app.webauth.repository.SessionRepository;
import io.getlime.security.powerauth.app.webauth.repository.model.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.math.BigDecimal;
import java.util.Arrays;

/**
 * @author Roman Strobl
 */
@Controller
public class MessageController {

    private final SimpMessagingTemplate websocket;
    private final SessionRepository sessionRepository;

    @Autowired
    public MessageController(SimpMessagingTemplate websocket, SessionRepository sessionRepository) {
        this.websocket = websocket;
        this.sessionRepository = sessionRepository;
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
            Session session = new Session();
            sessionRepository.save(session);

            String sessionId = headerAccessor.getSessionId();

            ConfirmRegistrationResponse registrationResponse = new ConfirmRegistrationResponse(session.toString());

            this.websocket.convertAndSendToUser(
                    sessionId, WebSocketConfiguration.MESSAGE_PREFIX + "/registration", registrationResponse, createHeaders(sessionId));

            // UI test - walks through all UI screens with small delays, for development only
            if (message.getPerformUITest()) {
                // simulates redirect after the reply from the Next Server
                Thread.sleep(1000);
                String operationId = "40269145-d91f-4579-badd-c57fa1133239";

                DisplayLoginFormResponse displayLogin = new DisplayLoginFormResponse(session.toString(), operationId, false);
                this.websocket.convertAndSendToUser(
                        sessionId, WebSocketConfiguration.MESSAGE_PREFIX + "/authentication", displayLogin, createHeaders(sessionId));

                // simulates displaying the payment info
                Thread.sleep(2000);
                DisplayPaymentInfoResponse displayPayment = new DisplayPaymentInfoResponse(session.toString(),
                        operationId, new BigDecimal("103"), "CZK");
                this.websocket.convertAndSendToUser(
                        sessionId, WebSocketConfiguration.MESSAGE_PREFIX + "/authorization", displayPayment, createHeaders(sessionId));

                // simulates displaying the authorization form
                Thread.sleep(2000);
                DisplayAuthorizationFormResponse displayAuthorization = new DisplayAuthorizationFormResponse(session.toString(),
                        operationId);
                this.websocket.convertAndSendToUser(
                        sessionId, WebSocketConfiguration.MESSAGE_PREFIX + "/authorization", displayAuthorization, createHeaders(sessionId));

                // simulates displaying an information message
                Thread.sleep(2000);
                DisplayMessageResponse displayOKMessage = new DisplayMessageResponse(session.toString(),
                        WebAuthMessageType.INFORMATION, "Test OK message");
                this.websocket.convertAndSendToUser(
                        sessionId, WebSocketConfiguration.MESSAGE_PREFIX + "/messages", displayOKMessage, createHeaders(sessionId));

                // simulates displaying an error message
                Thread.sleep(2000);
                DisplayMessageResponse displayErrorMessage = new DisplayMessageResponse(session.toString(),
                        WebAuthMessageType.ERROR, "Test error message");
                this.websocket.convertAndSendToUser(
                        sessionId, WebSocketConfiguration.MESSAGE_PREFIX + "/messages", displayErrorMessage, createHeaders(sessionId));

                // simulates session termination without redirect
                /*Thread.sleep(2000);
                TerminateSessionResponse terminate = new TerminateSessionResponse(,
                        session.toString();
                this.websocket.convertAndSendToUser(
                        sessionId, WebSocketConfiguration.MESSAGE_PREFIX + "/registration", terminate, createHeaders(sessionId));
                */

                // simulates session termination with redirect
                Thread.sleep(2000);
                TerminateSessionAndRedirectResponse terminateAndRedirect = new TerminateSessionAndRedirectResponse(session.toString(), "./", 5);
                this.websocket.convertAndSendToUser(
                        sessionId, WebSocketConfiguration.MESSAGE_PREFIX + "/registration", terminateAndRedirect, createHeaders(sessionId));
                sessionRepository.delete(session);
            } else {
                // responses are based on communication with user, also for development only
                // real communication will be initiated by OAuth authentication

                String operationId = "40269145-d91f-4579-badd-c57fa1133239";

                DisplayLoginFormResponse displayLogin = new DisplayLoginFormResponse(session.toString(), operationId, false);
                this.websocket.convertAndSendToUser(
                        sessionId, WebSocketConfiguration.MESSAGE_PREFIX + "/authentication", displayLogin, createHeaders(sessionId));
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
                // fake authentication - if it succeeds, user is moved to the authorization screen
                if (username != null
                        && password != null
                        && authenticationRequest.getUsername().equals("test")
                        && Arrays.equals(authenticationRequest.getPassword(), "test".toCharArray())) {
                    DisplayPaymentInfoResponse displayPayment = new DisplayPaymentInfoResponse(authenticationRequest.getSessionId(),
                            authenticationRequest.getOperationId(), new BigDecimal("1000"), "EUR");
                    this.websocket.convertAndSendToUser(
                            sessionId, WebSocketConfiguration.MESSAGE_PREFIX + "/authorization", displayPayment, createHeaders(sessionId));
                } else {
                    DisplayMessageResponse displayErrorMessage = new DisplayMessageResponse(authenticationRequest.getSessionId(),
                            WebAuthMessageType.ERROR, "Login failed.");
                    this.websocket.convertAndSendToUser(
                            sessionId, WebSocketConfiguration.MESSAGE_PREFIX + "/messages", displayErrorMessage, createHeaders(sessionId));
                }
                break;
            case LOGIN_CANCEL:
                // login canceled
                DisplayMessageResponse displayErrorMessage = new DisplayMessageResponse(authenticationRequest.getSessionId(),
                        WebAuthMessageType.ERROR, "Authentication has been canceled.");
                this.websocket.convertAndSendToUser(
                        sessionId, WebSocketConfiguration.MESSAGE_PREFIX + "/messages", displayErrorMessage, createHeaders(sessionId));
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
                this.websocket.convertAndSendToUser(
                        sessionId, WebSocketConfiguration.MESSAGE_PREFIX + "/authorization", displayAuth, createHeaders(sessionId));
                break;
            case PAYMENT_CANCEL:
                // payment canceled
                DisplayMessageResponse displayErrorMessage = new DisplayMessageResponse(authorizationRequest.getSessionId(),
                        WebAuthMessageType.ERROR, "Payment has been canceled.");
                this.websocket.convertAndSendToUser(
                        sessionId, WebSocketConfiguration.MESSAGE_PREFIX + "/messages", displayErrorMessage, createHeaders(sessionId));
                break;
            case PAYMENT_AUTHORIZATION_CONFIRM:
                // fake authorization
                if (authorizationRequest.getAuthorizationCode() != null && authorizationRequest.getAuthorizationCode().equals("12345")) {
                    DisplayMessageResponse displayMessage = new DisplayMessageResponse(authorizationRequest.getSessionId(), WebAuthMessageType.INFORMATION,
                            "Payment has been authorized, you will be redirected to your bank...");
                    this.websocket.convertAndSendToUser(
                            sessionId, WebSocketConfiguration.MESSAGE_PREFIX + "/messages", displayMessage, createHeaders(sessionId));
                    Thread.sleep(5000);
                    TerminateSessionAndRedirectResponse terminateAndRedirect = new TerminateSessionAndRedirectResponse(authorizationRequest.getSessionId(), "./", 0);
                    this.websocket.convertAndSendToUser(
                            sessionId, WebSocketConfiguration.MESSAGE_PREFIX + "/registration", terminateAndRedirect, createHeaders(sessionId));
                    sessionRepository.delete(new Long(authorizationRequest.getSessionId()));
                } else {
                    // payment not authorized
                    DisplayMessageResponse displayUnauthorizedMessage = new DisplayMessageResponse(authorizationRequest.getSessionId(),
                            WebAuthMessageType.ERROR, "Payment authorization failed.");
                    this.websocket.convertAndSendToUser(
                            sessionId, WebSocketConfiguration.MESSAGE_PREFIX + "/messages", displayUnauthorizedMessage, createHeaders(sessionId));
                    break;
                }
                break;
            case PAYMENT_AUTHORIZATION_CANCEL:
                // payment authorization canceled
                DisplayMessageResponse displayErrorMessageAuth = new DisplayMessageResponse(authorizationRequest.getSessionId(),
                        WebAuthMessageType.ERROR, "Payment authorization has been canceled.");
                this.websocket.convertAndSendToUser(
                        sessionId, WebSocketConfiguration.MESSAGE_PREFIX + "/messages", displayErrorMessageAuth, createHeaders(sessionId));
                break;
        }
    }

}
