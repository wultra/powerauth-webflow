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
package io.getlime.security.powerauth.lib.webflow.authentication.service;

import com.wultra.security.powerauth.client.PowerAuthClient;
import com.wultra.security.powerauth.client.model.enumeration.OperationStatus;
import com.wultra.security.powerauth.client.model.enumeration.SignatureType;
import com.wultra.security.powerauth.client.model.enumeration.UserActionResult;
import com.wultra.security.powerauth.client.model.error.PowerAuthClientException;
import com.wultra.security.powerauth.client.model.request.*;
import com.wultra.security.powerauth.client.model.response.OperationDetailResponse;
import com.wultra.security.powerauth.client.model.response.OperationUserActionResponse;
import com.wultra.security.powerauth.client.v3.ActivationStatus;
import com.wultra.security.powerauth.client.v3.GetActivationStatusResponse;
import io.getlime.security.powerauth.lib.nextstep.client.NextStepClient;
import io.getlime.security.powerauth.lib.nextstep.client.NextStepClientException;
import io.getlime.security.powerauth.lib.nextstep.model.entity.OperationHistory;
import io.getlime.security.powerauth.lib.nextstep.model.entity.PAAuthenticationContext;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthInstrument;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthMethod;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthStepResult;
import io.getlime.security.powerauth.lib.nextstep.model.response.GetOperationDetailResponse;
import io.getlime.security.powerauth.lib.webflow.authentication.configuration.WebFlowServicesConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * Service for managing operations in PowerAuth server.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Service
public class PowerAuthOperationService {

    private static final Logger logger = LoggerFactory.getLogger(PowerAuthOperationService.class);

    private final WebFlowServicesConfiguration configuration;
    private final PowerAuthClient powerAuthClient;
    private final NextStepClient nextStepClient;
    private final AuthMethodResolutionService authMethodResolutionService;

    /**
     * Service constructor.
     * @param configuration Web Flow configuration.
     * @param powerAuthClient PowerAuth client.
     * @param nextStepClient Next Step client.
     * @param authMethodResolutionService Authentication method resolution service.
     */
    @Autowired
    public PowerAuthOperationService(WebFlowServicesConfiguration configuration, PowerAuthClient powerAuthClient, NextStepClient nextStepClient, AuthMethodResolutionService authMethodResolutionService) {
        this.configuration = configuration;
        this.powerAuthClient = powerAuthClient;
        this.nextStepClient = nextStepClient;
        this.authMethodResolutionService = authMethodResolutionService;
    }

    /**
     * Approve a PowerAuth operation.
     * @param operation Operation detail.
     * @param activationId Activation ID.
     * @param signatureType Used signature type.
     * @return Whether approval succeeded.
     */
    public boolean approveOperation(GetOperationDetailResponse operation, String activationId, SignatureType signatureType) {
        boolean operationEnabled = configuration.isPowerAuthOperationSupportEnabled();
        if (!operationEnabled) {
            return true;
        }
        List<OperationHistory> history = operation.getHistory();
        if (history.isEmpty()) {
            return false;
        }
        OperationHistory currentHistory = history.get(history.size() - 1);
        boolean mobileTokenActive = currentHistory.isMobileTokenActive();
        String paOperationId = currentHistory.getPowerAuthOperationId();
        if (!mobileTokenActive || paOperationId == null) {
            return true;
        }

        try {
            // Check activation status
            boolean approvalFailed = false;
            GetActivationStatusResponse status = powerAuthClient.getActivationStatus(activationId);
            if (status.getActivationStatus() != ActivationStatus.ACTIVE) {
                approvalFailed = true;
            }

            OperationApproveRequest request = new OperationApproveRequest();
            request.setOperationId(paOperationId);
            request.setUserId(operation.getUserId());
            request.setApplicationId(status.getApplicationId());
            request.setData(operation.getOperationData());
            request.setSignatureType(signatureType);

            // Approve operation in PowerAuth server
            OperationUserActionResponse paResponse = powerAuthClient.operationApprove(request);

            // Check result
            if (paResponse.getResult() != UserActionResult.APPROVED) {
                approvalFailed = true;
            }
            if (approvalFailed) {
                // Fail Next Step operation because approval failed
                failNextStepOperation(operation);
            } else {
                return true;
            }
        } catch (PowerAuthClientException ex) {
            logger.warn(ex.getMessage(), ex);
            failNextStepOperation(operation);
        }
        return false;
    }

    /**
     * Fail an approval of a PowerAuth operation.
     * @param operation Operation detail.
     * @return Whether approval fail succeeded.
     */
    public boolean failApprovalForOperation(GetOperationDetailResponse operation) {
        boolean operationEnabled = configuration.isPowerAuthOperationSupportEnabled();
        if (!operationEnabled) {
            return true;
        }
        List<OperationHistory> history = operation.getHistory();
        if (history.isEmpty()) {
            return false;
        }
        OperationHistory currentHistory = history.get(history.size() - 1);
        boolean mobileTokenActive = currentHistory.isMobileTokenActive();
        String paOperationId = currentHistory.getPowerAuthOperationId();
        if (!mobileTokenActive || paOperationId == null) {
            return true;
        }

        try {
            OperationFailApprovalRequest request = new OperationFailApprovalRequest();
            request.setOperationId(paOperationId);

            // Fail approval for operation in PowerAuth server
            OperationUserActionResponse paResponse = powerAuthClient.failApprovalOperation(request);
            if (paResponse.getResult() == UserActionResult.APPROVAL_FAILED) {
                // One attempt failed successfully, operation is still pending
                return true;
            }
            // In case PowerAuth operation response is not APPROVAL_FAILED, fail Next Step operation
            failNextStepOperation(operation);
        } catch (PowerAuthClientException ex) {
            logger.warn(ex.getMessage(), ex);
            failNextStepOperation(operation);
        }
        return false;
    }

    /**
     * Reject a PowerAuth operation.
     * @param operation Operation detail.
     * @param activationId Activation ID.
     * @return Whether reject succeeded.
     */
    public boolean rejectOperation(GetOperationDetailResponse operation, String activationId) {
        boolean operationEnabled = configuration.isPowerAuthOperationSupportEnabled();
        if (!operationEnabled) {
            return true;
        }
        List<OperationHistory> history = operation.getHistory();
        if (history.isEmpty()) {
            return false;
        }
        OperationHistory currentHistory = history.get(history.size() - 1);
        boolean mobileTokenActive = currentHistory.isMobileTokenActive();
        String paOperationId = currentHistory.getPowerAuthOperationId();
        if (!mobileTokenActive || paOperationId == null) {
            return true;
        }

        try {
            // Check activation status
            boolean rejectFailed = false;
            GetActivationStatusResponse status = powerAuthClient.getActivationStatus(activationId);
            if (status.getActivationStatus() != ActivationStatus.ACTIVE) {
                rejectFailed = true;
            }

            OperationRejectRequest request = new OperationRejectRequest();
            request.setOperationId(paOperationId);
            request.setUserId(operation.getUserId());
            request.setApplicationId(status.getApplicationId());

            // Reject operation in PowerAuth server
            OperationUserActionResponse paResponse = powerAuthClient.operationReject(request);
            if (paResponse.getResult() != UserActionResult.REJECTED) {
                rejectFailed = true;
            }
            if (rejectFailed) {
                // Fail Next Step operation because reject failed
                failNextStepOperation(operation);
            } else {
                return true;
            }
        } catch (PowerAuthClientException ex) {
            logger.warn(ex.getMessage(), ex);
            failNextStepOperation(operation);
        }
        return false;
    }

    /**
     * Cancel a PowerAuth operation.
     * @param operation Operation detail.
     * @return Whether cancellation succeeded.
     */
    public boolean cancelOperation(GetOperationDetailResponse operation) {
        boolean operationEnabled = configuration.isPowerAuthOperationSupportEnabled();
        if (!operationEnabled) {
            return true;
        }
        List<OperationHistory> history = operation.getHistory();
        if (history.isEmpty()) {
            return false;
        }
        OperationHistory currentHistory = history.get(history.size() - 1);
        boolean mobileTokenActive = currentHistory.isMobileTokenActive();
        String paOperationId = currentHistory.getPowerAuthOperationId();
        if (!mobileTokenActive || paOperationId == null) {
            return true;
        }

        try {
            OperationCancelRequest request = new OperationCancelRequest();
            request.setOperationId(paOperationId);

            // Cancel operation in PowerAuth server
            OperationDetailResponse paResponse = powerAuthClient.operationCancel(request);
            if (paResponse.getStatus() == OperationStatus.CANCELED) {
                return true;
            }
            // Fail Next Step operation because cancellation failed
            failNextStepOperation(operation);
        } catch (PowerAuthClientException ex) {
            logger.warn(ex.getMessage(), ex);
            failNextStepOperation(operation);
        }
        return false;
    }

    /**
     * Get PowerAuth operation status.
     * @param operation Operation detail.
     * @return PowerAuth operation status.
     */
    public OperationStatus getOperationStatus(GetOperationDetailResponse operation) {
        boolean operationEnabled = configuration.isPowerAuthOperationSupportEnabled();
        if (!operationEnabled) {
            return null;
        }
        List<OperationHistory> history = operation.getHistory();
        if (history.isEmpty()) {
            return null;
        }
        OperationHistory currentHistory = history.get(history.size() - 1);
        boolean mobileTokenActive = currentHistory.isMobileTokenActive();
        String paOperationId = currentHistory.getPowerAuthOperationId();
        if (!mobileTokenActive || paOperationId == null) {
            return null;
        }

        try {
            OperationDetailRequest request = new OperationDetailRequest();
            request.setOperationId(paOperationId);

            OperationDetailResponse paResponse = powerAuthClient.operationDetail(request);
            return paResponse.getStatus();
        } catch (PowerAuthClientException ex) {
            logger.warn(ex.getMessage(), ex);
        }
        return null;
    }

    /**
     * Get current authentication method for an operation.
     * @param operation Operation detail.
     * @return Current authentication method.
     */
    private AuthMethod getAuthMethod(GetOperationDetailResponse operation) {
        AuthMethod authMethod = AuthMethod.POWERAUTH_TOKEN;
        AuthMethod overriddenAuthMethod = authMethodResolutionService.resolveAuthMethodOverride(operation);
        if (overriddenAuthMethod != null) {
            authMethod = overriddenAuthMethod;
        }
        return authMethod;
    }

    /**
     * Fail Next Step operation.
     * @param operation Operation detail.
     */
    private void failNextStepOperation(GetOperationDetailResponse operation) {
        try {
            PAAuthenticationContext authenticationContext = null;
            if (operation.getHistory() != null) {
                authenticationContext = operation.getHistory().get(operation.getHistory().size() - 1).getPaAuthenticationContext();
            }
            // Fail Next Step operation
            nextStepClient.updateOperation(operation.getOperationId(), operation.getUserId(),
                    operation.getOrganizationId(), getAuthMethod(operation), Collections.singletonList(AuthInstrument.POWERAUTH_TOKEN),
                    AuthStepResult.AUTH_METHOD_FAILED, null, null, operation.getApplicationContext(), authenticationContext);
        } catch (NextStepClientException ex) {
            logger.warn(ex.getMessage(), ex);
        }
    }

}