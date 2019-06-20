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

/**
 * Lookup user identity by username.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class UserLookupRequest {

    /**
     * User name for this user lookup request.
     */
    private String username;

    /**
     * Organization ID for this user lookup request.
     */
    private String organizationId;

    /**
     * Operation context.
     */
    private OperationContext operationContext;

    /**
     * Default constructor.
     */
    public UserLookupRequest() {
    }

    /**
     * Constructor with username and organization ID.
     * @param username Username for this lookup request.
     * @param organizationId Organization ID for this lookup request.
     * @param operationContext Operation context.
     */
    public UserLookupRequest(String username, String organizationId, OperationContext operationContext) {
        this.username = username;
        this.organizationId = organizationId;
        this.operationContext = operationContext;
    }

    /**
     * Get the username.
     * @return Username.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Set the username.
     * @param username Username.
     */
    public void setUsername(String username) {
        this.username = username;
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

}
