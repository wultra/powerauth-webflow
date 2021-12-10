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

import io.getlime.security.powerauth.lib.dataadapter.model.entity.AuthenticationContext;
import io.getlime.security.powerauth.lib.dataadapter.model.entity.OperationContext;
import io.getlime.security.powerauth.lib.dataadapter.model.enumeration.AccountStatus;

/**
 * Request for SMS authorization code and password verification.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class VerifySmsAndPasswordRequest {

    /**
     * SMS message ID.
     */
    private String messageId;

    /**
     * SMS OTP authorization code.
     */
    private String authorizationCode;

    /**
     * User ID for this authentication request.
     */
    private String userId;

    /**
     * Password for this authentication request.
     */
    private String password;

    /**
     * Organization ID for this authentication request.
     */
    private String organizationId;

    /**
     * User account status.
     */
    private AccountStatus accountStatus;

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
    public VerifySmsAndPasswordRequest() {
    }

    /**
     * Constructor with all details.
     * @param messageId Message ID.
     * @param authorizationCode Authorization code from user.
     * @param userId User ID for this authentication request.
     * @param organizationId Organization ID for this authentication request.
     * @param accountStatus Current user account status.
     * @param password Password for this authentication request, optionally encrypted.
     * @param authenticationContext Authentication context.
     * @param operationContext Operation context.
     */
    public VerifySmsAndPasswordRequest(String messageId, String authorizationCode, String userId, String organizationId, AccountStatus accountStatus,
                                       String password, AuthenticationContext authenticationContext, OperationContext operationContext) {
        this.messageId = messageId;
        this.authorizationCode = authorizationCode;
        this.userId = userId;
        this.organizationId = organizationId;
        this.accountStatus = accountStatus;
        this.password = password;
        this.authenticationContext = authenticationContext;
        this.operationContext = operationContext;
    }

    /**
     * Get message ID.
     * @return Message ID.
     */
    public String getMessageId() {
        return messageId;
    }

    /**
     * Set message ID.
     * @param messageId Message ID.
     */
    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    /**
     * Get authorization code.
     * @return Authorization code.
     */
    public String getAuthorizationCode() {
        return authorizationCode;
    }

    /**
     * Set authorization code.
     * @param authorizationCode Authorization code.
     */
    public void setAuthorizationCode(String authorizationCode) {
        this.authorizationCode = authorizationCode;
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
     * Set the password. The password can be encrypted, in this case the type specifies encryption method and
     * cipherTransformation specifies the algorithm, mode and padding.
     * @param password Password.
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Get the password. The password can be encrypted, in this case the passwordProtection specifies encryption method and
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
