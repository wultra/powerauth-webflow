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
package io.getlime.security.powerauth.app.nextstep.repository.model.entity;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Entity which stores user preferences for various authentication methods.
 *
 * @author Roman Strobl
 */
@Entity
@Table(name = "ns_user_prefs")
@NamedQueries({
        @NamedQuery(name = "UserPrefsEntity.findUserPrefs", query = "SELECT p FROM UserPrefsEntity p WHERE p.userId=?1")
})
public class UserPrefsEntity implements Serializable {

    private static final long serialVersionUID = -7165002311514127800L;

    @Id
    @Column(name = "user_id")
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

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Boolean getAuthMethod1Enabled() {
        return authMethod1Enabled;
    }

    public void setAuthMethod1Enabled(Boolean authMethod1Enabled) {
        this.authMethod1Enabled = authMethod1Enabled;
    }

    public Boolean getAuthMethod2Enabled() {
        return authMethod2Enabled;
    }

    public void setAuthMethod2Enabled(Boolean authMethod2Enabled) {
        this.authMethod2Enabled = authMethod2Enabled;
    }

    public Boolean getAuthMethod3Enabled() {
        return authMethod3Enabled;
    }

    public void setAuthMethod3Enabled(Boolean authMethod3Enabled) {
        this.authMethod3Enabled = authMethod3Enabled;
    }

    public Boolean getAuthMethod4Enabled() {
        return authMethod4Enabled;
    }

    public void setAuthMethod4Enabled(Boolean authMethod4Enabled) {
        this.authMethod4Enabled = authMethod4Enabled;
    }

    public Boolean getAuthMethod5Enabled() {
        return authMethod5Enabled;
    }

    public void setAuthMethod5Enabled(Boolean authMethod5Enabled) {
        this.authMethod5Enabled = authMethod5Enabled;
    }

    /**
     * Get the status of an authentication method given the column number in user preferences.
     *
     * @param columnNumber column number with authentication method
     * @return true if enabled, false if disabled, null if unspecified
     */
    public Boolean getAuthMethodEnabled(int columnNumber) {
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
                throw new IllegalStateException("Unexpected column number for authentication method: " + columnNumber);
        }
    }

    /**
     * Set the status of an authentication method given the column number in user preferences.
     *
     * @param columnNumber column number with authentication method
     * @param enabled      true if enabled, false if disabled, null if unspecified
     */
    public void setAuthMethodEnabled(int columnNumber, Boolean enabled) {
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
                throw new IllegalStateException("Unexpected column number for authentication method: " + columnNumber);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserPrefsEntity that = (UserPrefsEntity) o;

        return userId != null ? userId.equals(that.userId) : that.userId == null;
    }

    @Override
    public int hashCode() {
        return userId != null ? userId.hashCode() : 0;
    }
}
