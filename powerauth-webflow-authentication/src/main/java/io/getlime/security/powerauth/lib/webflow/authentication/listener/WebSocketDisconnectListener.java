/*
 * PowerAuth Web Flow and related software components
 * Copyright (C) 2019 Wultra s.r.o.
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
package io.getlime.security.powerauth.lib.webflow.authentication.listener;

import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthMethod;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.OperationCancelReason;
import io.getlime.security.powerauth.lib.webflow.authentication.exception.CommunicationFailedException;
import io.getlime.security.powerauth.lib.webflow.authentication.service.OperationCancellationService;
import io.getlime.security.powerauth.lib.webflow.authentication.service.OperationSessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

/**
 * Listener for Web Socket disconnect events.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Component
public class WebSocketDisconnectListener implements ApplicationListener<SessionDisconnectEvent> {

    private final OperationSessionService operationSessionService;
    private final OperationCancellationService operationCancellationService;

    /**
     * Constructor for Web Socket disconnect listener.
     * @param operationSessionService Operation to session mapping service.
     * @param operationCancellationService Service used for cancelling operations.
     */
    @Autowired
    public WebSocketDisconnectListener(OperationSessionService operationSessionService, OperationCancellationService operationCancellationService) {
        this.operationSessionService = operationSessionService;
        this.operationCancellationService = operationCancellationService;
    }

    /**
     * In case user closes web browser a session disconnect event is processed. Check the operation result and cancel it if necessary.
     * @param sessionDisconnectEvent Session disconnect event.
     */
    @Override
    public void onApplicationEvent(SessionDisconnectEvent sessionDisconnectEvent) {
        String sessionId = sessionDisconnectEvent.getSessionId();
        String operationId = operationSessionService.lookupOperationIdByWebSocketSessionId(sessionId);
        if (operationId == null) {
            // Operation does not exist, nothing to do
            return;
        }
        // Cancel operation due to interrupted operation
        try {
            operationCancellationService.cancelOperation(operationId, AuthMethod.INIT, OperationCancelReason.INTERRUPTED_OPERATION, true);
        } catch (CommunicationFailedException ex) {
            // Exception is already logged
        }
    }
}
