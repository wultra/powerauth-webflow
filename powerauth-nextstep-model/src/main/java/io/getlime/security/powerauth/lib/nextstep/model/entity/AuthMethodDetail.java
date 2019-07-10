package io.getlime.security.powerauth.lib.nextstep.model.entity;

import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthMethod;

/**
 * Class represents details of an authentication method.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class AuthMethodDetail {

    private AuthMethod authMethod;
    private Boolean hasUserInterface;
    private String displayNameKey;
    private Boolean hasMobileToken;

    /**
     * Get authentication method.
     * @return Authentication method.
     */
    public AuthMethod getAuthMethod() {
        return authMethod;
    }

    /**
     * Set authentication method.
     * @param authMethod Authentication method.
     */
    public void setAuthMethod(AuthMethod authMethod) {
        this.authMethod = authMethod;
    }

    /**
     * Get whether authentication method has user interface.
     * @return Whether authentication method has user interface.
     */
    public Boolean getHasUserInterface() {
        return hasUserInterface;
    }

    /**
     * Set whether authentication method has user interface.
     * @param hasUserInterface Whether authentication method has user interface.
     */
    public void setHasUserInterface(Boolean hasUserInterface) {
        this.hasUserInterface = hasUserInterface;
    }

    /**
     * Get display name key for localization.
     * @return Display name key for localization.
     */
    public String getDisplayNameKey() {
        return displayNameKey;
    }

    /**
     * Set display name key for localization.
     * @param displayNameKey Display name key for localization.
     */
    public void setDisplayNameKey(String displayNameKey) {
        this.displayNameKey = displayNameKey;
    }

    /**
     * Get whether authentication method is compatible with mobile token.
     * @return Whether authentication method is compatible with mobile token.
     */
    public Boolean getHasMobileToken() {
        return hasMobileToken;
    }

    /**
     * Set whether authentication method is compatible with mobile token.
     * @param hasMobileToken Whether authentication method is compatible with mobile token.
     */
    public void setHasMobileToken(Boolean hasMobileToken) {
        this.hasMobileToken = hasMobileToken;
    }
}
