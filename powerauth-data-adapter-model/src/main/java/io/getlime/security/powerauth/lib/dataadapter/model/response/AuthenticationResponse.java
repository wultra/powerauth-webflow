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
package io.getlime.security.powerauth.lib.dataadapter.model.response;

/**
 * Response of the authentication using user ID and password.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class AuthenticationResponse {

    /**
     * Unique user ID which identifies the user.
     */
    private String userId;

    /**
     * Organization ID to which the user ID is assigned.
     */
    private String organizationId;

    /**
     * Default constructor.
     */
    public AuthenticationResponse() {
    }

    /**
     * Constructor with user ID parameter for convenience.
     * @param userId Identification of the user.
     * @param organizationId Organization ID.
     */
    public AuthenticationResponse(String userId, String organizationId) {
        this.userId = userId;
        this.organizationId = organizationId;
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
     * @return userID User ID.
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Get the organization ID.
     * @return Organization ID.
     */
    public String getOrganizationId() {
        return organizationId;
    }

    /**
     * Set the organization ID.
     * @param organizationId Organization ID.
     */
    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }
}
