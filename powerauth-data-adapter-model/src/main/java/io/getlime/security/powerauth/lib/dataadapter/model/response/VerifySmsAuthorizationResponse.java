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
package io.getlime.security.powerauth.lib.dataadapter.model.response;

import io.getlime.security.powerauth.lib.dataadapter.model.enumeration.SmsAuthorizationResult;

/**
 * Response for SMS authorization code verification.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class VerifySmsAuthorizationResponse {

    private SmsAuthorizationResult smsAuthorizationResult;
    private String errorMessage;
    private Integer remainingAttempts;
    private boolean showRemainingAttempts;

    /**
     * Default constructor.
     */
    public VerifySmsAuthorizationResponse() {
    }

    /**
     * Parameterized constructor.
     * @param smsAuthorizationResult SMS authorization result.
     */
    public VerifySmsAuthorizationResponse(SmsAuthorizationResult smsAuthorizationResult) {
        this.smsAuthorizationResult = smsAuthorizationResult;
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
     * Get error message key in case SMS authorization failed.
     * @return Error message key in case of failure.
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * Set error message key in case SMS authorization failed.
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
}
