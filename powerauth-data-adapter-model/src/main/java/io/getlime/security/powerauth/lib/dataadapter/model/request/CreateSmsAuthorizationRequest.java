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
import io.getlime.security.powerauth.lib.dataadapter.model.entity.UserContact;
import io.getlime.security.powerauth.lib.dataadapter.model.enumeration.AccountStatus;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthMethod;

import java.util.ArrayList;
import java.util.List;

/**
 * Request for creating SMS OTP authorization message.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class CreateSmsAuthorizationRequest {

    /**
     * User ID for this authorization request.
     */
    private String userId;

    /**
     * Organization ID for this authorization request.
     */
    private String organizationId;

    /**
     * User contact information.
     */
    private List<UserContact> userContacts = new ArrayList<>();


    /**
     * User account status.
     */
    private AccountStatus accountStatus;

    /**
     * Authentication method.
     */
    private AuthMethod authMethod;

    /**
     * Operation context.
     */
    private OperationContext operationContext;

    /**
     * Language used in the SMS OTP messages.
     */
    private String lang;

    /**
     * Whether SMS is being resent.
     */
    private boolean resend;

    /**
     * Default constructor.
     */
    public CreateSmsAuthorizationRequest() {
    }

    /**
     * Constructor with user ID, language and operation context.
     * @param userId User ID.
     * @param organizationId Organization ID.
     * @param accountStatus User account status.
     * @param lang SMS language.
     * @param authMethod Authentication method.
     * @param operationContext Operation context.
     * @param resend Whether SMS is being resent.
     */
    public CreateSmsAuthorizationRequest(String userId, String organizationId, List<UserContact> userContacts, AccountStatus accountStatus, String lang, AuthMethod authMethod, OperationContext operationContext, boolean resend) {
        this.userId = userId;
        this.organizationId = organizationId;
        this.userContacts = userContacts;
        this.accountStatus = accountStatus;
        this.lang = lang;
        this.authMethod = authMethod;
        this.operationContext = operationContext;
        this.resend = resend;
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
     * @param userId user ID.
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
     * Get user contacts.
     * @return User contacts.
     */
    public List<UserContact> getUserContacts() {
        return userContacts;
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
     * Get authentication method.
     * @return Authentication method.
     */
    public AuthMethod getAuthMethod() {
        return authMethod;
    }

    /**
     * Set authentication method.
     * @param authMethod Authentication method.
     */
    public void setAuthMethod(AuthMethod authMethod) {
        this.authMethod = authMethod;
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

    /**
     * Get SMS language.
     * @return SMS language.
     */
    public String getLang() {
        return lang;
    }

    /**
     * Set SMS language.
     * @param lang SMS language.
     */
    public void setLang(String lang) {
        this.lang = lang;
    }

    /**
     * Get whether SMS is being resent.
     * @return Whether SMS is being resent.
     */
    public boolean isResend() {
        return resend;
    }

    /**
     * Set whether SMS is being resent.
     * @param resend Whether SMS is being resent.
     */
    public void setResend(boolean resend) {
        this.resend = resend;
    }
}
