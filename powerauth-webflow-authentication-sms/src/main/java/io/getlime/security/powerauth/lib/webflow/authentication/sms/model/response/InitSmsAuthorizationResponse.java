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
package io.getlime.security.powerauth.lib.webflow.authentication.sms.model.response;

import io.getlime.security.powerauth.lib.webflow.authentication.base.AuthStepResponse;

/**
 * Response for init SMS authorization.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class InitSmsAuthorizationResponse extends AuthStepResponse {

    private String username;
    private boolean passwordEnabled;
    private boolean smsOtpEnabled;
    private boolean certificateEnabled;
    private int resendDelay;

    /**
     * Get username for SMS authorization combined with password (optional).
     * @return Username for SMS authorization combined with password (optional).
     */
    public String getUsername() {
        return username;
    }

    /**
     * Set username for SMS authorization combined with password (optional).
     * @param username Username for SMS authorization combined with password (optional).
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Get whether password is enabled for SMS authorization combined with password.
     * @return Whether password is enabled for SMS authorization combined with password.
     */
    public boolean isPasswordEnabled() {
        return passwordEnabled;
    }

    /**
     * Set whether password is enabled for SMS authorization combined with password.
     * @param passwordEnabled Whether password is enabled for SMS authorization combined with password.
     */
    public void setPasswordEnabled(boolean passwordEnabled) {
        this.passwordEnabled = passwordEnabled;
    }

    /**
     * Get whether authorization using SMS code is enabled.
     * @return Whether authorization using SMS code is enabled.
     */
    public boolean isSmsOtpEnabled() {
        return smsOtpEnabled;
    }

    /**
     * Set whether authorization using SMS code is enabled.
     * @param smsOtpEnabled Whether authorization using SMS code is enabled.
     */
    public void setSmsOtpEnabled(boolean smsOtpEnabled) {
        this.smsOtpEnabled = smsOtpEnabled;
    }

    /**
     * Get delay for resending SMS in milliseconds.
     * @return Delay for resending SMS in milliseconds.
     */
    public int getResendDelay() {
        return resendDelay;
    }

    /**
     * Set delay for resending SMS in milliseconds.
     * @param resendDelay Delay for resending SMS in milliseconds.
     */
    public void setResendDelay(int resendDelay) {
        this.resendDelay = resendDelay;
    }

    /**
     * Get whether authorization using certificate is enabled.
     * @return Whether authorization using certificate is enabled.
     */
    public boolean isCertificateEnabled() {
        return certificateEnabled;
    }

    /**
     * Set whether authorization using certificate is enabled.
     * @param certificateEnabled Whether authorization using certificate is enabled.
     */
    public void setCertificateEnabled(boolean certificateEnabled) {
        this.certificateEnabled = certificateEnabled;
    }
}
