package io.getlime.security.powerauth.lib.nextstep.model.exception;

/**
 * Exception for case when user contact already exists.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class UserContactAlreadyExistsException extends NextStepServiceException {

    public static final String CODE = "USER_CONTACT_ALREADY_EXISTS";

    /**
     * Constructor with error message.
     * @param message Error message.
     */
    public UserContactAlreadyExistsException(String message) {
        super(message);
    }

}
