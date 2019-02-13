package io.getlime.security.powerauth.lib.webflow.authentication.mtoken.service;

import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthResult;
import io.getlime.security.powerauth.lib.webflow.authentication.mtoken.model.response.WebSocketAuthorizationResponse;
import io.getlime.security.powerauth.lib.webflow.authentication.mtoken.model.response.WebSocketRegistrationResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import javax.xml.bind.DatatypeConverter;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Service that handles web socket to session pairing and notifying
 * clients about events.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
@Service
public class WebSocketMessageService {

    private final SimpMessagingTemplate websocket;
    private final ConcurrentMap<String, String> websocketIdToSessionMap;

    /**
     * Service constructor.
     * @param websocket Web Socket simple messaging template.
     */
    @Autowired
    public WebSocketMessageService(SimpMessagingTemplate websocket) {
        this.websocket = websocket;
        websocketIdToSessionMap = new ConcurrentHashMap<>();
    }

    /**
     * Create a MessageHeaders object for session.
     *
     * @param sessionId WebSocket session ID.
     * @return Message headers.
     */
    private MessageHeaders createHeaders(String sessionId) {
        SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.create(SimpMessageType.MESSAGE);
        headerAccessor.setSessionId(sessionId);
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
        final String webSocketId = generateWebSocketId(operationId);
        final String sessionId = getSessionId(webSocketId);
        WebSocketAuthorizationResponse authorizationResponse = new WebSocketAuthorizationResponse();
        authorizationResponse.setWebSocketId(webSocketId);
        authorizationResponse.setAuthResult(authResult);
        if (sessionId != null) {
            websocket.convertAndSendToUser(sessionId, "/topic/authorization", authorizationResponse, createHeaders(sessionId));
        }
    }

    /**
     * Generates a hash from operationId which is used as Web Socket ID.
     *
     * @param operationId Operation ID.
     * @return webSocketId Web Socket ID.
     */
    public String generateWebSocketId(String operationId) {
        try {
            return DatatypeConverter.printHexBinary(MessageDigest.getInstance("SHA-512").digest(operationId.getBytes("UTF-8")));
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            return null;
        }
    }

    /**
     * Sends a message about successful websocket registration to the user.
     *
     * @param webSocketId Web Socket ID.
     * @param sessionId Session ID.
     */
    public void sendRegistrationMessage(String webSocketId, String sessionId) {
        WebSocketRegistrationResponse registrationResponse = new WebSocketRegistrationResponse();
        registrationResponse.setWebSocketId(webSocketId);
        websocket.convertAndSendToUser(sessionId, "/topic/registration", registrationResponse, createHeaders(sessionId));
    }

    /**
     * Get session ID for given Web Socket ID.
     *
     * @param webSocketId Web socket ID
     * @return Session ID.
     */
    public String getSessionId(String webSocketId) {
        return websocketIdToSessionMap.get(webSocketId);
    }

    /**
     * Relate a new web socket identifier to the session with given ID.
     *
     * @param webSocketId Web Socket ID.
     * @param sessionId Session ID.
     */
    public void putWebSocketSession(String webSocketId, String sessionId) {
        websocketIdToSessionMap.put(webSocketId, sessionId);
    }

    /**
     * Removes WebSocket session identified by operationId from session tracking.
     *
     * @param operationId Operation ID.
     */
    public void removeWebSocketSession(String operationId) {
        websocketIdToSessionMap.remove(generateWebSocketId(operationId));
    }

}
