package io.getlime.security.powerauth.lib.nextstep.model.exception;

/**
 * Exception for case when user identity is already blocked.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class UserAlreadyBlockedException extends NextStepServiceException {

    public static final String CODE = "USER_IDENTITY_ALREADY_BLOCKED";

    /**
     * Constructor with error message.
     * @param message Error message.
     */
    public UserAlreadyBlockedException(String message) {
        super(message);
    }

}
