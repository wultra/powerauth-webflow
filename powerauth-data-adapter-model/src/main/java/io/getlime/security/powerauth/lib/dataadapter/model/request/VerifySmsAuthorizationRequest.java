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
