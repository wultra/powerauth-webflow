/*
 * PowerAuth Web Flow and related software components
 * Copyright (C) 2017 Wultra s.r.o.
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

package io.getlime.security.powerauth.lib.webflow.authentication.configuration;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.wultra.core.rest.client.base.RestClientConfiguration;
import io.getlime.security.powerauth.lib.dataadapter.client.DataAdapterClient;
import io.getlime.security.powerauth.lib.dataadapter.client.DataAdapterClientErrorException;
import io.getlime.security.powerauth.lib.dataadapter.model.enumeration.AfsType;
import io.getlime.security.powerauth.lib.dataadapter.model.enumeration.PasswordProtectionType;
import io.getlime.security.powerauth.lib.nextstep.client.NextStepClient;
import io.getlime.security.powerauth.lib.nextstep.client.NextStepClientException;
import io.getlime.security.powerauth.lib.webflow.authentication.service.SSLConfigurationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(WebFlowServicesConfiguration.class);

    private final SSLConfigurationService sslConfigurationService;

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
     * Whether PowerAuth operations support is enabled.
     */
    @Value("${powerauth.webflow.pa.operations.enabled}")
    private boolean powerAuthOperationSupportEnabled;

    /**
     * WebSocket support configuration.
     */
    @Value("${powerauth.webflow.websockets.enabled:true}")
    private boolean webSocketSupportEnabled;

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
     * Whether authentication with temporary credentials is allowed in Web Flow.
     */
    @Value("${powerauth.webflow.authentication.allowTemporaryCredentials:false}")
    private boolean authenticationWithTemporaryCredentialsAllowed;

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

    @Autowired
    public WebFlowServicesConfiguration(SSLConfigurationService sslConfigurationService) {
        this.sslConfigurationService = sslConfigurationService;
    }

    /**
     * Construct jackson configuration which allows sending empty objects and allows unknown properties.
     * @return jackson configuration
     */
    private static RestClientConfiguration.JacksonConfiguration createJacksonConfiguration() {
        final RestClientConfiguration.JacksonConfiguration jacksonConfiguration = new RestClientConfiguration.JacksonConfiguration();
        jacksonConfiguration.getSerialization().put(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        jacksonConfiguration.getDeserialization().put(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return jacksonConfiguration;
    }

    /**
     * Default data adapter client.
     *
     * @return Data adapter client.
     */
    @Bean
    public DataAdapterClient defaultDataAdapterClient() {
        RestClientConfiguration restClientConfiguration = new RestClientConfiguration();
        restClientConfiguration.setBaseUrl(dataAdapterServiceUrl);
        restClientConfiguration.setAcceptInvalidSslCertificate(acceptInvalidSslCertificate);
        restClientConfiguration.setJacksonConfiguration(createJacksonConfiguration());
        try {
            return new DataAdapterClient(restClientConfiguration);
        } catch (DataAdapterClientErrorException ex) {
            logger.error(ex.getMessage(), ex);
            return null;
        }
    }

    /**
     * Default Next Step service client.
     *
     * @return Next Step service client.
     */
    @Bean
    public NextStepClient defaultNextStepClient() {
        RestClientConfiguration restClientConfiguration = new RestClientConfiguration();
        restClientConfiguration.setBaseUrl(nextstepServiceUrl);
        restClientConfiguration.setAcceptInvalidSslCertificate(acceptInvalidSslCertificate);
        restClientConfiguration.setJacksonConfiguration(createJacksonConfiguration());
        try {
            return new NextStepClient(restClientConfiguration);
        } catch (NextStepClientException ex) {
            logger.error(ex.getMessage(), ex);
            return null;
        }
    }

    /**
     * Whether offline mode is available.
     * @return True if offline mode is available.
     */
    public boolean isOfflineModeAvailable() {
        return offlineModeAvailable;
    }

    /**
     * Whether PowerAuth operations support is enabled.
     * @return Whether PowerAuth operations support is enabled.
     */
    public boolean isPowerAuthOperationSupportEnabled() {
        return powerAuthOperationSupportEnabled;
    }

    /**
     * Get whether WebSocket support is enabled.
     * @return Whether WebSocket support is enabled.
     */
    public boolean isWebSocketSupportEnabled() {
        return webSocketSupportEnabled;
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
     * Get whether authentication with temporary credentials is allowed.
     * @return Whether authentication with temporary credentials is allowed.
     */
    public boolean isAuthenticationWithTemporaryCredentialsAllowed() {
        return authenticationWithTemporaryCredentialsAllowed;
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

}
