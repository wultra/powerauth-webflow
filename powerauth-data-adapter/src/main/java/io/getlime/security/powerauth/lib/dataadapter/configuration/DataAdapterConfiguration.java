/*
 * Copyright 2017 Lime - HighTech Solutions s.r.o.
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
package io.getlime.security.powerauth.lib.dataadapter.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration of Data Adapter.
 *
 * @author Roman Strobl, roman.strobl@lime-company.eu
 */
@Configuration
@ComponentScan(basePackages = {"io.getlime.security.powerauth"})
public class DataAdapterConfiguration {

    /**
     * SMS OTP message expiration time in seconds.
     */
    @Value("${powerauth.authorization.sms-otp.expiration-time-in-seconds}")
    private int smsOtpExpirationTime;

    /**
     * Maximum number of tries to verify a SMS OTP message.
     */
    @Value("${powerauth.authorization.sms-otp.max-verify-tries-per-message}")
    private int smsOtpMaxVerifyTriesPerMessage;

    /**
     * Application name.
     */
    @Value("${powerauth.dataAdapter.service.applicationName}")
    private String applicationName;

    /**
     * Application display name.
     */
    @Value("${powerauth.dataAdapter.service.applicationDisplayName}")
    private String applicationDisplayName;

    /**
     * Application environment.
     */
    @Value("${powerauth.dataAdapter.service.applicationEnvironment}")
    private String applicationEnvironment;

    /**
     * Get the SMS OTP message expiration time.
     *
     * @return expiration time for SMS OTP message in seconds
     */
    public int getSmsOtpExpirationTime() {
        return smsOtpExpirationTime;
    }

    /**
     * Get the maximum number of tries for SMS OTP verification per message.
     *
     * @return Maximum number of verification tries.
     */
    public int getSmsOtpMaxVerifyTriesPerMessage() {
        return smsOtpMaxVerifyTriesPerMessage;
    }

    /**
     * Get application name.
     * @return Application name.
     */
    public String getApplicationName() {
        return applicationName;
    }

    /**
     * Get application display name.
     * @return Application display name.
     */
    public String getApplicationDisplayName() {
        return applicationDisplayName;
    }

    /**
     * Get application environment.
     * @return Application environment.
     */
    public String getApplicationEnvironment() {
        return applicationEnvironment;
    }
}
