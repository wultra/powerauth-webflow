package io.getlime.security.powerauth.lib.webauth.authentication.mtoken.model.request;

/**
 * @author Petr Dvorak, petr@lime-company.eu
 */
public class MobileTokenSignRequest {

    private String id;
    private String data;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
