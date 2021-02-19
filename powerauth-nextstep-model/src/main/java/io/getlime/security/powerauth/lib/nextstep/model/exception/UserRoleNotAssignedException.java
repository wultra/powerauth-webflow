package io.getlime.security.powerauth.lib.nextstep.model.exception;

import io.getlime.core.rest.model.base.entity.Error;

/**
 * Exception for case when role is not assigned to a user identity.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class UserRoleNotAssignedException extends NextStepServiceException {

    public static final String CODE = "USER_ROLE_NOT_ASSIGNED";

    private Error error;

    /**
     * Constructor with error message.
     * @param message Error message.
     */
    public UserRoleNotAssignedException(String message) {
        super(message);
    }

    /**
     * Constructor with cause.
     * @param cause Original exception.
     */
    public UserRoleNotAssignedException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructor with cause and error details.
     * @param cause Original exception.
     * @param error Object with error information.
     */
    public UserRoleNotAssignedException(Throwable cause, Error error) {
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
