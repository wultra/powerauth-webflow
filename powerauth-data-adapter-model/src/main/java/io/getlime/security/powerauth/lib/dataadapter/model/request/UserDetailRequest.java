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

/**
 * Request object for obtaining user details by user ID.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
public class UserDetailRequest {

    private String userId;
    private String organizationId;

    /**
     * Default constructor.
     */
    public UserDetailRequest() {
    }

    /**
     * Constructor with user ID as a parameter.
     * @param userId User ID.
     * @param organizationId Organization ID.
     */
    public UserDetailRequest(String userId, String organizationId) {
        this.userId = userId;
        this.organizationId = organizationId;
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

}
