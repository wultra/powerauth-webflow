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
package io.getlime.security.powerauth.lib.nextstep.model.entity;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration of authentication methods per user. Configuration parameters are stored in a key-value Map.
 *
 * @author Roman Strobl, roman.strobl@lime-company.eu
 */
public class AuthMethodConfiguration {

    private Map<String, String> parameters = new HashMap<>();

    /**
     * Get all configuration parameters.
     * @return Map with configuration parameters.
     */
    public Map<String, String> getParameters() {
        return parameters;
    }

    /**
     * Get value of a configuration parameter.
     * @param parameterName Parameter name.
     * @return Parameter value.
     */
    public String getParameterValue(String parameterName) {
        return parameters.get(parameterName);
    }

    /**
     * Set value of a configuration parameter.
     * @param parameterName Parameter name.
     * @param parameterValue Parameter value.
     */
    public void setParameterValue(String parameterName, String parameterValue) {
        parameters.put(parameterName, parameterValue);
    }
}
