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

import io.getlime.security.powerauth.lib.dataadapter.model.entity.OperationContext;
import io.getlime.security.powerauth.lib.dataadapter.model.enumeration.AfsAction;
import io.getlime.security.powerauth.lib.dataadapter.model.enumeration.AfsType;
import io.getlime.security.powerauth.lib.dataadapter.model.enumeration.AuthInstrument;
import io.getlime.security.powerauth.lib.dataadapter.model.enumeration.OperationTerminationReason;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthStepResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Request for an anti-fraud system call.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class AfsRequest {

    /**
     * AFS product type.
     */
    private AfsType afsType;

    /**
     * User ID for this request, use null value before user is authenticated.
     */
    private String userId;

    /**
     * Organization ID for this request.
     */
    private String organizationId;

    /**
     * Operation context which provides context for creating the consent form.
     */
    private OperationContext operationContext;

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
     * Authentication instruments used during this authentication step.
     */
    private final List<AuthInstrument> authInstruments = new ArrayList<>();

    /**
     * Extra parameters sent with the request depending on AFS type, e.g. cookies for Threat Mark, logout reason, etc.
     */
    private final Map<String, String> extras = new HashMap<>();

    /**
     * Default constructor.
     */
    public AfsRequest() {
    }

    /**
     * Constructor all details.
     * @param afsType AFS product type.
     * @param afsAction AFS action.
     * @param userId User ID.
     * @param organizationId Organization ID.
     * @param operationContext Operation context which provides context for creating the consent form.
     */
    public AfsRequest(AfsType afsType, AfsAction afsAction, String userId, String organizationId, OperationContext operationContext) {
        this.afsType = afsType;
        this.afsAction = afsAction;
        this.userId = userId;
        this.organizationId = organizationId;
        this.operationContext = operationContext;
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
     * Get user ID.
     * @return User ID.
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Set user ID.
     * @param userId user ID.
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * Get organization ID.
     * @return Organization ID.
     */
    public String getOrganizationId() {
        return organizationId;
    }

    /**
     * Set organization ID.
     * @param organizationId Organization ID.
     */
    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }

    /**
     * Get operation context which provides context for creating the consent form.
     * @return Operation context which provides context for creating the consent form.
     */
    public OperationContext getOperationContext() {
        return operationContext;
    }

    /**
     * Set operation context which provides context for creating the consent form.
     * @param operationContext Operation context which provides context for creating the consent form.
     */
    public void setOperationContext(OperationContext operationContext) {
        this.operationContext = operationContext;
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

    /**
     * Get authentication authentication instruments used during this step.
     * @return Authentication authentication instruments used during this step.
     */
    public List<AuthInstrument> getAuthInstruments() {
        return authInstruments;
    }

    /**
     * Extra parameters sent with the request depending on AFS type, e.g. cookies for Threat Mark.
     * @return Get extra parameters for AFS.
     */
    public Map<String, String> getExtras() {
        return extras;
    }
}
