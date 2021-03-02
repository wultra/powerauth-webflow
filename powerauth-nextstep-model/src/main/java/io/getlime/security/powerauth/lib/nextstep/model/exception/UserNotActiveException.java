package io.getlime.security.powerauth.lib.nextstep.model.exception;

/**
 * Exception for case when user identity is not active.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class UserNotActiveException extends NextStepServiceException {

    public static final String CODE = "USER_IDENTITY_NOT_ACTIVE";

    /**
     * Constructor with error message.
     * @param message Error message.
     */
    public UserNotActiveException(String message) {
        super(message);
    }

}
