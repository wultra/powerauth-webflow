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

import io.getlime.security.powerauth.app.nextstep.converter.OperationConverter;
import io.getlime.security.powerauth.app.nextstep.repository.model.entity.OperationEntity;
import io.getlime.security.powerauth.lib.dataadapter.client.DataAdapterClient;
import io.getlime.security.powerauth.lib.dataadapter.client.DataAdapterClientErrorException;
import io.getlime.security.powerauth.lib.dataadapter.model.entity.OperationChange;
import io.getlime.security.powerauth.lib.dataadapter.model.entity.OperationContext;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthStepResult;
import io.getlime.security.powerauth.lib.nextstep.model.response.GetOperationDetailResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

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
        GetOperationDetailResponse operationDetail = operationConverter.fromEntity(operation);
        String userId = operationDetail.getUserId();
        String organizationId = operationDetail.getOrganizationId();
        OperationContext operationContext = operationConverter.toOperationContext(operation);
        OperationChange operationChange;
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
        try {
            dataAdapterClient.operationChangedNotification(operationChange, userId, organizationId, operationContext);
        } catch (DataAdapterClientErrorException ex) {
            logger.warn(ex.getMessage(), ex);
        }
    }

}
