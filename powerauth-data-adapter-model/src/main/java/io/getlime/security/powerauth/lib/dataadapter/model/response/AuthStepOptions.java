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
