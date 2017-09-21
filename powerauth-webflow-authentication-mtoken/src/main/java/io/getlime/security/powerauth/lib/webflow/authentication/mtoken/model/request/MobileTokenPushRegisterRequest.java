package io.getlime.security.powerauth.lib.webflow.authentication.mtoken.model.request;

/**
 * @author Petr Dvorak, petr@lime-company.eu
 */
public class MobileTokenPushRegisterRequest {

    private String platform;
    private String token;

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
