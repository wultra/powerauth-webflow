package io.getlime.security.powerauth.lib.webflow.authentication.sms.model.request;

import io.getlime.security.powerauth.lib.webflow.authentication.base.AuthStepRequest;

/**
 * Request for SMS authorization.
 *
 * @author Roman Strobl, roman.strobl@lime-company.eu
 */
public class SMSAuthorizationRequest extends AuthStepRequest {

    private String authCode;

    public String getAuthCode() {
        return authCode;
    }

    public void setAuthCode(String authCode) {
        this.authCode = authCode;
    }
}
