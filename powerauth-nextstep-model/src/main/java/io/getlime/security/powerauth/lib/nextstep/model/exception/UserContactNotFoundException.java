package io.getlime.security.powerauth.lib.nextstep.model.exception;

/**
 * Exception for case when user contact is not found.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class UserContactNotFoundException extends NextStepServiceException {

    public static final String CODE = "USER_CONTACT_NOT_FOUND";

    /**
     * Constructor with error message.
     * @param message Error message.
     */
    public UserContactNotFoundException(String message) {
        super(message);
    }

}
