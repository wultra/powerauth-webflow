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
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * Dummy WebSocket service.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Service
@Slf4j
@ConditionalOnProperty(name = "powerauth.webflow.websockets.enabled", havingValue = "false", matchIfMissing = true)
public class WebSocketMessageServiceDummy implements WebSocketMessageService {

    {
        logger.info("WebSocketMessageServiceDummy was initialized.");
    }

    @Override
    public void notifyAuthorizationComplete(String operationId, AuthResult authResult) {
    }

    @Override
    public void sendRegistrationMessage(String operationHash, String sessionId, boolean registrationSucceeded) {
    }

    @Override
    public String lookupWebSocketSessionId(String operationHash) {
        return null;
    }

    @Override
    public boolean registerWebSocketSession(String operationHash, String webSocketSessionId, String clientIpAddress) {
        return false;
    }

}
