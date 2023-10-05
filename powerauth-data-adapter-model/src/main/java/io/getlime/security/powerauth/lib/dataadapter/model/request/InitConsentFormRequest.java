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

import io.getlime.security.powerauth.lib.dataadapter.model.entity.OperationContext;

/**
 * Request for initializing the OAuth 2.1 consent form.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class InitConsentFormRequest {

    /**
     * User ID for this request.
     */
    private String userId;

    /**
     * Organization ID for this request.
     */
    private String organizationId;

    /**
     * Operation context which provides context for initializing the consent form.
     */
    private OperationContext operationContext;

    /**
     * Default constructor.
     */
    public InitConsentFormRequest() {
    }

    /**
     * Constructor with user ID, language and operation context.
     * @param userId User ID.
     * @param organizationId Organization ID.
     * @param operationContext Operation context which provides context for initializing the consent form.
     */
    public InitConsentFormRequest(String userId, String organizationId, OperationContext operationContext) {
        this.userId = userId;
        this.organizationId = organizationId;
        this.operationContext = operationContext;
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
     * Get operation context which provides context for initializing the consent form.
     * @return Operation context which provides context for initializing the consent form.
     */
    public OperationContext getOperationContext() {
        return operationContext;
    }

    /**
     * Set operation context which provides context for initializing the consent form.
     * @param operationContext Operation context which provides context for initializing the consent form.
     */
    public void setOperationContext(OperationContext operationContext) {
        this.operationContext = operationContext;
    }

}
