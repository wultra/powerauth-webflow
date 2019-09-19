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
package io.getlime.security.powerauth.lib.dataadapter.model.request;

import io.getlime.security.powerauth.lib.dataadapter.model.enumeration.AfsAction;
import io.getlime.security.powerauth.lib.dataadapter.model.enumeration.AfsType;
import io.getlime.security.powerauth.lib.dataadapter.model.enumeration.OperationTerminationReason;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthStepResult;

public class AfsRequestParameters {

    /**
     * AFS product type.
     */
    private AfsType afsType;

    /**
     * AFS action.
     */
    private AfsAction afsAction;

    /**
     * Client IP address.
     */
    private String clientIp;

    /**
     * Index counter for this authentication step.
     */
    private int stepIndex;

    /**
     * Authentication step result.
     */
    private AuthStepResult authStepResult;

    /**
     * Reason why operation was terminated.
     */
    private OperationTerminationReason operationTerminationReason;

    /**
     * Default constuctor.
     */
    public AfsRequestParameters() {
    }

    /**
     * Constuctor with all details.
     * @param afsType AFS product type.
     * @param afsAction AFS action.
     * @param clientIp Client IP address.
     * @param stepIndex Index counter for this authentication step.
     * @param authStepResult Authentication step result.
     * @param operationTerminationReason Reason why operation was terminated.
     */
    public AfsRequestParameters(AfsType afsType, AfsAction afsAction, String clientIp, int stepIndex, AuthStepResult authStepResult, OperationTerminationReason operationTerminationReason) {
        this.afsType = afsType;
        this.afsAction = afsAction;
        this.clientIp = clientIp;
        this.stepIndex = stepIndex;
        this.authStepResult = authStepResult;
        this.operationTerminationReason = operationTerminationReason;
    }

    /**
     * Get the AFS product type.
     * @return AFS product type.
     */
    public AfsType getAfsType() {
        return afsType;
    }

    /**
     * Set the AFS product type.
     * @param afsType AFS product type.
     */
    public void setAfsType(AfsType afsType) {
        this.afsType = afsType;
    }

    /**
     * Get the AFS action.
     * @return AFS action.
     */
    public AfsAction getAfsAction() {
        return afsAction;
    }

    /**
     * Set the AFS action.
     * @param afsAction AFS action.
     */
    public void setAfsAction(AfsAction afsAction) {
        this.afsAction = afsAction;
    }
    /**
     * Get client IP address.
     * @return Client IP address.
     */
    public String getClientIp() {
        return clientIp;
    }

    /**
     * Set client IP address.
     * @param clientIp IP address.
     */
    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }

    /**
     * Get index counter for this authentication step.
     * @return Index counter for this authentication step.
     */
    public int getStepIndex() {
        return stepIndex;
    }

    /**
     * Set index counter for this authentication step.
     * @param stepIndex Index counter for this authentication step.
     */
    public void setStepIndex(int stepIndex) {
        this.stepIndex = stepIndex;
    }

    /**
     * Get authentication step result.
     * @return Authentication step result.
     */
    public AuthStepResult getAuthStepResult() {
        return authStepResult;
    }

    /**
     * Set authentication step result.
     * @param authStepResult Authentication step result.
     */
    public void setAuthStepResult(AuthStepResult authStepResult) {
        this.authStepResult = authStepResult;
    }

    /**
     * Get reason why operation was terminated, use null for active operations.
     * @return Reason why operation was terminated.
     */
    public OperationTerminationReason getOperationTerminationReason() {
        return operationTerminationReason;
    }

    /**
     * Set reason why operation was terminated, use null for active operations.
     * @param operationTerminationReason Reason why operation was terminated.
     */
    public void setOperationTerminationReason(OperationTerminationReason operationTerminationReason) {
        this.operationTerminationReason = operationTerminationReason;
    }
}
