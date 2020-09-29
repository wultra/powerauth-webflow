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

package io.getlime.security.powerauth.app.tppengine.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for the TPP Engine application.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Configuration
@ConfigurationProperties("ext")
@ComponentScan(basePackages = {"io.getlime.security.powerauth"})
public class TppEngineConfiguration {

    /**
     * Application name.
     */
    @Value("${powerauth.tppEngine.service.applicationName}")
    private String applicationName;

    /**
     * Application display name.
     */
    @Value("${powerauth.tppEngine.service.applicationDisplayName}")
    private String applicationDisplayName;

    /**
     * Application environment.
     */
    @Value("${powerauth.tppEngine.service.applicationEnvironment}")
    private String applicationEnvironment;

    /**
     * When a new app is created in TPP engine, this value is set as the default
     * access token validity in seconds.
     */
    @Value("${powerauth.tppEngine.service.oauth2.defaultAccessTokenValidityInSeconds}")
    private Long defaultAccessTokenValidityInSeconds;

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
     * Get default app access token validity in seconds.
     * @return Access token validity in seconds.
     */
    public Long getDefaultAccessTokenValidityInSeconds() {
        return defaultAccessTokenValidityInSeconds;
    }

}
