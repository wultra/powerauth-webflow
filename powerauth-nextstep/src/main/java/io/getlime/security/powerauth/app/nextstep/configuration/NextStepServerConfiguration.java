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
package io.getlime.security.powerauth.app.nextstep.configuration;

import io.getlime.security.powerauth.lib.dataadapter.client.DataAdapterClient;
import io.getlime.security.powerauth.lib.dataadapter.client.DataAdapterClientErrorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration of Next Step server.
 *
 * @author Roman Strobl
 */
@Configuration
@ConfigurationProperties("ext")
@ComponentScan(basePackages = {"io.getlime.security.powerauth"})
public class NextStepServerConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(NextStepServerConfiguration.class);

    /**
     * Data Adapter service URL.
     */
    @Value("${powerauth.dataAdapter.service.url}")
    private String dataAdapterServiceUrl;

    /**
     * Operation expiration time in seconds.
     */
    @Value("${powerauth.nextstep.operation.expirationTimeInSeconds}")
    private int operationExpirationTime;

    @Value("${powerauth.nextstep.identity.credential.useOriginalUsername}")
    private boolean useOriginalUsername;

    @Value("${powerauth.nextstep.identity.credential.generateUsernameMaxAttempts}")
    private int generateUsernameMaxAttempts;

    /**
     * Application name.
     */
    @Value("${powerauth.nextstep.service.applicationName}")
    private String applicationName;

    /**
     * Application display name.
     */
    @Value("${powerauth.nextstep.service.applicationDisplayName}")
    private String applicationDisplayName;

    /**
     * Application environment.
     */
    @Value("${powerauth.nextstep.service.applicationEnvironment}")
    private String applicationEnvironment;

    /**
     * Get the operation expiration time.
     *
     * @return expiration time for operations in seconds
     */
    public int getOperationExpirationTime() {
        return operationExpirationTime;
    }

    /**
     * Get whether original username for a removed credential when the credential is recreated.
     * @return Whether original username for a removed credential when the credential is recreated.
     */
    public boolean isUseOriginalUsername() {
        return useOriginalUsername;
    }

    /**
     * Get maximum number of attempts when generating username.
     * @return Maximum number of attempts when generating username.
     */
    public int getGenerateUsernameMaxAttempts() {
        return generateUsernameMaxAttempts;
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
     * Default data adapter client.
     *
     * @return Data adapter client.
     */
    @Bean
    public DataAdapterClient defaultDataAdapterClient() {
        try {
            return new DataAdapterClient(dataAdapterServiceUrl);
        } catch (DataAdapterClientErrorException ex) {
            logger.error(ex.getMessage(), ex);
            return null;
        }
    }

}
