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
package io.getlime.security.powerauth.lib.webflow.authentication.service;

import io.getlime.core.rest.model.base.response.ObjectResponse;
import io.getlime.security.powerauth.lib.dataadapter.model.enumeration.OperationTerminationReason;
import io.getlime.security.powerauth.lib.nextstep.client.NextStepClient;
import io.getlime.security.powerauth.lib.nextstep.client.NextStepClientException;
import io.getlime.security.powerauth.lib.nextstep.model.entity.ApplicationContext;
import io.getlime.security.powerauth.lib.nextstep.model.entity.OperationHistory;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthMethod;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthResult;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthStepResult;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.OperationCancelReason;
import io.getlime.security.powerauth.lib.nextstep.model.response.GetOperationDetailResponse;
import io.getlime.security.powerauth.lib.nextstep.model.response.UpdateOperationResponse;
import io.getlime.security.powerauth.lib.webflow.authentication.exception.CommunicationFailedException;
import io.getlime.security.powerauth.lib.webflow.authentication.model.converter.OperationCancellationConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * Service which centralizes logic for cancellation of operations.
 * @author Roman Strobl, roman.strob@wultra.com
 */
@Service
public class OperationCancellationService {

    private static final Logger logger = LoggerFactory.getLogger(OperationCancellationService.class);

    private final NextStepClient nextStepClient;
    private final AfsIntegrationService afsIntegrationService;
    private final PowerAuthOperationService powerAuthOperationService;
    private final OperationCancellationConverter operationCancellationConverter = new OperationCancellationConverter();

    /**
     * Service constructor.
     * @param nextStepClient Next Step client.
     * @param afsIntegrationService AFS integration service.
     * @param powerAuthOperationService PowerAuth operation service.
     */
    public OperationCancellationService(NextStepClient nextStepClient, AfsIntegrationService afsIntegrationService, PowerAuthOperationService powerAuthOperationService) {
        this.nextStepClient = nextStepClient;
        this.afsIntegrationService = afsIntegrationService;
        this.powerAuthOperationService = powerAuthOperationService;
    }

    /**
     * Cancel operation with given operation ID in case it is still active.
     * @param operationId Operation ID.
     * @param authMethod Authentication method which triggered operation cancellation.
     * @param cancelReason Reason for canceling the operation.
     * @return Update operation response or null in case cancellation was skipped.
     * @throws CommunicationFailedException Thrown in case communication with remote system fails.
     */
    public UpdateOperationResponse cancelOperation(String operationId, AuthMethod authMethod, OperationCancelReason cancelReason, boolean cancelPowerAuthOperation) throws CommunicationFailedException {
        try {
            final ObjectResponse<GetOperationDetailResponse> operationResponse = nextStepClient.getOperationDetail(operationId);
            final GetOperationDetailResponse operationDetail = operationResponse.getResponseObject();
            return cancelOperation(operationDetail, authMethod, cancelReason, cancelPowerAuthOperation);
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
     * @throws CommunicationFailedException Thrown in case communication with remote system fails.
     */
    public UpdateOperationResponse cancelOperation(GetOperationDetailResponse operationDetail, AuthMethod authMethod, OperationCancelReason cancelReason, boolean cancelPowerAuthOperation) throws CommunicationFailedException {
        try {
            boolean paCancelSucceeded = true;
            if (cancelPowerAuthOperation) {
                // In case a PowerAuth operation is available, cancel it
                List<OperationHistory> history = operationDetail.getHistory();
                if (!history.isEmpty()) {
                    OperationHistory h = history.get(history.size() - 1);
                    if (h.isMobileTokenActive() && h.getPowerAuthOperationId() != null) {
                        paCancelSucceeded = powerAuthOperationService.cancelOperation(operationDetail);
                    }
                }
            }
            // Cancel operation only in case it is still active
            if (operationDetail.getResult() == AuthResult.CONTINUE) {
                // Notify AFS about logout event
                OperationTerminationReason terminationReason = operationCancellationConverter.convertCancelReason(cancelReason);
                afsIntegrationService.executeLogoutAction(operationDetail.getOperationId(), terminationReason);
                ObjectResponse<UpdateOperationResponse> updateOperationResponse;
                // Avoid canceling operation when it already failed due to PowerAuth operation cancellation error
                if (!paCancelSucceeded) {
                    return null;
                }
                final ApplicationContext applicationContext = operationDetail.getApplicationContext();
                updateOperationResponse = nextStepClient.updateOperation(operationDetail.getOperationId(), operationDetail.getUserId(), operationDetail.getOrganizationId(), authMethod, Collections.emptyList(), AuthStepResult.CANCELED, cancelReason.toString(), null, applicationContext, null);
                return updateOperationResponse.getResponseObject();
            }
        } catch (NextStepClientException ex) {
            logger.error("Error occurred while canceling operation", ex);
            throw new CommunicationFailedException("Communication failed while canceling operation");
        }
        return null;
    }

}