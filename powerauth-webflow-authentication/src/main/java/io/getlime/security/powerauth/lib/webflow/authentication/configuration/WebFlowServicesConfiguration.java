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

package io.getlime.security.powerauth.lib.webflow.authentication.configuration;

import io.getlime.security.powerauth.lib.dataadapter.client.DataAdapterClient;
import io.getlime.security.powerauth.lib.dataadapter.model.enumeration.AfsType;
import io.getlime.security.powerauth.lib.dataadapter.model.enumeration.PasswordProtectionType;
import io.getlime.security.powerauth.lib.nextstep.client.NextStepClient;
import io.getlime.security.powerauth.lib.webflow.authentication.service.SSLConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Basic configuration class, used to configure clients to Next Step service and Data Adapter.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
@Configuration
public class WebFlowServicesConfiguration {

    private SSLConfigurationService sslConfigurationService;

    /**
     * Data Adapter service URL.
     */
    @Value("${powerauth.dataAdapter.service.url}")
    private String dataAdapterServiceUrl;

    /**
     * Next step server service URL.
     */
    @Value("${powerauth.nextstep.service.url}")
    private String nextstepServiceUrl;

    /**
     * Whether invalid SSL certificates should be accepted.
     */
    @Value("${powerauth.service.ssl.acceptInvalidSslCertificate}")
    private boolean acceptInvalidSslCertificate;

    /**
     * Whether offline mode is available in Mobile Token.
     */
    @Value("${powerauth.webflow.offlineMode.available}")
    private boolean offlineModeAvailable;

    /**
     * Authentication type which configures how username and password is transferred for verification.
     */
    @Value("${powerauth.webflow.password.protection.type:NO_PROTECTION}")
    private PasswordProtectionType passwordProtection;

    /**
     * Encryption cipher transformation for encrypted requests (e.g. AES/CBC/PKCS7Padding).
     */
    @Value("${powerauth.webflow.password.encryption.transformation}")
    private String cipherTransformation;

    /**
     * Base64 encoded password encryption key.
     */
    @Value("${powerauth.webflow.password.encryption.key}")
    private String passwordEncryptionKey;

    /**
     * Delay for resending SMS in milliseconds.
     */
    @Value("${powerauth.webflow.sms.resend.delayMs:60000}")
    private int smsResendDelay;

    /**
     * Delay for displaying timeout warning in milliseconds.
     */
    @Value("${powerauth.webflow.timeout.warning.delayMs:60000}")
    private int timeoutWarningDelay;

    /**
     * Whether anti-fraud system integration is enabled.
     */
    @Value("${powerauth.webflow.afs.enabled:false}")
    private boolean afsEnabled;

    /**
     * Type of product used for anti-fraud system integration.
     */
    @Value("${powerauth.webflow.afs.type:THREAT_MARK}")
    private AfsType afsType;

    /**
     * Whether client IP address is detected in anti-fraud system integration.
     */
    @Value("${powerauth.webflow.afs.detectIpAddress:false}")
    private boolean afsIpAddressDetectionEnabled;

    /**
     * Whether anti-fraud system requires IPv4 addresses.
     */
    @Value("${powerauth.webflow.afs.forceIpv4:true}")
    private boolean afsIpv4Forced;

    /**
     * Configuration of tm_device_tag cookie for Threat Mark AFS integration.
     */
    @Value("${powerauth.webflow.afs.tm.cookies.deviceTag}")
    private String tmDeviceTagCookie;

    /**
     * Configuration of tm_session_sid cookie for Threat Mark AFS integration.
     */
    @Value("${powerauth.webflow.afs.tm.cookies.sessionSid}")
    private String tmSessionSidCookie;

    /**
     * Whether client TLS certificate can be used for authenticating user.
     */
    @Value("${powerauth.webflow.authentication.client.certificate.enabled}")
    private boolean clientCertificateAuthenticationEnabled;

    /**
     * Get client TLS certificate verification URL for login which is used in case client TLS certificate verification is enabled.
     */
    @Value("${powerauth.webflow.authentication.client.certificate.login.url}")
    private String certificateVerificationUrlForLogin;

    /**
     * Get client TLS certificate verification URL for approval which is used in case client TLS certificate verification is enabled.
     */
    @Value("${powerauth.webflow.authentication.client.certificate.approval.url}")
    private String certificateVerificationUrlForApproval;

    @Autowired
    public WebFlowServicesConfiguration(SSLConfigurationService sslConfigurationService) {
        this.sslConfigurationService = sslConfigurationService;
    }

    /**
     * Default data adapter client.
     *
     * @return Data adapter client.
     */
    @Bean
    public DataAdapterClient defaultDataAdapterClient() {
        DataAdapterClient client = new DataAdapterClient(dataAdapterServiceUrl);
        // whether invalid SSL certificates should be accepted
        if (acceptInvalidSslCertificate) {
            sslConfigurationService.trustAllCertificates();
        }
        return client;
    }

    /**
     * Default Next Step service client.
     *
     * @return Next Step service client.
     */
    @Bean
    public NextStepClient defaultNextStepClient() {
        NextStepClient client = new NextStepClient(nextstepServiceUrl);
        // whether invalid SSL certificates should be accepted
        if (acceptInvalidSslCertificate) {
            sslConfigurationService.trustAllCertificates();
        }
        return client;
    }

    /**
     * Whether offline mode is available.
     * @return True if offline mode is available.
     */
    public boolean isOfflineModeAvailable() {
        return offlineModeAvailable;
    }

    /**
     * Get authentication type which configures how username and password is transferred for verification.
     * @return Authentication type.
     */
    public PasswordProtectionType getPasswordProtection() {
        return passwordProtection;
    }

    /**
     * Get encryption cipher transformation for encrypted requests (e.g. AES/CBC/PKCS7Padding).
     * @return Encryption cipher transformation.
     */
    public String getCipherTransformation() {
        return cipherTransformation;
    }

    /**
     * Get Base64 encoded password encryption key.
     * @return Base64 encoded password encryption key.
     */
    public String getPasswordEncryptionKey() {
        return passwordEncryptionKey;
    }

    /**
     * Get delay for resending SMS in milliseconds.
     * @return Delay for resending SMS in milliseconds.
     */
    public int getSmsResendDelay() {
        return smsResendDelay;
    }

    /**
     * Set delay for resending SMS in milliseconds.
     * @param smsResendDelay Delay for resending SMS in milliseconds.
     */
    public void setSmsResendDelay(int smsResendDelay) {
        this.smsResendDelay = smsResendDelay;
    }

    /**
     * Get delay for showing timeout warning in milliseconds.
     * @return Delay for showing timeout warning in milliseconds.
     */
    public int getTimeoutWarningDelay() {
        return timeoutWarningDelay;
    }

    /**
     * Set delay for showing timeout warning in milliseconds.
     * @param timeoutWarningDelay Delay for showing timeout warning in milliseconds.
     */
    public void setTimeoutWarningDelay(int timeoutWarningDelay) {
        this.timeoutWarningDelay = timeoutWarningDelay;
    }

    /**
     * Get whether anti-fraud system integration is enabled.
     * @return Whether anti-fraud system integration is enabled.
     */
    public boolean isAfsEnabled() {
        return afsEnabled;
    }

    /**
     * Get anti-fraud system type.
     * @return Anti-fraud system type.
     */
    public AfsType getAfsType() {
        return afsType;
    }

    /**
     * Get whether client IP address is detected in anti-fraud system integration.
     * @return Whether client IP address is detected in anti-fraud system integration.
     */
    public boolean isAfsIpAddressDetectionEnabled() {
        return afsIpAddressDetectionEnabled;
    }

    /**
     * Get whether anti-fraud system requires IPv4 addresses.
     * @return Whether anti-fraud system requires IPv4 addresses.
     */
    public boolean isAfsIpv4Forced() {
        return afsIpv4Forced;
    }

    /**
     * Get name of tm_device_tag cookie for Threat Mark AFS integration.
     * @return Name of tm_device_tag cookie for Threat Mark AFS integration.
     */
    public String getTmDeviceTagCookie() {
        return tmDeviceTagCookie;
    }

    /**
     * Get name of tm_session_sid cookie for Threat Mark AFS integration.
     * @return Name of tm_session_sid cookie for Threat Mark AFS integration.
     */
    public String getTmSessionSidCookie() {
        return tmSessionSidCookie;
    }

    /**
     * Get whether authentication using client TLS certificate is enabled.
     * @return Whether authentication using client TLS certificate is enabled.
     */
    public boolean isClientCertificateAuthenticationEnabled() {
        return clientCertificateAuthenticationEnabled;
    }

    /**
     * Get client TLS certificate verification URL for login.
     * @return Client TLS certificate verification URL.
     */
    public String getCertificateVerificationUrlForLogin() {
        return certificateVerificationUrlForLogin;
    }

    /**
     * Get client TLS certificate verification URL for approval.
     * @return Client TLS certificate verification URL for approval.
     */
    public String getCertificateVerificationUrlForApproval() {
        return certificateVerificationUrlForApproval;
    }

}
