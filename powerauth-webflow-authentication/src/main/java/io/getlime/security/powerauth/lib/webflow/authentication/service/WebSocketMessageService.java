/*
 * Copyright 201ý Wultra s.r.o.
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
package io.getlime.security.powerauth.lib.webflow.authentication.service;

import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthResult;
import io.getlime.security.powerauth.lib.webflow.authentication.model.response.WebSocketAuthorizationResponse;
import io.getlime.security.powerauth.lib.webflow.authentication.model.response.WebSocketRegistrationResponse;
import org.springframework.beans.factory.annotation.Autowired;
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
public class WebSocketMessageService {

    private final SimpMessagingTemplate websocket;
    private final OperationSessionService operationSessionService;

    /**
     * Service constructor.
     * @param websocket Web Socket simple messaging template.
     * @param operationSessionService Operation to session mapping service.
     */
    @Autowired
    public WebSocketMessageService(SimpMessagingTemplate websocket, OperationSessionService operationSessionService) {
        this.websocket = websocket;
        this.operationSessionService = operationSessionService;
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

    /**
     * Notification of clients about completed authorization.
     *
     * @param operationId Operation ID.
     * @param authResult Authorization result.
     */
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

    /**
     * Sends a message about successful websocket registration to the user.
     *
     * @param operationHash Operation hash.
     * @param sessionId Session ID.
     * @param registrationSucceeded Whether Web Socket registration was successful.
     */
    public void sendRegistrationMessage(String operationHash, String sessionId, boolean registrationSucceeded) {
        WebSocketRegistrationResponse registrationResponse = new WebSocketRegistrationResponse();
        registrationResponse.setWebSocketId(operationHash);
        registrationResponse.setRegistrationSucceeded(registrationSucceeded);
        websocket.convertAndSendToUser(sessionId, "/topic/registration", registrationResponse, createHeaders(sessionId));
    }

    /**
     * Get Web Socket session ID for given operation hash.
     *
     * @param operationHash Operation hash.
     * @return Web Socket session ID.
     */
    public String lookupWebSocketSessionId(String operationHash) {
        return operationSessionService.lookupWebSocketSessionIdByOperationHash(operationHash);
    }

    /**
     * Store a mapping for new web socket identifier to the Web Socket session with given ID.
     *
     * @param operationHash Operation hash.
     * @param webSocketSessionId Web Socket Session ID.
     * @param clientIpAddress Remote client IP address.
     * @return Whether Web Socket registration was successful.
     */
    public boolean registerWebSocketSession(String operationHash, String webSocketSessionId, String clientIpAddress) {
        return operationSessionService.registerWebSocketSession(operationHash, webSocketSessionId, clientIpAddress);
    }

}
