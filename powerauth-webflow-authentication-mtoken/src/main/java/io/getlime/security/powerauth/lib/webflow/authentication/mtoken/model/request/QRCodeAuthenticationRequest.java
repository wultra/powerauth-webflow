package io.getlime.security.powerauth.lib.webflow.authentication.mtoken.model.request;

import io.getlime.security.powerauth.lib.webflow.authentication.base.AuthStepRequest;

/**
 * Request for QR code based authentication in offline mode for mobile token.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class QRCodeAuthenticationRequest extends AuthStepRequest {

    private String activationId;
    private String authCode;
    private String nonce;

    /**
     * Get activation ID.
     * @return Activation ID.
     */
    public String getActivationId() {
        return activationId;
    }

    /**
     * Set activation ID.
     * @param activationId Activation ID.
     */
    public void setActivationId(String activationId) {
        this.activationId = activationId;
    }

    /**
     * Get authorization code.
     * @return Authorization code.
     */
    public String getAuthCode() {
        return authCode;
    }

    /**
     * Set authorization code.
     * @param authCode Authorization code.
     */
    public void setAuthCode(String authCode) {
        this.authCode = authCode;
    }

    /**
     * Get nonce.
     * @return Nonce.
     */
    public String getNonce() {
        return nonce;
    }

    /**
     * Set nonce.
     * @param nonce Nonce.
     */
    public void setNonce(String nonce) {
        this.nonce = nonce;
    }

}
