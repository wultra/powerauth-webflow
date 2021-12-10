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
package io.getlime.security.powerauth.lib.dataadapter.model.response;

import io.getlime.security.powerauth.lib.dataadapter.model.enumeration.AccountStatus;
import io.getlime.security.powerauth.lib.dataadapter.model.enumeration.SmsAuthorizationResult;
import io.getlime.security.powerauth.lib.dataadapter.model.enumeration.UserAuthenticationResult;

/**
 * Response for SMS authorization code and password verification.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class VerifySmsAndPasswordResponse {

    private SmsAuthorizationResult smsAuthorizationResult;
    private UserAuthenticationResult userAuthenticationResult;
    private String errorMessage;
    private Integer remainingAttempts;
    private boolean showRemainingAttempts;
    private AccountStatus accountStatus;

    /**
     * Default constructor.
     */
    public VerifySmsAndPasswordResponse() {
    }

    /**
     * Parameterized constructor.
     * @param smsAuthorizationResult SMS authorization result.
     * @param userAuthenticationResult User authentication result.
     */
    public VerifySmsAndPasswordResponse(SmsAuthorizationResult smsAuthorizationResult, UserAuthenticationResult userAuthenticationResult) {
        this.smsAuthorizationResult = smsAuthorizationResult;
        this.userAuthenticationResult = userAuthenticationResult;
    }

    /**
     * Get SMS authorization result.
     * @return SMS authorization result.
     */
    public SmsAuthorizationResult getSmsAuthorizationResult() {
        return smsAuthorizationResult;
    }

    /**
     * Set SMS authorization result.
     * @param smsAuthorizationResult SMS authorization result.
     */
    public void setSmsAuthorizationResult(SmsAuthorizationResult smsAuthorizationResult) {
        this.smsAuthorizationResult = smsAuthorizationResult;
    }

    /**
     * Get user authentication result.
     * @return User authentication result.
     */
    public UserAuthenticationResult getUserAuthenticationResult() {
        return userAuthenticationResult;
    }

    /**
     * User authentication result.
     * @param userAuthenticationResult User authentication result.
     */
    public void setUserAuthenticationResult(UserAuthenticationResult userAuthenticationResult) {
        this.userAuthenticationResult = userAuthenticationResult;
    }

    /**
     * Get error message key in case SMS authorization or user authentication failed.
     * @return Error message key in case of failure.
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * Set error message key in case SMS authorization or user authentication failed.
     * @param errorMessage Error message key in case of failure.
     */
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    /**
     * Get remaining attempts (optional).
     * @return Remaining attempts.
     */
    public Integer getRemainingAttempts() {
        return remainingAttempts;
    }

    /**
     * Set remaining attempts (optional).
     * @param remainingAttempts Remaining attempts.
     */
    public void setRemainingAttempts(Integer remainingAttempts) {
        this.remainingAttempts = remainingAttempts;
    }

    /**
     * Whether remaining attempts should be shown to the user.
     * @return Whether remaining attempts should be shown.
     */
    public boolean getShowRemainingAttempts() {
        return showRemainingAttempts;
    }

    /**
     * Set whether remaining attempts should be shown to the user.
     * @param showRemainingAttempts Whether remaining attempts should be shown.
     */
    public void setShowRemainingAttempts(boolean showRemainingAttempts) {
        this.showRemainingAttempts = showRemainingAttempts;
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

}
