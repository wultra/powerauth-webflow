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

package io.getlime.security.powerauth.app.webflow.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for the Web Flow application.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Configuration
@ConfigurationProperties("ext")
@ComponentScan(basePackages = {"io.getlime.security.powerauth"})
public class WebFlowServerConfiguration {

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
}
