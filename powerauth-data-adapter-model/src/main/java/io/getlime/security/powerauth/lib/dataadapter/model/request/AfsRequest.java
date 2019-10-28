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

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Request for an anti-fraud system call.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class AfsRequest {

    /**
     * User ID for this request, use null value before user is authenticated.
     */
    private String userId;

    /**
     * Organization ID for this request.
     */
    private String organizationId;

    /**
     * Operation context.
     */
    private OperationContext operationContext;

    /**
     * Request parameters for anti-fraud system.
     */
    private AfsRequestParameters afsRequestParameters;

    /**
     * Extra parameters sent with the request depending on AFS type, e.g. cookies for Threat Mark.
     */
    private final Map<String, Object> extras = new LinkedHashMap<>();

    /**
     * Default constructor.
     */
    public AfsRequest() {
    }

    /**
     * Constructor with all details.
     * @param userId User ID.
     * @param organizationId Organization ID.
     * @param operationContext Operation context.
     * @param afsRequestParameters Request parameters for AFS.
     * @param extras Extra parameters for AFS.
     */
    public AfsRequest(String userId, String organizationId, OperationContext operationContext, AfsRequestParameters afsRequestParameters, Map<String, Object> extras) {
        this.userId = userId;
        this.organizationId = organizationId;
        this.operationContext = operationContext;
        this.afsRequestParameters = afsRequestParameters;
        this.extras.putAll(extras);
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
     * Get operation context.
     * @return Operation context.
     */
    public OperationContext getOperationContext() {
        return operationContext;
    }

    /**
     * Set operation context.
     * @param operationContext Operation context.
     */
    public void setOperationContext(OperationContext operationContext) {
        this.operationContext = operationContext;
    }

    /**
     * Get request parameters for anti-fraud system.
     * @return Request parameters for anti-fraud system.
     */
    public AfsRequestParameters getAfsRequestParameters() {
        return afsRequestParameters;
    }

    /**
     * Set request parameters for anti-fraud system.
     * @param afsRequestParameters Request parameters for anti-fraud system.
     */
    public void setAfsRequestParameters(AfsRequestParameters afsRequestParameters) {
        this.afsRequestParameters = afsRequestParameters;
    }

    /**
     * Extra parameters sent with the request depending on AFS type, e.g. cookies for Threat Mark.
     * @return Get extra parameters for AFS.
     */
    public Map<String, Object> getExtras() {
        return extras;
    }
}
