/*
 * Copyright 2019 Wultra s.r.o.
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
    private UserDetailResponse userDetail;
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
     * @param userDetail User detail.
     */
    public UserAuthenticationResponse(UserAuthenticationResult authenticationResult, UserDetailResponse userDetail) {
        this.authenticationResult = authenticationResult;
        this.userDetail = userDetail;
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
     * Get user detail.
     * @return User detail.
     */
    public UserDetailResponse getUserDetail() {
        return userDetail;
    }

    /**
     * Set user detail.
     * @param userDetail User detail.
     */
    public void setUserDetail(UserDetailResponse userDetail) {
        this.userDetail = userDetail;
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
