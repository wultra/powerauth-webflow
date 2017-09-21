package io.getlime.security.powerauth.lib.webflow.authentication.mtoken.model.request;

/**
 * @author Petr Dvorak, petr@lime-company.eu
 */
public class MobileTokenCancelOperationRequest {

    private String id;
    private String reason;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
