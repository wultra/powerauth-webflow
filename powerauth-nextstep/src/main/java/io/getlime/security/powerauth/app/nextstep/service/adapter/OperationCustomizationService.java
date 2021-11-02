/*
 * Copyright 2021 Wultra s.r.o.
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
package io.getlime.security.powerauth.app.nextstep.service.adapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.getlime.security.powerauth.app.nextstep.converter.OperationConverter;
import io.getlime.security.powerauth.app.nextstep.repository.model.entity.OperationEntity;
import io.getlime.security.powerauth.app.nextstep.repository.model.entity.OperationHistoryEntity;
import io.getlime.security.powerauth.lib.dataadapter.client.DataAdapterClient;
import io.getlime.security.powerauth.lib.dataadapter.client.DataAdapterClientErrorException;
import io.getlime.security.powerauth.lib.dataadapter.model.entity.OperationChange;
import io.getlime.security.powerauth.lib.dataadapter.model.entity.OperationContext;
import io.getlime.security.powerauth.lib.dataadapter.model.enumeration.PowerAuthSignatureType;
import io.getlime.security.powerauth.lib.nextstep.model.entity.PAAuthenticationContext;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthStepResult;
import io.getlime.security.powerauth.lib.nextstep.model.response.GetOperationDetailResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This service handles operation customization.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Service
public class OperationCustomizationService {

    private final DataAdapterClient dataAdapterClient;

    private final Logger logger = LoggerFactory.getLogger(OperationCustomizationService.class);

    private final OperationConverter operationConverter = new OperationConverter();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Customization service for operations.
     *
     * @param dataAdapterClient Data Adapter client.
     */
    public OperationCustomizationService(DataAdapterClient dataAdapterClient) {
        this.dataAdapterClient = dataAdapterClient;
    }

    /**
     * Execute operation change notification in Data Adapter.
     * @param operation Operation entity.
     */
    public void notifyOperationChange(OperationEntity operation) {
        final GetOperationDetailResponse operationDetail = operationConverter.fromEntity(operation);
        final String userId = operationDetail.getUserId();
        final String organizationId = operationDetail.getOrganizationId();
        final OperationContext operationContext = operationConverter.toOperationContext(operation);
        final OperationChange operationChange;
        switch (operationDetail.getResult()) {
            case DONE:
                operationChange = OperationChange.DONE;
                break;
            case FAILED:
                if (operation.getCurrentOperationHistoryEntity() != null && operation.getCurrentOperationHistoryEntity().getRequestAuthStepResult() == AuthStepResult.CANCELED) {
                    operationChange = OperationChange.CANCELED;
                    break;
                }
                operationChange = OperationChange.FAILED;
                break;
            default:
                // Notification is not sent when authResult is CONTINUE
                return;
        }
        List<OperationHistoryEntity> operationHistory = new ArrayList<>(operation.getOperationHistory());
        Collections.reverse(operationHistory);
        io.getlime.security.powerauth.lib.dataadapter.model.entity.PAAuthenticationContext authenticationContextDA = null;
        // Find the last PowerAuth authentication context
        for (OperationHistoryEntity history: operationHistory) {
            if (history.getPowerAuthAuthenticationContext() != null) {
                String authContext = history.getPowerAuthAuthenticationContext();
                try {
                    PAAuthenticationContext authenticationContextNS = objectMapper.readValue(authContext, PAAuthenticationContext.class);
                    authenticationContextDA = new io.getlime.security.powerauth.lib.dataadapter.model.entity.PAAuthenticationContext();
                    authenticationContextDA.setSignatureType(PowerAuthSignatureType.getEnumFromString(authenticationContextNS.getSignatureType()));
                    authenticationContextDA.setRemainingAttempts(authenticationContextNS.getRemainingAttempts());
                    authenticationContextDA.setBlocked(authenticationContextNS.isBlocked());
                    break;
                } catch (IOException ex) {
                    logger.error("Error while deserializing authentication context", ex);
                    // Invalid authentication context, do not continue searching
                    break;
                }
            }
        }
        if (authenticationContextDA == null) {
            // PowerAuth authentication context is not present (e.g. operation was canceled before first PowerAuth authentication),
            authenticationContextDA = new io.getlime.security.powerauth.lib.dataadapter.model.entity.PAAuthenticationContext();
            authenticationContextDA.setBlocked(false);
            authenticationContextDA.setSignatureType(null);
            authenticationContextDA.setRemainingAttempts(null);
        }
        operationContext.setAuthenticationContext(authenticationContextDA);
        try {
            dataAdapterClient.operationChangedNotification(operationChange, userId, organizationId, operationContext);
        } catch (DataAdapterClientErrorException ex) {
            logger.warn(ex.getMessage(), ex);
        }
    }

}
