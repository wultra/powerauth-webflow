/*
 * PowerAuth Web Flow and related software components
 * Copyright (C) 2021 Wultra s.r.o.
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
import org.springframework.beans.factory.annotation.Autowired;
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
    private final OperationConverter operationConverter;
    private final ObjectMapper objectMapper;

    private final Logger logger = LoggerFactory.getLogger(OperationCustomizationService.class);

    /**
     * Customization service for operations.
     * @param dataAdapterClient Data Adapter client.
     * @param operationConverter Operation converter.
     * @param objectMapper Object mapper.
     */
    @Autowired
    public OperationCustomizationService(DataAdapterClient dataAdapterClient, OperationConverter operationConverter, ObjectMapper objectMapper) {
        this.dataAdapterClient = dataAdapterClient;
        this.operationConverter = operationConverter;
        this.objectMapper = objectMapper;
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
