package io.getlime.security.powerauth.lib.webflow.authentication.sms.model.response;

import io.getlime.security.powerauth.lib.webflow.authentication.base.AuthStepResponse;

/**
 * Response for resend SMS authorization.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class ResendSmsAuthorizationResponse extends AuthStepResponse {

    private int resendDelay;

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
