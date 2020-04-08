/*
 * Copyright 2019 Wultra s.r.o.
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
package io.getlime.security.powerauth.lib.webflow.authentication.listener;

import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthMethod;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.OperationCancelReason;
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
        operationCancellationService.cancelOperation(operationId, AuthMethod.INIT, OperationCancelReason.INTERRUPTED_OPERATION);
    }
}
