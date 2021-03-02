package io.getlime.security.powerauth.lib.nextstep.model.exception;

/**
 * Exception for case when user identity is not found.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class UserNotFoundException extends NextStepServiceException {

    public static final String CODE = "USER_IDENTITY_NOT_FOUND";

    /**
     * Constructor with error message.
     * @param message Error message.
     */
    public UserNotFoundException(String message) {
        super(message);
    }

}
