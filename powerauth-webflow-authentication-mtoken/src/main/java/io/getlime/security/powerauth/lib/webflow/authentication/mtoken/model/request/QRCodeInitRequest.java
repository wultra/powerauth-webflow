package io.getlime.security.powerauth.lib.webflow.authentication.mtoken.model.request;

import io.getlime.security.powerauth.lib.webflow.authentication.base.AuthStepRequest;

/**
 * Request for QR code initialization in offline mode for QR token.
 *
 * @author Roman Strobl, roman.strobl@lime-company.eu
 */
public class QRCodeInitRequest extends AuthStepRequest {

    private String activationId;

    public String getActivationId() {
        return activationId;
    }

    public void setActivationId(String activationId) {
        this.activationId = activationId;
    }
}
