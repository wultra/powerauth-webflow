package io.getlime.security.powerauth.lib.webflow.authentication.sms.model.request;

import io.getlime.security.powerauth.lib.webflow.authentication.base.AuthStepRequest;

/**
 * Request for SMS authorization.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class SmsAuthorizationRequest extends AuthStepRequest {

    private String authCode;
    private String password;

    /**
     * Get authorization code from SMS message.
     * @return Authorization code from SMS message.
     */
    public String getAuthCode() {
        return authCode;
    }

    /**
     * Set authorization code from SMS message.
     * @param authCode Authorization code from SMS message.
     */
    public void setAuthCode(String authCode) {
        this.authCode = authCode;
    }

    /**
     * Get user password (optional).
     * @return User password.
     */
    public String getPassword() {
        return password;
    }

    /**
     * Set user password (optional).
     * @param password User password.
     */
    public void setPassword(String password) {
        this.password = password;
    }
}
