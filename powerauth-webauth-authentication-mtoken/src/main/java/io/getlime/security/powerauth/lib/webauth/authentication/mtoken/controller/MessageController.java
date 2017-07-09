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
package io.getlime.security.powerauth.lib.webauth.authentication.mtoken.controller;

import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthResult;
import io.getlime.security.powerauth.lib.webauth.authentication.mtoken.model.request.WebSocketRegistrationRequest;
import io.getlime.security.powerauth.lib.webauth.authentication.mtoken.model.response.WebSocketAuthorizationResponse;
import io.getlime.security.powerauth.lib.webauth.authentication.mtoken.model.response.WebSocketRegistrationResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import javax.xml.bind.DatatypeConverter;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

/**
 * Controller for WebSocket messages.
 *
 * @author Roman Strobl
 */
@Controller
public class MessageController {

    private final SimpMessagingTemplate websocket;
    private final Map<String, String> websocketIdToSessionMap;

    @Autowired
    public MessageController(SimpMessagingTemplate websocket) {
        this.websocket = websocket;
        websocketIdToSessionMap = new HashMap<>();
    }

    /**
     * Create a MessageHeaders object for session.
     *
     * @param sessionId WebSocket session ID
     * @return MessageHeaders
     */
    private MessageHeaders createHeaders(String sessionId) {
        SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.create(SimpMessageType.MESSAGE);
        headerAccessor.setSessionId(sessionId);
        headerAccessor.setLeaveMutable(true);
        return headerAccessor.getMessageHeaders();
    }

    /**
     * Registration of WebSockets. WebSocket sessions are linked to operations for later authorization messages
     * to the clients. When authorization is complete, the removeWebSocketSession(String operationId) method should
     * be called to stop the tracking of given operation.
     *
     * @param headerAccessor      message headers
     * @param registrationRequest request for registration of a new WebSocket client
     */
    @MessageMapping("/registration")
    public void register(SimpMessageHeaderAccessor headerAccessor, WebSocketRegistrationRequest registrationRequest) {
        String sessionId = headerAccessor.getSessionId();
        websocketIdToSessionMap.put(registrationRequest.getWebSocketId(), sessionId);
        WebSocketRegistrationResponse registrationResponse = new WebSocketRegistrationResponse();
        registrationResponse.setWebSocketId(registrationRequest.getWebSocketId());
        websocket.convertAndSendToUser(
                sessionId, "/topic/registration", registrationResponse, createHeaders(sessionId));
    }

    /**
     * Notification of clients about completed authorization.
     *
     * @param operationId operation ID
     * @param authResult  authorization result
     */
    public void notifyAuthorizationComplete(String operationId, AuthResult authResult) {
        final String webSocketId = generateWebSocketId(operationId);
        final String sessionId = websocketIdToSessionMap.get(webSocketId);
        WebSocketAuthorizationResponse authorizationResponse = new WebSocketAuthorizationResponse();
        authorizationResponse.setWebSocketId(webSocketId);
        authorizationResponse.setAuthResult(authResult);
        if (sessionId != null) {
            websocket.convertAndSendToUser(
                    sessionId, "/topic/authorization", authorizationResponse, createHeaders(sessionId));
        }
    }

    /**
     * Generates a hash from operationId which is used as webSocketId.
     *
     * @param operationId operation ID
     * @return webSocketId
     */
    public String generateWebSocketId(String operationId) {
        try {
            return DatatypeConverter.printHexBinary(
                    MessageDigest.getInstance("SHA-512").digest(operationId.getBytes("UTF-8")));
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            return null;
        }
    }

    /**
     * Removes WebSocket session identified by operationId from session tracking.
     *
     * @param operationId operation ID
     */
    public void removeWebSocketSession(String operationId) {
        websocketIdToSessionMap.remove(generateWebSocketId(operationId));
    }

}