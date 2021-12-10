/*
 * PowerAuth Web Flow and related software components
 * Copyright (C) 2020 Wultra s.r.o.
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

package io.getlime.security.powerauth.app.tppengine.configuration;

import com.wultra.core.audit.base.Audit;
import com.wultra.core.audit.base.AuditFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
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

    private final AuditFactory auditFactory;

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
     * Configuration constructor.
     * @param auditFactory Audit factory.
     */
    @Autowired
    public TppEngineConfiguration(AuditFactory auditFactory) {
        this.auditFactory = auditFactory;
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
     * Get default app access token validity in seconds.
     * @return Access token validity in seconds.
     */
    public Long getDefaultAccessTokenValidityInSeconds() {
        return defaultAccessTokenValidityInSeconds;
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
