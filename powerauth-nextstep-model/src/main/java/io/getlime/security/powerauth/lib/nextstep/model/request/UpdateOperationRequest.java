/*
 * Copyright 2017 Wultra s.r.o.
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
package io.getlime.security.powerauth.lib.nextstep.model.request;

import io.getlime.security.powerauth.lib.nextstep.model.entity.KeyValueParameter;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthMethod;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthStepResult;

import java.util.ArrayList;
import java.util.List;

/**
 * Request object used for updating an operation.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
public class UpdateOperationRequest {

    private String operationId;
    private String userId;
    private String organizationId;
    private AuthMethod authMethod;
    private AuthStepResult authStepResult;
    private String authStepResultDescription;
    private List<KeyValueParameter> params;

    /**
     * Default constructor.
     */
    public UpdateOperationRequest() {
        params = new ArrayList<>();
    }

    /**
     * Get operation ID.
     * @return Operation ID.
     */
    public String getOperationId() {
        return operationId;
    }

    /**
     * Set operation ID.
     * @param operationId Operation ID.
     */
    public void setOperationId(String operationId) {
        this.operationId = operationId;
    }

    /**
     * Get user ID of the user who is associated with the operation.
     * @return User ID.
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Set user ID of the user who is associated with the operation.
     * @param userId User ID.
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
     * Get the used authentication method.
     * @return Authentication method.
     */
    public AuthMethod getAuthMethod() {
        return authMethod;
    }

    /**
     * Set the used authentication method.
     * @param authMethod Authentication method.
     */
    public void setAuthMethod(AuthMethod authMethod) {
        this.authMethod = authMethod;
    }

    /**
     * Get the result of the previous authentication step.
     * @return Authentication step result.
     */
    public AuthStepResult getAuthStepResult() {
        return authStepResult;
    }

    /**
     * Set the result of the previous authentication step.
     * @param authStepResult Authentication step result.
     */
    public void setAuthStepResult(AuthStepResult authStepResult) {
        this.authStepResult = authStepResult;
    }

    /**
     * Get the description of the previous authentication step result.
     *
     * @return Description of authentication step result.
     */
    public String getAuthStepResultDescription() {
        return authStepResultDescription;
    }

    /**
     * Set the description of the previous authentication step result.
     *
     * @param authStepResultDescription Description of authentication step result.
     */
    public void setAuthStepResultDescription(String authStepResultDescription) {
        this.authStepResultDescription = authStepResultDescription;
    }
    /**
     * Get the list with optional extra parameters.
     * @return Extra parameters.
     */
    public List<KeyValueParameter> getParams() {
        return params;
    }

}
