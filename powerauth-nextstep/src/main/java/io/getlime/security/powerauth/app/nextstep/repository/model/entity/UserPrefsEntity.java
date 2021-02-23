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
package io.getlime.security.powerauth.app.nextstep.repository.model.entity;

import io.getlime.security.powerauth.lib.nextstep.model.exception.InvalidConfigurationException;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * Entity which stores user preferences for various authentication methods.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Entity
@Table(name = "ns_user_prefs")
@Data
@EqualsAndHashCode(of = "userId")
public class UserPrefsEntity implements Serializable {

    private static final long serialVersionUID = -7165002311514127800L;

    @Id
    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "auth_method_1")
    private Boolean authMethod1Enabled;

    @Column(name = "auth_method_2")
    private Boolean authMethod2Enabled;

    @Column(name = "auth_method_3")
    private Boolean authMethod3Enabled;

    @Column(name = "auth_method_4")
    private Boolean authMethod4Enabled;

    @Column(name = "auth_method_5")
    private Boolean authMethod5Enabled;

    @Column(name = "auth_method_1_config")
    private String authMethod1Config;

    @Column(name = "auth_method_2_config")
    private String authMethod2Config;

    @Column(name = "auth_method_3_config")
    private String authMethod3Config;

    @Column(name = "auth_method_4_config")
    private String authMethod4Config;

    @Column(name = "auth_method_5_config")
    private String authMethod5Config;

    /**
     * Get the status of an authentication method given the column number in user preferences.
     *
     * @param columnNumber Column number with authentication method.
     * @return True if enabled, false if disabled, null if unspecified.
     * @throws InvalidConfigurationException Thrown when Next Step configuration is invalid.
     */
    public Boolean getAuthMethodEnabled(int columnNumber) throws InvalidConfigurationException {
        switch (columnNumber) {
            case 1:
                return getAuthMethod1Enabled();
            case 2:
                return getAuthMethod2Enabled();
            case 3:
                return getAuthMethod3Enabled();
            case 4:
                return getAuthMethod4Enabled();
            case 5:
                return getAuthMethod5Enabled();
            default:
                throw new InvalidConfigurationException("Unexpected column number for authentication method: " + columnNumber);
        }
    }

    /**
     * Set the status of an authentication method given the column number in user preferences.
     *
     * @param columnNumber column number with authentication method
     * @param enabled      true if enabled, false if disabled, null if unspecified
     * @throws InvalidConfigurationException Thrown when Next Step configuration is invalid.
     */
    public void setAuthMethodEnabled(int columnNumber, Boolean enabled) throws InvalidConfigurationException {
        switch (columnNumber) {
            case 1:
                setAuthMethod1Enabled(enabled);
                break;
            case 2:
                setAuthMethod2Enabled(enabled);
                break;
            case 3:
                setAuthMethod3Enabled(enabled);
                break;
            case 4:
                setAuthMethod4Enabled(enabled);
                break;
            case 5:
                setAuthMethod5Enabled(enabled);
                break;
            default:
                throw new InvalidConfigurationException("Unexpected column number for authentication method: " + columnNumber);
        }
    }

    public String getAuthMethodConfig(int columnNumber) throws InvalidConfigurationException {
        switch (columnNumber) {
            case 1:
                return getAuthMethod1Config();
            case 2:
                return getAuthMethod2Config();
            case 3:
                return getAuthMethod3Config();
            case 4:
                return getAuthMethod4Config();
            case 5:
                return getAuthMethod5Config();
            default:
                throw new InvalidConfigurationException("Unexpected column number for authentication method: " + columnNumber);
        }
    }

    public void setAuthMethodConfig(int columnNumber, String config) throws InvalidConfigurationException {
        switch (columnNumber) {
            case 1:
                setAuthMethod1Config(config);
                break;
            case 2:
                setAuthMethod2Config(config);
                break;
            case 3:
                setAuthMethod3Config(config);
                break;
            case 4:
                setAuthMethod4Config(config);
                break;
            case 5:
                setAuthMethod5Config(config);
                break;
            default:
                throw new InvalidConfigurationException("Unexpected column number for authentication method: " + columnNumber);
        }

    }

}
