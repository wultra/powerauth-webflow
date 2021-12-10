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
import io.getlime.security.powerauth.lib.dataadapter.model.enumeration.UserAuthenticationResult;

/**
 * Response for user authentication.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class UserAuthenticationResponse {

    private UserAuthenticationResult authenticationResult;
    private String errorMessage;
    private Integer remainingAttempts;
    private boolean showRemainingAttempts;
    private AccountStatus accountStatus;

    /**
     * Default constructor.
     */
    public UserAuthenticationResponse() {
    }

    /**
     * Parameterized constructor.
     * @param authenticationResult Result of user authentication.
     */
    public UserAuthenticationResponse(UserAuthenticationResult authenticationResult) {
        this.authenticationResult = authenticationResult;
    }

    /**
     * Get user authentication result.
     * @return User authentication result.
     */
    public UserAuthenticationResult getAuthenticationResult() {
        return authenticationResult;
    }

    /**
     * Set user authentication result.
     * @param authenticationResult User authentication result.
     */
    public void setAuthenticationResult(UserAuthenticationResult authenticationResult) {
        this.authenticationResult = authenticationResult;
    }

    /**
     * Get error message key used in case user authentication fails.
     * @return Error message key.
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * Set error message key used in case user authentication fails.
     * @param errorMessage Error message key.
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
