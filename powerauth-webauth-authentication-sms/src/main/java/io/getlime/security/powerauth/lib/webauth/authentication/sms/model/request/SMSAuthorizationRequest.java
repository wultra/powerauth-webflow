package io.getlime.security.powerauth.lib.webauth.authentication.sms.model.request;

import io.getlime.security.powerauth.lib.webauth.authentication.base.AuthStepRequest;

/**
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
