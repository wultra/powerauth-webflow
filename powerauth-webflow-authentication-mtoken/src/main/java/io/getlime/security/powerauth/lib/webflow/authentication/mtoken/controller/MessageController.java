/*
 * PowerAuth Web Flow and related software components
 * Copyright (C) 2017 Wultra s.r.o.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package io.getlime.security.powerauth.lib.webflow.authentication.mtoken.controller;

import io.getlime.security.powerauth.lib.webflow.authentication.model.request.WebSocketRegistrationRequest;
import io.getlime.security.powerauth.lib.webflow.authentication.service.WebSocketMessageService;
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

    private final WebSocketMessageService webSocketMessageService;

    @Autowired
    public MessageController(WebSocketMessageService webSocketMessageService) {
        this.webSocketMessageService = webSocketMessageService;
    }

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
        String clientIpAddress = (String) headerAccessor.getSessionAttributes().get("client_ip_address");
        boolean registrationSucceeded = webSocketMessageService.registerWebSocketSession(webSocketId, sessionId, clientIpAddress);
        webSocketMessageService.sendRegistrationMessage(webSocketId, sessionId, registrationSucceeded);
    }

}