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
package io.getlime.security.powerauth.lib.dataadapter.model.request;

import io.getlime.security.powerauth.lib.dataadapter.model.entity.AuthenticationContext;
import io.getlime.security.powerauth.lib.dataadapter.model.entity.OperationContext;

/**
 * Request for authenticating user with user ID and password.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class UserAuthenticationRequest {

    /**
     * User ID for this authentication request.
     */
    private String userId;

    /**
     * Organization ID for this authentication request.
     */
    private String organizationId;

    /**
     * Password for this authentication request.
     */
    private String password;

    /**
     * Authentication context.
     */
    private AuthenticationContext authenticationContext;

    /**
     * Operation context.
     */
    private OperationContext operationContext;

    /**
     * Default constructor.
     */
    public UserAuthenticationRequest() {
    }

    /**
     * Constructor with all parameters for convenience.
     * @param userId User ID for this authentication request.
     * @param organizationId Organization ID for this authentication request.
     * @param password Password for this authentication request, optionally encrypted.
     * @param authenticationContext Authentication context.
     * @param operationContext Operation context.
     */
    public UserAuthenticationRequest(String userId, String organizationId, String password, AuthenticationContext authenticationContext, OperationContext operationContext) {
        this.userId = userId;
        this.organizationId = organizationId;
        this.password = password;
        this.authenticationContext = authenticationContext;
        this.operationContext = operationContext;
    }

    /**
     * Set the user ID.
     * @param userId User ID.
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * Get the user ID.
     * @return User ID.
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Set the password. The password can be encrypted, in this case the type specifies encryption type and
     * cipherTransformation specifies the algorithm, mode and padding.
     * @param password Password.
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Get the password. The password can be encrypted, in this case the passwordProtection specifies encryption type and
     * cipherTransformation specifies the algorithm, mode and padding.
     * @return Password.
     */
    public String getPassword() {
        return password;
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
     * Get authentication context.
     * @return Authentication context.
     */
    public AuthenticationContext getAuthenticationContext() {
        return authenticationContext;
    }

    /**
     * Set authentication context.
     * @param authenticationContext Authentication context.
     */
    public void setAuthenticationContext(AuthenticationContext authenticationContext) {
        this.authenticationContext = authenticationContext;
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
