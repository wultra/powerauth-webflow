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
package io.getlime.security.powerauth.app.nextstep.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration of Next Step server.
 *
 * @author Roman Strobl
 */
@Configuration
@ComponentScan(basePackages = {"io.getlime.security.powerauth"})
public class NextStepServerConfiguration {

    /**
     * Operation expiration time in seconds.
     */
    @Value("${powerauth.nextstep.operation.expirationTimeInSeconds}")
    private int operationExpirationTime;

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
