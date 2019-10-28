package io.getlime.security.powerauth.lib.webflow.authentication.mtoken.model.request;

/**
 * Request for QR code initialization in offline mode for QR token.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class QrCodeInitRequest {

    private String activationId;

    public String getActivationId() {
        return activationId;
    }

    public void setActivationId(String activationId) {
        this.activationId = activationId;
    }
}
