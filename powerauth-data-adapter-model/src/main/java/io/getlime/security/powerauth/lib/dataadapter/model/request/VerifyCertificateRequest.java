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
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthInstrument;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthMethod;

/**
 * Request for certificate verification.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class VerifyCertificateRequest {

    /**
     * Certificate in PEM format for client TLS authentication.
     */
    private String certificate;

    /**
     * Signed message created using qualified certificate, including the certificate.
     */
    private String signedMessage;

    /**
     * Authentication instrument used for certificate verification: client TLS authentication
     * vs. signature using qualified certificate.
     */
    private AuthInstrument authInstrument;

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
     * Constructor with all parameters.
     * @param userId User ID.
     * @param organizationId Organization ID.
     * @param certificate Certificate in PEM format for client TLS authentication.
     * @param signedMessage Signed message created using qualified certificate, including the certificate.
     * @param authInstrument Authentication instrument used for certificate verification.
     * @param authMethod Authentication method requesting certificate verification.
     * @param accountStatus Current user account status.
     * @param operationContext Operation context.
     */
    public VerifyCertificateRequest(String userId, String organizationId, String certificate, String signedMessage,
                                    AuthInstrument authInstrument, AuthMethod authMethod,
                                    AccountStatus accountStatus, OperationContext operationContext) {
        this.userId = userId;
        this.organizationId = organizationId;
        this.certificate = certificate;
        this.signedMessage = signedMessage;
        this.authInstrument = authInstrument;
        this.authMethod = authMethod;
        this.accountStatus = accountStatus;
        this.operationContext = operationContext;
    }

    /**
     * Get certificate in PEM format.
     * @return Certificate.
     */
    public String getCertificate() {
        return certificate;
    }

    /**
     * Set certificate in PEM format.
     * @param certificate Certificate.
     */
    public void setCertificate(String certificate) {
        this.certificate = certificate;
    }

    /**
     * Get signature created using certificate.
     * @return Signature.
     */
    public String getSignedMessage() {
        return signedMessage;
    }

    /**
     * Set signature created using certificate.
     * @param signedMessage Signature.
     */
    public void setSignedMessage(String signedMessage) {
        this.signedMessage = signedMessage;
    }

    /**
     * Get authentication instrument used for certificate verification.
     * @return Authentication instrument used for certificate verification.
     */
    public AuthInstrument getAuthInstrument() {
        return authInstrument;
    }

    /**
     * Set authentication instrument used for certificate verification.
     * @param authInstrument Authentication instrument used for certificate verification.
     */
    public void setAuthInstrument(AuthInstrument authInstrument) {
        this.authInstrument = authInstrument;
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
