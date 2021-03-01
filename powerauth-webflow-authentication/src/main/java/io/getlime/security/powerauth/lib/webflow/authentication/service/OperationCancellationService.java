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
package io.getlime.security.powerauth.lib.webflow.authentication.service;

import io.getlime.core.rest.model.base.response.ObjectResponse;
import io.getlime.security.powerauth.lib.dataadapter.client.DataAdapterClient;
import io.getlime.security.powerauth.lib.dataadapter.client.DataAdapterClientErrorException;
import io.getlime.security.powerauth.lib.dataadapter.model.entity.FormData;
import io.getlime.security.powerauth.lib.dataadapter.model.entity.OperationChange;
import io.getlime.security.powerauth.lib.dataadapter.model.entity.OperationContext;
import io.getlime.security.powerauth.lib.dataadapter.model.enumeration.OperationTerminationReason;
import io.getlime.security.powerauth.lib.nextstep.client.NextStepClient;
import io.getlime.security.powerauth.lib.nextstep.client.NextStepClientException;
import io.getlime.security.powerauth.lib.nextstep.model.entity.ApplicationContext;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthMethod;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthResult;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthStepResult;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.OperationCancelReason;
import io.getlime.security.powerauth.lib.nextstep.model.response.GetOperationDetailResponse;
import io.getlime.security.powerauth.lib.nextstep.model.response.UpdateOperationResponse;
import io.getlime.security.powerauth.lib.webflow.authentication.exception.CommunicationFailedException;
import io.getlime.security.powerauth.lib.webflow.authentication.model.converter.FormDataConverter;
import io.getlime.security.powerauth.lib.webflow.authentication.model.converter.OperationCancellationConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * Service which centralizes logic for cancellation of operations.
 * @author Roman Strobl, roman.strob@wultra.com
 */
@Service
public class OperationCancellationService {

    private static final Logger logger = LoggerFactory.getLogger(OperationCancellationService.class);

    private final NextStepClient nextStepClient;
    private final DataAdapterClient dataAdapterClient;
    private final AfsIntegrationService afsIntegrationService;
    private final OperationCancellationConverter operationCancellationConverter = new OperationCancellationConverter();

    /**
     * Service constructor.
     * @param nextStepClient Next Step client.
     * @param dataAdapterClient Data Adapter client.
     * @param afsIntegrationService AFS integration service.
     */
    public OperationCancellationService(NextStepClient nextStepClient, DataAdapterClient dataAdapterClient, AfsIntegrationService afsIntegrationService) {
        this.nextStepClient = nextStepClient;
        this.dataAdapterClient = dataAdapterClient;
        this.afsIntegrationService = afsIntegrationService;
    }

    /**
     * Cancel operation with given operation ID in case it is still active.
     * @param operationId Operation ID.
     * @param authMethod Authentication method which triggered operation cancellation.
     * @param cancelReason Reason for canceling the operation.
     * @return Update operation response or null in case cancellation was skipped.
     */
    public UpdateOperationResponse cancelOperation(String operationId, AuthMethod authMethod, OperationCancelReason cancelReason) throws CommunicationFailedException {
        try {
            final ObjectResponse<GetOperationDetailResponse> operationResponse = nextStepClient.getOperationDetail(operationId);
            final GetOperationDetailResponse operationDetail = operationResponse.getResponseObject();
            return cancelOperation(operationDetail, authMethod, cancelReason);
        } catch (NextStepClientException ex) {
            logger.error("Error occurred while canceling operation", ex);
            throw new CommunicationFailedException("Communication failed while canceling operation");
        }
    }

    /**
     * Cancel given operation in case it is still active.
     * @param operationDetail Operation.
     * @param authMethod Authentication method which triggered operation cancellation.
     * @param cancelReason Reason for canceling the operation.
     * @return Update operation response or null in case cancellation was skipped.
     */
    public UpdateOperationResponse cancelOperation(GetOperationDetailResponse operationDetail, AuthMethod authMethod, OperationCancelReason cancelReason) throws CommunicationFailedException {
        try {
            // Cancel operation only in case it is still active
            if (operationDetail.getResult() == AuthResult.CONTINUE) {
                final ApplicationContext applicationContext = operationDetail.getApplicationContext();
                ObjectResponse<UpdateOperationResponse> updateOperationResponse = nextStepClient.updateOperation(operationDetail.getOperationId(), operationDetail.getUserId(), operationDetail.getOrganizationId(), authMethod, Collections.emptyList(), AuthStepResult.CANCELED, cancelReason.toString(), null, applicationContext);
                // Notify Data Adapter about cancellation event
                FormData formData = new FormDataConverter().fromOperationFormData(operationDetail.getFormData());
                OperationContext operationContext = new OperationContext(operationDetail.getOperationId(), operationDetail.getOperationName(), operationDetail.getOperationData(), operationDetail.getExternalTransactionId(), formData, applicationContext);
                dataAdapterClient.operationChangedNotification(OperationChange.CANCELED, operationDetail.getUserId(), operationDetail.getOrganizationId(), operationContext);
                // Notify AFS about logout event
                OperationTerminationReason terminationReason = operationCancellationConverter.convertCancelReason(cancelReason);
                afsIntegrationService.executeLogoutAction(operationDetail.getOperationId(), terminationReason);
                return updateOperationResponse.getResponseObject();
            }
        } catch (NextStepClientException | DataAdapterClientErrorException ex) {
            logger.error("Error occurred while canceling operation", ex);
            throw new CommunicationFailedException("Communication failed while canceling operation");
        }
        return null;
    }

}