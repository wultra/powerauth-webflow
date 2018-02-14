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

import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthMethod;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Entity which stores configuration of authentication methods.
 *
 * @author Roman Strobl
 */
@Entity
@Table(name = "ns_auth_method")
public class AuthMethodEntity implements Serializable {

    private static final long serialVersionUID = -2015768978885351433L;

    @Id
    @Column(name = "auth_method")
    @Enumerated(EnumType.STRING)
    private AuthMethod authMethod;

    @Column(name = "order_number")
    private Long orderNumber;

    @Column(name = "check_user_prefs")
    private Boolean checkUserPrefs;

    @Column(name = "user_prefs_column")
    private Integer userPrefsColumn;

    @Column(name = "user_prefs_default")
    private Boolean userPrefsDefault;

    @Column(name = "check_auth_fails")
    private Boolean checkAuthorizationFailures;

    @Column(name = "max_auth_fails")
    private Integer maxAuthorizationFailures;

    @Column(name = "has_user_interface")
    private Boolean hasUserInterface;

    @Column(name = "display_name_key")
    private String displayNameKey;

    public AuthMethod getAuthMethod() {
        return authMethod;
    }

    public void setAuthMethod(AuthMethod authMethod) {
        this.authMethod = authMethod;
    }

    public Long getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(Long orderNumber) {
        this.orderNumber = orderNumber;
    }

    public Boolean getCheckUserPrefs() {
        return checkUserPrefs;
    }

    public void setCheckUserPrefs(Boolean checkUserPrefs) {
        this.checkUserPrefs = checkUserPrefs;
    }

    public Integer getUserPrefsColumn() {
        return userPrefsColumn;
    }

    public void setUserPrefsColumn(Integer userPrefsColumn) {
        this.userPrefsColumn = userPrefsColumn;
    }

    public Boolean getUserPrefsDefault() {
        return userPrefsDefault;
    }

    public void setUserPrefsDefault(Boolean userPrefsDefault) {
        this.userPrefsDefault = userPrefsDefault;
    }

    public Boolean getCheckAuthorizationFailures() {
        return checkAuthorizationFailures;
    }

    public void setCheckAuthorizationFailures(Boolean checkAuthorizationFailures) {
        this.checkAuthorizationFailures = checkAuthorizationFailures;
    }

    public Integer getMaxAuthorizationFailures() {
        return maxAuthorizationFailures;
    }

    public void setMaxAuthorizationFailures(Integer maxAuthorizationFailures) {
        this.maxAuthorizationFailures = maxAuthorizationFailures;
    }

    public Boolean getHasUserInterface() {
        return hasUserInterface;
    }

    public void setHasUserInterface(Boolean hasUserInterface) {
        this.hasUserInterface = hasUserInterface;
    }

    public String getDisplayNameKey() {
        return displayNameKey;
    }

    public void setDisplayNameKey(String displayNameKey) {
        this.displayNameKey = displayNameKey;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AuthMethodEntity that = (AuthMethodEntity) o;

        return authMethod == that.authMethod;
    }

    @Override
    public int hashCode() {
        return authMethod != null ? authMethod.hashCode() : 0;
    }

}
