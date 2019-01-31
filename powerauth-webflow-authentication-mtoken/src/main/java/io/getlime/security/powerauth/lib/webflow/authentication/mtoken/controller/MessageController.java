/*
 * Copyright 2017 Wultra s.r.o.
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

import io.getlime.security.powerauth.lib.webflow.authentication.mtoken.model.request.WebSocketRegistrationRequest;
import io.getlime.security.powerauth.lib.webflow.authentication.mtoken.service.WebSocketMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

/**
 * Controller for Web Socket messages.
 *
 * @author Roman Strobl
 */
@Controller
public class MessageController {

    @Autowired
    private WebSocketMessageService webSocketMessageService;

    /**
     * Registration of WebSockets. WebSocket sessions are linked to operations for later authorization messages
     * to the clients. When authorization is complete, the removeWebSocketSession(String operationId) method should
     * be called to stop the tracking of given operation.
     *
     * @param headerAccessor Message headers.
     * @param registrationRequest Request for registration of a new WebSocket client.
     */
    @MessageMapping("/registration")
    public void register(SimpMessageHeaderAccessor headerAccessor, WebSocketRegistrationRequest registrationRequest) {
        String sessionId = headerAccessor.getSessionId();
        String webSocketId = registrationRequest.getWebSocketId();
        webSocketMessageService.putWebSocketSession(webSocketId, sessionId);
        webSocketMessageService.sendRegistrationMessage(webSocketId, sessionId);
    }

}