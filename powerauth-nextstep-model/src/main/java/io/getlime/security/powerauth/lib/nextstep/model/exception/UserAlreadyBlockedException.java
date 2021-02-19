package io.getlime.security.powerauth.lib.nextstep.model.exception;

import io.getlime.core.rest.model.base.entity.Error;

/**
 * Exception for case when user identity is already blocked.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class UserAlreadyBlockedException extends NextStepServiceException {

    public static final String CODE = "USER_IDENTITY_ALREADY_BLOCKED";

    private Error error;

    /**
     * Constructor with error message.
     * @param message Error message.
     */
    public UserAlreadyBlockedException(String message) {
        super(message);
    }

    /**
     * Constructor with cause.
     * @param cause Original exception.
     */
    public UserAlreadyBlockedException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructor with cause and error details.
     * @param cause Original exception.
     * @param error Object with error information.
     */
    public UserAlreadyBlockedException(Throwable cause, Error error) {
        super(cause);
        this.error = error;
    }

    /**
     * Get error detail information.
     * @return Error detail information.
     */
    public Error getError() {
        return error;
    }
}
