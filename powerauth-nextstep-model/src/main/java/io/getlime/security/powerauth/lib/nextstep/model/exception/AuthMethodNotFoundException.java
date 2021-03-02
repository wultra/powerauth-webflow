package io.getlime.security.powerauth.lib.nextstep.model.exception;

/**
 * Exception for case when authentication method already exists.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class AuthMethodNotFoundException extends NextStepServiceException {

    public static final String CODE = "AUTH_METHOD_NOT_FOUND";

    /**
     * Constructor with error message.
     * @param message Error message.
     */
    public AuthMethodNotFoundException(String message) {
        super(message);
    }

}
