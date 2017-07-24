package io.getlime.security.powerauth.lib.nextstep.model.entity;

import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthMethod;

/**
 * @author Roman Strobl, roman.strobl@lime-company.eu
 */
public class AuthMethodDetail {

    private AuthMethod authMethod;
    private Boolean hasUserInterface;
    private String displayNameKey;

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
}
