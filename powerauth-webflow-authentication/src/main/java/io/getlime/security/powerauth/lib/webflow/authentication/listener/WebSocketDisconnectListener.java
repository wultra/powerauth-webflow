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

import io.getlime.core.rest.model.base.response.ObjectResponse;
import io.getlime.security.powerauth.lib.dataadapter.client.DataAdapterClient;
import io.getlime.security.powerauth.lib.dataadapter.client.DataAdapterClientErrorException;
import io.getlime.security.powerauth.lib.dataadapter.model.entity.FormData;
import io.getlime.security.powerauth.lib.dataadapter.model.entity.OperationChange;
import io.getlime.security.powerauth.lib.dataadapter.model.entity.OperationContext;
import io.getlime.security.powerauth.lib.nextstep.client.NextStepClient;
import io.getlime.security.powerauth.lib.nextstep.model.entity.OperationHistory;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthMethod;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthResult;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthStepResult;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.OperationCancelReason;
import io.getlime.security.powerauth.lib.nextstep.model.exception.NextStepServiceException;
import io.getlime.security.powerauth.lib.nextstep.model.response.GetOperationDetailResponse;
import io.getlime.security.powerauth.lib.webflow.authentication.model.converter.FormDataConverter;
import io.getlime.security.powerauth.lib.webflow.authentication.service.AuthMethodResolutionService;
import io.getlime.security.powerauth.lib.webflow.authentication.service.OperationSessionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.List;

/**
 * Listener for Web Socket disconnect events.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Component
public class WebSocketDisconnectListener implements ApplicationListener<SessionDisconnectEvent> {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketDisconnectListener.class);

    private final OperationSessionService operationSessionService;
    private final AuthMethodResolutionService authMethodResolutionService;
    private final NextStepClient nextStepClient;
    private final DataAdapterClient dataAdapterClient;

    /**
     * Constructor for Web Socket disconnect listener.
     * @param operationSessionService Operation to session mapping service.
     * @param authMethodResolutionService Authentication method resolution service.
     * @param nextStepClient Next Step client.
     * @param dataAdapterClient Data Adapter client.
     */
    @Autowired
    public WebSocketDisconnectListener(OperationSessionService operationSessionService, AuthMethodResolutionService authMethodResolutionService, NextStepClient nextStepClient, DataAdapterClient dataAdapterClient) {
        this.operationSessionService = operationSessionService;
        this.authMethodResolutionService = authMethodResolutionService;
        this.nextStepClient = nextStepClient;
        this.dataAdapterClient = dataAdapterClient;
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
        try {
            ObjectResponse<GetOperationDetailResponse> operationDetailResponse = nextStepClient.getOperationDetail(operationId);
            GetOperationDetailResponse operationDetail = operationDetailResponse.getResponseObject();
            if (operationDetail.getResult() == AuthResult.CONTINUE) {
                // Update operation result in operation to session mapping
                operationSessionService.updateOperationResult(operationId, AuthResult.FAILED);
                // Cancel operation due to interrupt by close browser event
                List<OperationHistory> operationHistory = operationDetail.getHistory();
                if (!operationHistory.isEmpty()) {
                    // Check whether authentication method is overridden, in this case use overridden authentication method
                    AuthMethod authMethod = authMethodResolutionService.resolveAuthMethodOverride(operationDetail);
                    if (authMethod == null) {
                        // Authentication method is not overridden, use last known authentication method
                        authMethod = operationHistory.get(operationHistory.size() - 1).getAuthMethod();
                    }
                    nextStepClient.updateOperation(operationDetail.getOperationId(), operationDetail.getUserId(), operationDetail.getOrganizationId(), authMethod, AuthStepResult.CANCELED, OperationCancelReason.INTERRUPTED_OPERATION.toString(), null, operationDetail.getApplicationContext());
                    // Notify Data Adapter about cancellation
                    FormData formData = new FormDataConverter().fromOperationFormData(operationDetail.getFormData());
                    OperationContext operationContext = new OperationContext(operationDetail.getOperationId(), operationDetail.getOperationName(), operationDetail.getOperationData(), formData, operationDetail.getApplicationContext());
                    dataAdapterClient.operationChangedNotification(OperationChange.CANCELED, operationDetail.getUserId(), operationDetail.getOrganizationId(), operationContext);
                } else {
                    logger.warn("Operation history is not available for operation ID {}", operationId);
                }
            }
        } catch (NextStepServiceException | DataAdapterClientErrorException ex) {
            logger.error(ex.getMessage(), ex);
        }
    }
}
