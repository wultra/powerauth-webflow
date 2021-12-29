/*
 * PowerAuth Web Flow and related software components
 * Copyright (C) 2020 Wultra s.r.o.
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
import io.getlime.security.powerauth.lib.dataadapter.model.enumeration.AccountStatus;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthMethod;

/**
 * Request for certificate verification.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class VerifyCertificateRequest {

    /**
     * Client TLS certificate in PEM format.
     */
    private String clientCertificate;

    /**
     * Authentication method requesting certificate verification.
     */
    private AuthMethod authMethod;

    /**
     * User ID for this authentication request.
     */
    private String userId;

    /**
     * Organization ID for this authentication request.
     */
    private String organizationId;

    /**
     * User account status.
     */
    private AccountStatus accountStatus;

    /**
     * Operation context.
     */
    private OperationContext operationContext;

    /**
     * Default constructor.
     */
    public VerifyCertificateRequest() {
    }

    /**
     * Constructor with message ID, authorization code and operation context.
     * @param userId User ID.
     * @param organizationId Organization ID.
     * @param clientCertificate Client TLS certificate.
     * @param authMethod Authentication method requesting certificate verification.
     * @param accountStatus Current user account status.
     * @param operationContext Operation context.
     */
    public VerifyCertificateRequest(String userId, String organizationId, String clientCertificate, AuthMethod authMethod,
                                    AccountStatus accountStatus, OperationContext operationContext) {
        this.userId = userId;
        this.organizationId = organizationId;
        this.clientCertificate = clientCertificate;
        this.authMethod = authMethod;
        this.accountStatus = accountStatus;
        this.operationContext = operationContext;
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
     * Get authentication method requesting certificate verification.
     * @return Authentication method requesting certificate verification.
     */
    public AuthMethod getAuthMethod() {
        return authMethod;
    }

    /**
     * Set authentication method requesting certificate verification.
     * @param authMethod Authentication method requesting certificate verification.
     */
    public void setAuthMethod(AuthMethod authMethod) {
        this.authMethod = authMethod;
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

    /**
     * Get current user account status.
     * @return User account status.
     */
    public AccountStatus getAccountStatus() {
        return accountStatus;
    }

    /**
     * Set current user account status.
     * @param accountStatus User account status.
     */
    public void setAccountStatus(AccountStatus accountStatus) {
        this.accountStatus = accountStatus;
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
