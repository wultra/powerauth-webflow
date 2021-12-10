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

package io.getlime.security.powerauth.lib.webflow.authentication.security;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Locale;

/**
 * Object representing a user authentication.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
public class UserOperationAuthentication extends AbstractAuthenticationToken implements Serializable {

    private static final long serialVersionUID = 4514448849365459373L;

    private String userId;
    private String operationId;
    private Boolean strongAuthentication;
    private String language;
    private String organizationId;

    /**
     * Default constructor.
     */
    public UserOperationAuthentication() {
        super(null);
        this.strongAuthentication = false;
        this.language = Locale.US.getLanguage();
        this.organizationId = null;
    }

    /**
     * Constructor with operation and user details.
     *
     * @param operationId Operation ID.
     * @param userId User ID.
     * @param organizationId Organization ID.
     */
    public UserOperationAuthentication(String operationId, String userId, String organizationId) {
        super(null);
        this.operationId = operationId;
        this.userId = userId;
        this.strongAuthentication = false;
        this.language = Locale.US.getLanguage();
        this.organizationId = organizationId;
    }

    @Override
    public String getName() {
        return userId;
    }

    @Override
    public Collection<GrantedAuthority> getAuthorities() {
        ArrayList<GrantedAuthority> authorities = new ArrayList<>(1);
        authorities.add(new SimpleGrantedAuthority("USER"));
        return Collections.unmodifiableList(authorities);
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return this.userId + "." + this.operationId;
    }

    /**
     * Get user ID
     *
     * @return User ID
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Set user ID
     *
     * @param userId User ID
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
     * Get operation ID.
     *
     * @return Operation ID.
     */
    public String getOperationId() {
        return operationId;
    }

    /**
     * Set operation ID.
     *
     * @param operationId Operation ID.
     */
    public void setOperationId(String operationId) {
        this.operationId = operationId;
    }

    /**
     * Get information about the type of authentication (strong = more than one factor).
     * @return True of authentication is strong, false otherwise.
     */
    public Boolean isStrongAuthentication() {
        return strongAuthentication;
    }

    /**
     * Set information about the type of authentication (strong = more than one factor).
     * @param strongAuthentication True of authentication is strong, false otherwise.
     */
    public void setStrongAuthentication(Boolean strongAuthentication) {
        this.strongAuthentication = strongAuthentication;
    }

    /**
     * Get information about language that is set for the authentication.
     * @return Language information in ISO format.
     */
    public String getLanguage() {
        return language;
    }

    /**
     * Set information about language that is set for the authentication.
     * @param language Language information in ISO format.
     */
    public void setLanguage(String language) {
        this.language = language;
    }
}
