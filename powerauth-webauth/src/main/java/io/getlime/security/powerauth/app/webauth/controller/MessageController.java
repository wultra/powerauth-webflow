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
import io.getlime.security.powerauth.app.webauth.model.entity.authentication.ResponseDisplayLoginForm;
import io.getlime.security.powerauth.app.webauth.model.entity.authorization.ResponseDisplayAuthorizationForm;
import io.getlime.security.powerauth.app.webauth.model.entity.authorization.ResponseDisplayPaymentInfo;
import io.getlime.security.powerauth.app.webauth.model.entity.messages.ResponseDisplayMessage;
import io.getlime.security.powerauth.app.webauth.model.entity.messages.WebAuthMessageType;
import io.getlime.security.powerauth.app.webauth.model.entity.registration.ResponseConfirmRegistration;
import io.getlime.security.powerauth.app.webauth.model.entity.registration.ResponseTerminateAndRedirect;
import io.getlime.security.powerauth.app.webauth.repository.SessionRepository;
import io.getlime.security.powerauth.app.webauth.repository.model.Session;
import io.getlime.security.powerauth.app.webauth.model.entity.registration.RequestRegistration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import java.math.BigDecimal;

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
    public void register(SimpMessageHeaderAccessor headerAccessor, RequestRegistration message) throws Exception {
        System.out.println("Received registration message: " + message);
        if (message.getAction() == WebSocketJsonMessage.WebAuthAction.REGISTER) {
            Session session = new Session();
            sessionRepository.save(session);

            String sessionId = headerAccessor.getSessionId();

            ResponseConfirmRegistration registrationResponse = new ResponseConfirmRegistration(session.toString());

            this.websocket.convertAndSendToUser(
                    sessionId,WebSocketConfiguration.MESSAGE_PREFIX + "/registration", registrationResponse, createHeaders(sessionId));

            if (message.getPerformUITest()) {
                // simulates redirect after the reply from the Next Server
                Thread.sleep(1000);
                String operationId = "40269145-d91f-4579-badd-c57fa1133239";

                ResponseDisplayLoginForm displayLogin = new ResponseDisplayLoginForm(session.toString(), operationId, false);
                this.websocket.convertAndSendToUser(
                        sessionId, WebSocketConfiguration.MESSAGE_PREFIX + "/authentication", displayLogin, createHeaders(sessionId));

                // simulates displaying the payment info
                Thread.sleep(2000);
                ResponseDisplayPaymentInfo displayPayment = new ResponseDisplayPaymentInfo(session.toString(),
                        operationId, new BigDecimal("103"), "CZK");
                this.websocket.convertAndSendToUser(
                        sessionId, WebSocketConfiguration.MESSAGE_PREFIX + "/authorization", displayPayment, createHeaders(sessionId));

                // simulates displaying the authorization form
                Thread.sleep(2000);
                ResponseDisplayAuthorizationForm displayAuthorization = new ResponseDisplayAuthorizationForm(session.toString(),
                        operationId);
                this.websocket.convertAndSendToUser(
                        sessionId, WebSocketConfiguration.MESSAGE_PREFIX + "/authorization", displayAuthorization, createHeaders(sessionId));

                // simulates displaying an information message
                Thread.sleep(2000);
                ResponseDisplayMessage displayOKMessage = new ResponseDisplayMessage(session.toString(),
                        WebAuthMessageType.INFORMATION, "Test OK message");
                this.websocket.convertAndSendToUser(
                        sessionId, WebSocketConfiguration.MESSAGE_PREFIX + "/messages", displayOKMessage, createHeaders(sessionId));

                // simulates displaying an error message
                Thread.sleep(2000);
                ResponseDisplayMessage displayErrorMessage = new ResponseDisplayMessage(session.toString(),
                        WebAuthMessageType.ERROR, "Test error message");
                this.websocket.convertAndSendToUser(
                        sessionId, WebSocketConfiguration.MESSAGE_PREFIX + "/messages", displayErrorMessage, createHeaders(sessionId));

                // simulates session termination without redirect
                /*Thread.sleep(2000);
                ResponseTerminate terminate = new ResponseTerminate(,
                        session.toString();
                this.websocket.convertAndSendToUser(
                        sessionId, WebSocketConfiguration.MESSAGE_PREFIX + "/registration", terminate, createHeaders(sessionId));
                */

                // simulates session termination with redirect
                Thread.sleep(2000);
                ResponseTerminateAndRedirect terminateAndRedirect = new ResponseTerminateAndRedirect(session.toString(), "./", 5);
                this.websocket.convertAndSendToUser(
                        sessionId, WebSocketConfiguration.MESSAGE_PREFIX + "/registration", terminateAndRedirect, createHeaders(sessionId));
                sessionRepository.delete(session);
            }
        }
    }

}
