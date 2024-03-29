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

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
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
    @JsonSetter(nulls = Nulls.SKIP)
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
