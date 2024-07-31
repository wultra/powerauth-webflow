/*
 * Copyright 2024 Wultra s.r.o.
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

/**
 * Interface for WebSocket messages.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public interface WebSocketMessageService {

    /**
     * Notification of clients about completed authorization.
     *
     * @param operationId Operation ID.
     * @param authResult Authorization result.
     */
    void notifyAuthorizationComplete(String operationId, AuthResult authResult);

    /**
     * Sends a message about successful websocket registration to the user.
     *
     * @param operationHash Operation hash.
     * @param sessionId Session ID.
     * @param registrationSucceeded Whether Web Socket registration was successful.
     */
    void sendRegistrationMessage(String operationHash, String sessionId, boolean registrationSucceeded);

    /**
     * Get Web Socket session ID for given operation hash.
     *
     * @param operationHash Operation hash.
     * @return Web Socket session ID.
     */
    String lookupWebSocketSessionId(String operationHash);

    /**
     * Store a mapping for new web socket identifier to the Web Socket session with given ID.
     *
     * @param operationHash Operation hash.
     * @param webSocketSessionId Web Socket Session ID.
     * @param clientIpAddress Remote client IP address.
     * @return Whether Web Socket registration was successful.
     */
    boolean registerWebSocketSession(String operationHash, String webSocketSessionId, String clientIpAddress);

}
