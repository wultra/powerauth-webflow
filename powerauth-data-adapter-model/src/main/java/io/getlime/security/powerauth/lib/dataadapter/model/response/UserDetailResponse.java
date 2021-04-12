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

import io.getlime.security.powerauth.lib.dataadapter.model.enumeration.AccountStatus;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Response with user details.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
public class UserDetailResponse {

    private String id;
    private String givenName;
    private String familyName;
    private String organizationId;
    private AccountStatus accountStatus;
    private final Map<String, Object> extras = new LinkedHashMap<>();

    /**
     * Get user ID.
     * @return User ID.
     */
    public String getId() {
        return id;
    }

    /**
     * Set user ID.
     * @param id User ID.
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Get user's given name.
     * @return Given (first) name.
     */
    public String getGivenName() {
        return givenName;
    }

    /**
     * Set user's given name.
     * @param givenName Given (first) name.
     */
    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }

    /**
     * Get user's family name.
     * @return User's family (last) name.
     */
    public String getFamilyName() {
        return familyName;
    }

    /**
     * Set user's family name.
     * @param familyName User's family (last) name.
     */
    public void setFamilyName(String familyName) {
        this.familyName = familyName;
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
     * Get user account status.
     * @return User account status.
     */
    public AccountStatus getAccountStatus() {
        return accountStatus;
    }

    /**
     * Set user account status.
     * @param accountStatus Status.
     */
    public void setAccountStatus(AccountStatus accountStatus) {
        this.accountStatus = accountStatus;
    }

    /**
     * Extra attributes related to user identity.
     * @return Get extra attributes related to user identity.
     */
    public Map<String, Object> getExtras() {
        return extras;
    }

}
