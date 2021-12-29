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
package io.getlime.security.powerauth.lib.dataadapter.model.request;

import io.getlime.security.powerauth.lib.dataadapter.model.enumeration.AfsAction;
import io.getlime.security.powerauth.lib.dataadapter.model.enumeration.AfsAuthInstrument;
import io.getlime.security.powerauth.lib.dataadapter.model.enumeration.AfsType;
import io.getlime.security.powerauth.lib.dataadapter.model.enumeration.OperationTerminationReason;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthStepResult;

import java.util.ArrayList;
import java.util.List;

/**
 * Request parameters for AFS action.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
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
    private String clientIpAddress;

    /**
     * Index counter for this authentication step.
     */
    private int stepIndex;

    /**
     * Username filled in by the user. This value is used for user identification before user is authenticated.
     */
    private String username;

    /**
     * Authentication instruments used during this authentication step.
     */
    private final List<AfsAuthInstrument> authInstruments = new ArrayList<>();

    /**
     * Authentication step result.
     */
    private AuthStepResult authStepResult;

    /**
     * Reason why operation was terminated.
     */
    private OperationTerminationReason operationTerminationReason;

    /**
     * Default constructor.
     */
    public AfsRequestParameters() {
    }

    /**
     * Constructor with all details.
     * @param afsType AFS product type.
     * @param afsAction AFS action.
     * @param clientIpAddress Client IP address.
     * @param stepIndex Index counter for this authentication step.
     * @param username Username filled in by the user, which is used before user is authenticated.
     * @param authInstruments Authentication instruments used during this authentication step.
     * @param authStepResult Authentication step result.
     * @param operationTerminationReason Reason why operation was terminated.
     */
    public AfsRequestParameters(AfsType afsType, AfsAction afsAction, String clientIpAddress, int stepIndex, String username, List<AfsAuthInstrument> authInstruments, AuthStepResult authStepResult, OperationTerminationReason operationTerminationReason) {
        this.afsType = afsType;
        this.afsAction = afsAction;
        this.clientIpAddress = clientIpAddress;
        this.stepIndex = stepIndex;
        this.username = username;
        this.authInstruments.addAll(authInstruments);
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
    public String getClientIpAddress() {
        return clientIpAddress;
    }

    /**
     * Set client IP address.
     * @param clientIpAddress IP address.
     */
    public void setClientIpAddress(String clientIpAddress) {
        this.clientIpAddress = clientIpAddress;
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
     * Get username filled in by the user.
     * @return Username filled in by the user.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Set username filled in by the user.
     * @param username Username filled in by the user.
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Get authentication authentication instruments used during this step.
     * @return Authentication authentication instruments used during this step.
     */
    public List<AfsAuthInstrument> getAuthInstruments() {
        return authInstruments;
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
