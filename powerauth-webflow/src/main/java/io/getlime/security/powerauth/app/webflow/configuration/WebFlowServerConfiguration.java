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

package io.getlime.security.powerauth.app.webflow.configuration;

import com.wultra.core.audit.base.Audit;
import com.wultra.core.audit.base.AuditFactory;
import io.getlime.security.powerauth.lib.webflow.authentication.model.enumeration.CertificateSigner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Configuration for the Web Flow application.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Configuration
@ConfigurationProperties("ext")
@ComponentScan(basePackages = {"io.getlime.security.powerauth"})
public class WebFlowServerConfiguration {

    private final AuditFactory auditFactory;

    /**
     * Dynamic URL for external CSS stylesheet.
     */
    @Value("${powerauth.webflow.page.custom-css.url}")
    private String customStyleSheetUrl;

    /**
     * External location for the resources.
     * Note: Default value is "classpath:/static/resources/.
     */
    @Value("${powerauth.webflow.page.ext-resources.location}")
    private String resourcesLocation;

    /**
     * Cache duration for the resources.
     */
    @Value("${powerauth.webflow.page.ext-resources.cache-duration:1h}")
    private Duration resourcesCacheDuration;

    /**
     * Dynamic page title.
     */
    @Value("${powerauth.webflow.page.title}")
    private String pageTitle;

    /**
     * Application name.
     */
    @Value("${powerauth.webflow.service.applicationName}")
    private String applicationName;

    /**
     * Application display name.
     */
    @Value("${powerauth.webflow.service.applicationDisplayName}")
    private String applicationDisplayName;

    /**
     * Application environment.
     */
    @Value("${powerauth.webflow.service.applicationEnvironment}")
    private String applicationEnvironment;

    /**
     * Whether security warning should be displayed on Android devices.
     */
    @Value("${powerauth.webflow.android.showSecurityWarning}")
    private boolean showAndroidSecurityWarning;

    /**
     * Whether anti-fraud system integration is enabled.
     */
    @Value("${powerauth.webflow.afs.enabled:false}")
    private boolean afsEnabled;

    /**
     * Maximum length of username.
     */
    @Value("${powerauth.webflow.input.username.maxLength:256}")
    private int usernameMaxLength;

    /**
     * Maximum length of user password.
     */
    @Value("${powerauth.webflow.input.password.maxLength:256}")
    private int passwordMaxLength;

    /**
     * Maximum length of SMS authorization code.
     */
    @Value("${powerauth.webflow.input.smsOtp.maxLength:8}")
    private int smsOtpMaxLength;

    /**
     * Whether limit for large consent panel is enabled.
     */
    @Value("${powerauth.webflow.consent.limit.enabled:false}")
    private boolean consentPanelLimitEnabled;

    /**
     * Configuration of limit for large consent panel in number of characters.
     */
    @Value("${powerauth.webflow.consent.limit.characters:750}")
    private int consentPanelLimitCharacters;

    /**
     * Whether certificate is enabled during approval.
     */
    @Value("${powerauth.webflow.approval.certificate.enabled:false}")
    private boolean approvalCertificateEnabled;

    /**
     * Whether certificate is enabled during approval.
     */
    @Value("${powerauth.webflow.approval.certificate.signer:ICA_CLIENT_SIGN}")
    private CertificateSigner approvalCertificateSigner;

    /**
     * ICA configuration URL.
     */
    @Value("${powerauth.webflow.approval.certificate.signer.ica.configurationUrl:}")
    private String icaConfigurationUrl;

    /**
     * ICA extension log level.
     */
    @Value("${powerauth.webflow.approval.certificate.signer.ica.logLevel:1}")
    private String icaLogLevel;

    /**
     * ICA extension owner name.
     */
    @Value("${powerauth.webflow.approval.certificate.signer.ica.extensionOwner:}")
    private String icaExtensionOwner;

    /**
     * ICA extension ID for Google Chrome.
     */
    @Value("${powerauth.webflow.approval.certificate.signer.ica.extensionIDChrome:}")
    private String icaExtensionIDChrome;

    /**
     * ICA extension ID for Opera.
     */
    @Value("${powerauth.webflow.approval.certificate.signer.ica.extensionIDOpera:}")
    private String icaExtensionIDOpera;

    /**
     * ICA extension ID for Microsoft Edge.
     */
    @Value("${powerauth.webflow.approval.certificate.signer.ica.extensionIDEdge:}")
    private String icaExtensionIDEdge;

    /**
     * ICA extension ID for Firefox.
     */
    @Value("${powerauth.webflow.approval.certificate.signer.ica.extensionIDFirefox:}")
    private String icaExtensionIDFirefox;

    /**
     * ICA extension ID for Firefox.
     */
    @Value("${powerauth.webflow.approval.certificate.signer.ica.extensionInstallURLFirefox:}")
    private String icaExtensionInstallURLFirefox;

    /**
     * WebSocket support configuration.
     */
    @Value("${powerauth.webflow.websockets.enabled:true}")
    private boolean webSocketSupportEnabled;

    /**
     * Configuration constructor.
     * @param auditFactory Audit factory.
     */
    @Autowired
    public WebFlowServerConfiguration(AuditFactory auditFactory) {
        this.auditFactory = auditFactory;
    }

    /**
     * Get custom external stylesheet URL.
     *
     * @return External stylesheet URL.
     */
    public String getCustomStyleSheetUrl() {
        return customStyleSheetUrl;
    }

    /**
     * Get the dynamic location for the resources to allow external styling and localization of Web Flow UI.
     *
     * @return Dynamic location for the resources. By default, "classpath:/static/resources/".
     */
    public String getResourcesLocation() {
        return resourcesLocation;
    }

    /**
     * Get the cache duration for the resources.
     *
     * @return Cache duration for the resources.
     */
    public Duration getResourcesCacheDuration() {
        return resourcesCacheDuration;
    }

    /**
     * Get dynamic page title.
     *
     * @return Page title.
     */
    public String getPageTitle() {
        return pageTitle;
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

    /**
     * Get whether security warning should be displayed on Android devices.
     * @return Whether security warning should be displayed on Android devices.
     */
    public boolean getShowAndroidSecurityWarning() {
        return showAndroidSecurityWarning;
    }

    /**
     * Get whether anti-fraud system integration is enabled.
     * @return Whether anti-fraud system integration is enabled.
     */
    public boolean isAfsEnabled() {
        return afsEnabled;
    }

    /**
     * Get maximum length of username.
     * @return Maximum length of username.
     */
    public int getUsernameMaxLength() {
        return usernameMaxLength;
    }

    /**
     * Get maximum length of password.
     * @return Maximum length of password.
     */
    public int getPasswordMaxLength() {
        return passwordMaxLength;
    }

    /**
     * Get maximum length of SMS authorization code.
     * @return Maximum length of SMS authorization code.
     */
    public int getSmsOtpMaxLength() {
        return smsOtpMaxLength;
    }

    /**
     * Get whether limit for large consent panel is enabled.
     * @return Whether limit for large consent panel is enabled.
     */
    public boolean isConsentPanelLimitEnabled() {
        return consentPanelLimitEnabled;
    }

    /**
     * Get limit of characters for displaying large consent panel.
     * @return Limit of characters for displaying large consent panel.
     */
    public int getConsentPanelLimitCharacters() {
        return consentPanelLimitCharacters;
    }

    /**
     * Get whether approval using certificate is enabled.
     * @return Whether approval using certificate is enabled.
     */
    public boolean isApprovalCertificateEnabled() {
        return approvalCertificateEnabled;
    }

    /**
     * Get signer for approval using certificate.
     * @return Signer for approval using certificate.
     */
    public CertificateSigner getApprovalCertificateSigner() {
        return approvalCertificateSigner;
    }

    /**
     * Get ICA configuration URL.
     * @return ICA configuration URL.
     */
    public String getIcaConfigurationUrl() {
        return icaConfigurationUrl;
    }

    /**
     * Get ICA extension log level.
     * @return ICA extension log level.
     */
    public String getIcaLogLevel() {
        return icaLogLevel;
    }

    /**
     * Get ICA extension owner name.
     * @return ICA extension owner name.
     */
    public String getIcaExtensionOwner() {
        return icaExtensionOwner;
    }

    /**
     * Get ICA extension for Google Chrome.
     * @return ICA extension for Google Chrome.
     */
    public String getIcaExtensionIDChrome() {
        return icaExtensionIDChrome;
    }

    /**
     * Get ICA extension for Opera.
     * @return ICA extension for Opera.
     */
    public String getIcaExtensionIDOpera() {
        return icaExtensionIDOpera;
    }

    /**
     * Get ICA extension for Microsoft Edge.
     * @return ICA extension for Microsoft Edge.
     */
    public String getIcaExtensionIDEdge() {
        return icaExtensionIDEdge;
    }

    /**
     * Get ICA extension for Firefox.
     * @return ICA extension for Firefox.
     */
    public String getIcaExtensionIDFirefox() {
        return icaExtensionIDFirefox;
    }

    /**
     * Get ICA extension for Firefox installation URL.
     * @return ICA extension for Firefox installation URL.
     */
    public String getIcaExtensionInstallURLFirefox() {
        return icaExtensionInstallURLFirefox;
    }

    /**
     * Get whether WebSocket support is enabled.
     * @return Whether WebSocket support is enabled.
     */
    public boolean isWebSocketSupportEnabled() {
        return webSocketSupportEnabled;
    }

    /**
     * Prepare audit interface.
     * @return Audit interface.
     */
    @Bean
    public Audit audit() {
        return auditFactory.getAudit();
    }
}
