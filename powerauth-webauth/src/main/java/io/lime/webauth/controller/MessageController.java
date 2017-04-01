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
package io.lime.webauth.controller;

import io.lime.webauth.model.entity.RegistrationMessage;
import io.lime.webauth.repository.SessionRepository;
import io.lime.webauth.repository.model.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import static io.lime.webauth.configuration.WebSocketConfiguration.MESSAGE_PREFIX;

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


    @MessageMapping("/registration")
    public void register(RegistrationMessage message) throws Exception {
        System.out.println("Received registration message: " + message);
        if ("REGISTER".equals(message.getAction())) {
            Session session = new Session();
            sessionRepository.save(session);
            this.websocket.convertAndSend(
                    MESSAGE_PREFIX + "/registration", "{\n" +
                            "    \"action\": \"REGISTRATION_CONFIRM\",\n" +
                            "    \"sessionId\": \"" + session.toString() + "\"\n" +
                            "}");

            if (message.getPerformUITest()) {
                // simulace redirectu po odpovědi od CBDS
                Thread.sleep(1000);
                this.websocket.convertAndSend(
                        MESSAGE_PREFIX + "/authentication", "{\n" +
                                "    \"action\": \"DISPLAY_LOGIN_FORM\",\n" +
                                "    \"sessionId\": \"" + session.toString() + "\"\n" +
                                "}");

                // simulace zobrazení payment info
                Thread.sleep(2000);
                this.websocket.convertAndSend(
                        MESSAGE_PREFIX + "/authorization", "{\n" +
                                "    \"action\": \"DISPLAY_PAYMENT_INFO\",\n" +
                                "    \"sessionId\": \"" + session.toString() + "\",\n" +
                                "    \"operationId\": \"40269145-d91f-4579-badd-c57fa1133239\",\n" +
                                "    \"amount\": \"103\",\n" +
                                "    \"currency\": \"CZK\"\n" +
                                "}");

                // simulace zobrazení autorizace
                Thread.sleep(2000);
                this.websocket.convertAndSend(
                        MESSAGE_PREFIX + "/authorization", "{\n" +
                                "    \"action\": \"DISPLAY_PAYMENT_AUTHORIZATION_FROM\",\n" +
                                "    \"sessionId\": \"" + session.toString() + "\",\n" +
                                "    \"operationId\": \"40269145-d91f-4579-badd-c57fa1133239\"\n" +
                                "}");

                // simulace informační zprávy
                Thread.sleep(2000);
                this.websocket.convertAndSend(
                        MESSAGE_PREFIX + "/messages", "{\n" +
                                "    \"action\": \"DISPLAY_MESSAGE\",\n" +
                                "    \"sessionId\": \"" + session.toString() + "\",\n" +
                                "    \"messageType\": \"information\",\n" +
                                "    \"text\": \"Test OK message\"\n" +
                                "}");

                // simulace chybové zprávy
                Thread.sleep(2000);
                this.websocket.convertAndSend(
                        MESSAGE_PREFIX + "/messages", "{\n" +
                                "    \"action\": \"DISPLAY_MESSAGE\",\n" +
                                "    \"sessionId\": \"" + session.toString() + "\",\n" +
                                "    \"messageType\": \"error\",\n" +
                                "    \"text\": \"Test error message\"\n" +
                                "}");

                // simulace ukončení session bez redirectu
                /*Thread.sleep(2000);
                this.websocket.convertAndSend(
                        MESSAGE_PREFIX + "/registration", "{\n" +
                                "    \"action\": \"TERMINATE\",\n" +
                                "    \"sessionId\": \"" + session.toString() + "\"\n" +
                                "}");*/

                // simulace ukončení session
                Thread.sleep(2000);
                this.websocket.convertAndSend(
                        MESSAGE_PREFIX + "/registration", "{\n" +
                                "    \"action\": \"TERMINATE_REDIRECT\",\n" +
                                "    \"sessionId\": \"" + session.toString() + "\",\n" +
                                "    \"redirectUrl\": \"http://localhost:8080\",\n" +
                                "    \"delay\": \"5\"\n" +
                                "}");
                sessionRepository.delete(session);
            }
        }
    }

}
