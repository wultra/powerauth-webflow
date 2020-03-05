/*
 * Copyright 2020 Wultra s.r.o.
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
import io.getlime.security.powerauth.lib.dataadapter.model.enumeration.CertificateVerificationResult;

/**
 * Response for client TLS certificate verification.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class VerifyCertificateResponse {

    private CertificateVerificationResult certificateVerificationResult;
    private String errorMessage;
    private Integer remainingAttempts;
    private boolean showRemainingAttempts;
    private AccountStatus accountStatus;

    /**
     * Default constructor.
     */
    public VerifyCertificateResponse() {
    }

    /**
     * Parameterized constructor.
     * @param certificateVerificationResult Result of client TLS certificate authorization.
     */
    public VerifyCertificateResponse(CertificateVerificationResult certificateVerificationResult) {
        this.certificateVerificationResult = certificateVerificationResult;
    }

    /**
     * Get certificate verification result.
     * @return Certificate verification result.
     */
    public CertificateVerificationResult getCertificateVerificationResult() {
        return certificateVerificationResult;
    }

    /**
     * Set certificate verification result.
     * @param certificateVerificationResult Certificate verification result.
     */
    public void setCertificateVerificationResult(CertificateVerificationResult certificateVerificationResult) {
        this.certificateVerificationResult = certificateVerificationResult;
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
