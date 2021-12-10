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
package io.getlime.security.powerauth.lib.dataadapter.model.request;

import io.getlime.security.powerauth.lib.dataadapter.model.entity.OperationContext;
import io.getlime.security.powerauth.lib.dataadapter.model.enumeration.AccountStatus;

/**
 * Request for SMS authorization code verification.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class VerifySmsAuthorizationRequest {

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
    public VerifySmsAuthorizationRequest() {
    }

    /**
     * Constructor with message ID, authorization code and operation context.
     * @param messageId Message ID.
     * @param authorizationCode Authorization code from user.
     * @param userId User ID.
     * @param organizationId Organization ID.
     * @param accountStatus Current user account status.
     * @param operationContext Operation context.
     */
    public VerifySmsAuthorizationRequest(String messageId, String authorizationCode, String userId, String organizationId,
                                         AccountStatus accountStatus, OperationContext operationContext) {
        this.messageId = messageId;
        this.authorizationCode = authorizationCode;
        this.userId = userId;
        this.organizationId = organizationId;
        this.accountStatus = accountStatus;
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
