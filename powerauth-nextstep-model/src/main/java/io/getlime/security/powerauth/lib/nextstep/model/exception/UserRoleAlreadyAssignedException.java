package io.getlime.security.powerauth.lib.nextstep.model.exception;

import io.getlime.core.rest.model.base.entity.Error;

/**
 * Exception for case when role is already assigned to a user identity.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class UserRoleAlreadyAssignedException extends NextStepServiceException {

    public static final String CODE = "USER_ROLE_ALREADY_ASSIGNED";

    private Error error;

    /**
     * Constructor with error message.
     * @param message Error message.
     */
    public UserRoleAlreadyAssignedException(String message) {
        super(message);
    }

    /**
     * Constructor with cause.
     * @param cause Original exception.
     */
    public UserRoleAlreadyAssignedException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructor with cause and error details.
     * @param cause Original exception.
     * @param error Object with error information.
     */
    public UserRoleAlreadyAssignedException(Throwable cause, Error error) {
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
