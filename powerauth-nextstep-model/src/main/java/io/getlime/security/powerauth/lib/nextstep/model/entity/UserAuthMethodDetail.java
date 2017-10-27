package io.getlime.security.powerauth.lib.nextstep.model.entity;

import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthMethod;

/**
 * Class represents state of an authentication method for given user.
 *
 * @author Roman Strobl, roman.strobl@lime-company.eu
 */
public class UserAuthMethodDetail {

    private String userId;
    private AuthMethod authMethod;
    private Boolean hasUserInterface;
    private String displayNameKey;
    private String config;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public AuthMethod getAuthMethod() {
        return authMethod;
    }

    public void setAuthMethod(AuthMethod authMethod) {
        this.authMethod = authMethod;
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

    public String getConfig() {
        return config;
    }

    public void setConfig(String config) {
        this.config = config;
    }

}
