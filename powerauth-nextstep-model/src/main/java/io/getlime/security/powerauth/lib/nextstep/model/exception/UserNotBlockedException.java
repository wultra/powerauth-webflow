package io.getlime.security.powerauth.lib.nextstep.model.exception;

/**
 * Exception for case when user identity is not blocked.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class UserNotBlockedException extends NextStepServiceException {

    public static final String CODE = "USER_IDENTITY_NOT_BLOCKED";

    /**
     * Constructor with error message.
     * @param message Error message.
     */
    public UserNotBlockedException(String message) {
        super(message);
    }

}
