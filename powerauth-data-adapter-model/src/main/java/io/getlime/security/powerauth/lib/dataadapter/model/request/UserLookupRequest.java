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
     * Client TLS certificate.
     */
    private String clientCertificate;

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
     * @param clientCertificate Client TLS certificate.
     * @param operationContext Operation context.
     */
    public UserLookupRequest(String username, String organizationId, String clientCertificate, OperationContext operationContext) {
        this.username = username;
        this.organizationId = organizationId;
        this.clientCertificate = clientCertificate;
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
     * Get client TLS certificate.
     * @return Client TLS certificate.
     */
    public String getClientCertificate() {
        return clientCertificate;
    }

    /**
     * Set client TLS certificate.
     * @param clientCertificate Client TLS certificate.
     */
    public void setClientCertificate(String clientCertificate) {
        this.clientCertificate = clientCertificate;
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
