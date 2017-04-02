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
import io.getlime.security.powerauth.app.webauth.repository.SessionRepository;
import io.getlime.security.powerauth.app.webauth.repository.model.Session;
import io.getlime.security.powerauth.app.webauth.model.entity.RegistrationMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

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
    public void register(SimpMessageHeaderAccessor headerAccessor, RegistrationMessage message) throws Exception {
        System.out.println("Received registration message: " + message);
        if ("REGISTER".equals(message.getAction())) {
            Session session = new Session();
            sessionRepository.save(session);

            String sessionId = headerAccessor.getSessionId();

            this.websocket.convertAndSendToUser(
                    sessionId,WebSocketConfiguration.MESSAGE_PREFIX + "/registration", "{\n" +
                            "    \"action\": \"REGISTRATION_CONFIRM\",\n" +
                            "    \"sessionId\": \"" + session.toString() + "\"\n" +
                            "}", createHeaders(sessionId));

            if (message.getPerformUITest()) {
                // simulace redirectu po odpovědi od CBDS
                Thread.sleep(1000);
                this.websocket.convertAndSendToUser(
                        sessionId, WebSocketConfiguration.MESSAGE_PREFIX + "/authentication", "{\n" +
                                "    \"action\": \"DISPLAY_LOGIN_FORM\",\n" +
                                "    \"sessionId\": \"" + session.toString() + "\"\n" +
                                "}", createHeaders(sessionId));

                // simulace zobrazení payment info
                Thread.sleep(2000);
                this.websocket.convertAndSendToUser(
                        sessionId, WebSocketConfiguration.MESSAGE_PREFIX + "/authorization", "{\n" +
                                "    \"action\": \"DISPLAY_PAYMENT_INFO\",\n" +
                                "    \"sessionId\": \"" + session.toString() + "\",\n" +
                                "    \"operationId\": \"40269145-d91f-4579-badd-c57fa1133239\",\n" +
                                "    \"amount\": \"103\",\n" +
                                "    \"currency\": \"CZK\"\n" +
                                "}", createHeaders(sessionId));

                // simulace zobrazení autorizace
                Thread.sleep(2000);
                this.websocket.convertAndSendToUser(
                        sessionId, WebSocketConfiguration.MESSAGE_PREFIX + "/authorization", "{\n" +
                                "    \"action\": \"DISPLAY_PAYMENT_AUTHORIZATION_FROM\",\n" +
                                "    \"sessionId\": \"" + session.toString() + "\",\n" +
                                "    \"operationId\": \"40269145-d91f-4579-badd-c57fa1133239\"\n" +
                                "}", createHeaders(sessionId));

                // simulace informační zprávy
                Thread.sleep(2000);
                this.websocket.convertAndSendToUser(
                        sessionId, WebSocketConfiguration.MESSAGE_PREFIX + "/messages", "{\n" +
                                "    \"action\": \"DISPLAY_MESSAGE\",\n" +
                                "    \"sessionId\": \"" + session.toString() + "\",\n" +
                                "    \"messageType\": \"information\",\n" +
                                "    \"text\": \"Test OK message\"\n" +
                                "}", createHeaders(sessionId));

                // simulace chybové zprávy
                Thread.sleep(2000);
                this.websocket.convertAndSendToUser(
                        sessionId, WebSocketConfiguration.MESSAGE_PREFIX + "/messages", "{\n" +
                                "    \"action\": \"DISPLAY_MESSAGE\",\n" +
                                "    \"sessionId\": \"" + session.toString() + "\",\n" +
                                "    \"messageType\": \"error\",\n" +
                                "    \"text\": \"Test error message\"\n" +
                                "}", createHeaders(sessionId));

                // simulace ukončení session bez redirectu
                /*Thread.sleep(2000);
                this.websocket.convertAndSend(
                        MESSAGE_PREFIX + "/registration", "{\n" +
                                "    \"action\": \"TERMINATE\",\n" +
                                "    \"sessionId\": \"" + session.toString() + "\"\n" +
                                "}");*/

                // simulace ukončení session
                Thread.sleep(2000);
                this.websocket.convertAndSendToUser(
                        sessionId, WebSocketConfiguration.MESSAGE_PREFIX + "/registration", "{\n" +
                                "    \"action\": \"TERMINATE_REDIRECT\",\n" +
                                "    \"sessionId\": \"" + session.toString() + "\",\n" +
                                "    \"redirectUrl\": \"./\",\n" +
                                "    \"delay\": \"5\"\n" +
                                "}", createHeaders(sessionId));
                sessionRepository.delete(session);
            }
        }
    }

}
