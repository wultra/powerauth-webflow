/*
 * PowerAuth Web Flow and related software components
 * Copyright (C) 2017 Wultra s.r.o.
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
