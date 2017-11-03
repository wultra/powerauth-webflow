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
package io.getlime.security.powerauth.lib.nextstep.model.request;

import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthMethod;

import java.util.Map;

/**
 * Request object used for updating authentication methods for user.
 *
 * @author Roman Strobl, roman.strobl@lime-company.eu
 */
public class UpdateAuthMethodRequest {

    private String userId;
    private AuthMethod authMethod;
    private Map<String, String> config;

    /**
     * Get the user ID.
     *
     * @return User ID.
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Set the user ID.
     *
     * @param userId User ID.
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * Get the authentication method.
     *
     * @return Authentication method.
     */
    public AuthMethod getAuthMethod() {
        return authMethod;
    }

    /**
     * Set the authentication method.
     *
     * @param authMethod Authentication method.
     */
    public void setAuthMethod(AuthMethod authMethod) {
        this.authMethod = authMethod;
    }

    /**
     * Get the authentication method configuration.
     * @return Configuration.
     */
    public Map<String, String> getConfig() {
        return config;
    }

    /**
     * Set the authentication method configuration.
     * @param config Configuration.
     */
    public void setConfig(Map<String, String> config) {
        this.config = config;
    }
}
