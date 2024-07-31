/*
 * Copyright 2017 Wultra s.r.o.
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
package io.getlime.security.powerauth.lib.webflow.authentication.service.websocket;

import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthResult;
import io.getlime.security.powerauth.lib.webflow.authentication.model.response.WebSocketAuthorizationResponse;
import io.getlime.security.powerauth.lib.webflow.authentication.model.response.WebSocketRegistrationResponse;
import io.getlime.security.powerauth.lib.webflow.authentication.service.OperationSessionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/**
 * Service that handles web socket to session pairing and notifying
 * clients about events.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
@Service
@Slf4j
@ConditionalOnProperty(name = "powerauth.webflow.websockets.enabled", havingValue = "true")
public class WebSocketMessageServiceImpl implements WebSocketMessageService {

    private final SimpMessagingTemplate websocket;
    private final OperationSessionService operationSessionService;

    /**
     * Service constructor.
     * @param websocket Web Socket simple messaging template.
     * @param operationSessionService Operation to session mapping service.
     */
    @Autowired
    public WebSocketMessageServiceImpl(SimpMessagingTemplate websocket, OperationSessionService operationSessionService) {
        this.websocket = websocket;
        this.operationSessionService = operationSessionService;
        logger.info("WebSocketMessageServiceImpl was initialized.");
    }

    /**
     * Create a MessageHeaders object for session.
     *
     * @param webSocketSessionId WebSocket session ID.
     * @return Message headers.
     */
    private MessageHeaders createHeaders(String webSocketSessionId) {
        SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.create(SimpMessageType.MESSAGE);
        headerAccessor.setSessionId(webSocketSessionId);
        headerAccessor.setLeaveMutable(true);
        return headerAccessor.getMessageHeaders();
    }

    @Override
    public void notifyAuthorizationComplete(String operationId, AuthResult authResult) {
        final String webSocketId = operationSessionService.generateOperationHash(operationId);
        final String sessionId = lookupWebSocketSessionId(webSocketId);
        WebSocketAuthorizationResponse authorizationResponse = new WebSocketAuthorizationResponse();
        authorizationResponse.setWebSocketId(webSocketId);
        authorizationResponse.setAuthResult(authResult);
        if (sessionId != null) {
            websocket.convertAndSendToUser(sessionId, "/topic/authorization", authorizationResponse, createHeaders(sessionId));
        }
    }

    @Override
    public void sendRegistrationMessage(String operationHash, String sessionId, boolean registrationSucceeded) {
        WebSocketRegistrationResponse registrationResponse = new WebSocketRegistrationResponse();
        registrationResponse.setWebSocketId(operationHash);
        registrationResponse.setRegistrationSucceeded(registrationSucceeded);
        websocket.convertAndSendToUser(sessionId, "/topic/registration", registrationResponse, createHeaders(sessionId));
    }

    @Override
    public String lookupWebSocketSessionId(String operationHash) {
        return operationSessionService.lookupWebSocketSessionIdByOperationHash(operationHash);
    }

    @Override
    public boolean registerWebSocketSession(String operationHash, String webSocketSessionId, String clientIpAddress) {
        return operationSessionService.registerWebSocketSession(operationHash, webSocketSessionId, clientIpAddress);
    }

}
