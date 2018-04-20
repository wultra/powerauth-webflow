package io.getlime.security.powerauth.lib.dataadapter.model.entity;

/**
 * Authorization code including salt used when generating the code.
 *
 * @author Roman Strobl, roman.strobl@lime-company.eu
 */
public class AuthorizationCode {

    private final String code;
    private final byte[] salt;

    public AuthorizationCode(String code, byte[] salt) {
        this.code = code;
        this.salt = salt;
    }

    public String getCode() {
        return code;
    }

    public byte[] getSalt() {
        return salt;
    }
}
