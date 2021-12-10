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

/**
 * Configuration of authentication options available for the user. The class can currently configure SMS authentication
 * with optional password. In future an extension of this class with Mobile Token configuration is planned.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class AuthStepOptions {

    // By default require 2FA for SMS authentication method
    private boolean smsOtpRequired = true;
    private boolean passwordRequired = true;

    /**
     * Default constructor.
     */
    public AuthStepOptions() {
    }

    /**
     * Constructor with configuration options.
     * @param smsOtpRequired Whether SMS authorization is required.
     * @param passwordRequired Whether password authentication is required.
     */
    public AuthStepOptions(boolean smsOtpRequired, boolean passwordRequired) {
        this.smsOtpRequired = smsOtpRequired;
        this.passwordRequired = passwordRequired;
    }

    /**
     * Get whether SMS authorization is required.
     * @return Whether SMS authorization is required.
     */
    public boolean isSmsOtpRequired() {
        return smsOtpRequired;
    }

    /**
     * Set whether SMS authorization is required.
     * @param smsOtpRequired Whether SMS authorization is required.
     */
    public void setSmsOtpRequired(boolean smsOtpRequired) {
        this.smsOtpRequired = smsOtpRequired;
    }

    /**
     * Get whether password authentication is required.
     * @return Whether password authentication is required.
     */
    public boolean isPasswordRequired() {
        return passwordRequired;
    }

    /**
     * Set whether password authentication is required.
     * @param passwordRequired Whether password authentication is required.
     */
    public void setPasswordRequired(boolean passwordRequired) {
        this.passwordRequired = passwordRequired;
    }
}
