package io.getlime.security.powerauth.lib.webflow.authentication.sms.model.response;

import io.getlime.security.powerauth.lib.webflow.authentication.base.AuthStepResponse;

/**
 * Response for init SMS authorization.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class InitSmsAuthorizationResponse extends AuthStepResponse {

    private String username;
    private boolean passwordEnabled;
    private int resendDelay;

    /**
     * Get username for SMS authorization combined with password (optional).
     * @return Username for SMS authorization combined with password (optional).
     */
    public String getUsername() {
        return username;
    }

    /**
     * Set username for SMS authorization combined with password (optional).
     * @param username Username for SMS authorization combined with password (optional).
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Get whether password is enabled for SMS authorization combined with password.
     * @return Whether password is enabled for SMS authorization combined with password.
     */
    public boolean isPasswordEnabled() {
        return passwordEnabled;
    }

    /**
     * Set whether password is enabled for SMS authorization combined with password.
     * @param passwordEnabled Whether password is enabled for SMS authorization combined with password.
     */
    public void setPasswordEnabled(boolean passwordEnabled) {
        this.passwordEnabled = passwordEnabled;
    }

    /**
     * Get delay for resending SMS in milliseconds.
     * @return Delay for resending SMS in milliseconds.
     */
    public int getResendDelay() {
        return resendDelay;
    }

    /**
     * Set delay for resending SMS in milliseconds.
     * @param resendDelay Delay for resending SMS in milliseconds.
     */
    public void setResendDelay(int resendDelay) {
        this.resendDelay = resendDelay;
    }
}
