package io.getlime.security.powerauth.lib.nextstep.model.exception;

/**
 * Exception for case when user identity already exists.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class UserAlreadyExistsException extends NextStepServiceException {

    public static final String CODE = "USER_IDENTITY_ALREADY_EXISTS";

    /**
     * Constructor with error message.
     * @param message Error message.
     */
    public UserAlreadyExistsException(String message) {
        super(message);
    }

}
