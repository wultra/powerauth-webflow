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
package io.getlime.security.powerauth.app.nextstep.repository.model.entity;

import io.getlime.security.powerauth.lib.nextstep.model.exception.InvalidConfigurationException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
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

    @Serial
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
        return switch (columnNumber) {
            case 1 -> getAuthMethod1Enabled();
            case 2 -> getAuthMethod2Enabled();
            case 3 -> getAuthMethod3Enabled();
            case 4 -> getAuthMethod4Enabled();
            case 5 -> getAuthMethod5Enabled();
            default -> throw new InvalidConfigurationException("Unexpected column number for authentication method: " + columnNumber);
        };
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
            case 1 -> setAuthMethod1Enabled(enabled);
            case 2 -> setAuthMethod2Enabled(enabled);
            case 3 -> setAuthMethod3Enabled(enabled);
            case 4 -> setAuthMethod4Enabled(enabled);
            case 5 -> setAuthMethod5Enabled(enabled);
            default -> throw new InvalidConfigurationException("Unexpected column number for authentication method: " + columnNumber);
        }
    }

    /**
     * Get authentication method configuration.
     * @param columnNumber Column number with authentication method.
     * @return Authentication method configuration.
     * @throws InvalidConfigurationException Thrown when Next Step configuration is invalid.
     */
    public String getAuthMethodConfig(int columnNumber) throws InvalidConfigurationException {
        return switch (columnNumber) {
            case 1 -> getAuthMethod1Config();
            case 2 -> getAuthMethod2Config();
            case 3 -> getAuthMethod3Config();
            case 4 -> getAuthMethod4Config();
            case 5 -> getAuthMethod5Config();
            default -> throw new InvalidConfigurationException("Unexpected column number for authentication method: " + columnNumber);
        };
    }

    /**
     * Set authentication method configuration.
     * @param columnNumber Column number with authentication method.
     * @param config Authentication method configuration.
     * @throws InvalidConfigurationException Thrown when Next Step configuration is invalid.
     */
    public void setAuthMethodConfig(int columnNumber, String config) throws InvalidConfigurationException {
        switch (columnNumber) {
            case 1 -> setAuthMethod1Config(config);
            case 2 -> setAuthMethod2Config(config);
            case 3 -> setAuthMethod3Config(config);
            case 4 -> setAuthMethod4Config(config);
            case 5 -> setAuthMethod5Config(config);
            default -> throw new InvalidConfigurationException("Unexpected column number for authentication method: " + columnNumber);
        }

    }

}
