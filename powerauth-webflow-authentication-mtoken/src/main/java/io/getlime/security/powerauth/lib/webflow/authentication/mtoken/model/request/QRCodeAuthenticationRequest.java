package io.getlime.security.powerauth.lib.webflow.authentication.mtoken.model.request;

import io.getlime.security.powerauth.lib.webflow.authentication.base.AuthStepRequest;

/**
 * Request for QR code based authentication in offline mode for mobile token.
 *
 * @author Roman Strobl, roman.strobl@lime-company.eu
 */
public class QRCodeAuthenticationRequest extends AuthStepRequest {

    private String activationId;
    private String authCode;
    private String nonce;
    private String dataHash;

    public String getActivationId() {
        return activationId;
    }

    public void setActivationId(String activationId) {
        this.activationId = activationId;
    }

    public String getAuthCode() {
        return authCode;
    }

    public void setAuthCode(String authCode) {
        this.authCode = authCode;
    }

    public String getNonce() {
        return nonce;
    }

    public void setNonce(String nonce) {
        this.nonce = nonce;
    }

    public String getDataHash() {
        return dataHash;
    }

    public void setDataHash(String dataHash) {
        this.dataHash = dataHash;
    }
}
