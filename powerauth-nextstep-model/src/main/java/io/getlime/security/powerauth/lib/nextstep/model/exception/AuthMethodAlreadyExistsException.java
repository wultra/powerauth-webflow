package io.getlime.security.powerauth.lib.nextstep.model.exception;

/**
 * Exception for case when authentication method already exists.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class AuthMethodAlreadyExistsException extends NextStepServiceException {

    public static final String CODE = "AUTH_METHOD_ALREADY_EXISTS";

    /**
     * Constructor with error message.
     * @param message Error message.
     */
    public AuthMethodAlreadyExistsException(String message) {
        super(message);
    }

}
