package io.getlime.security.powerauth.lib.nextstep.model.exception;

/**
 * Exception for case when role is not assigned to a user identity.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class UserRoleNotAssignedException extends NextStepServiceException {

    public static final String CODE = "USER_ROLE_NOT_ASSIGNED";

    /**
     * Constructor with error message.
     * @param message Error message.
     */
    public UserRoleNotAssignedException(String message) {
        super(message);
    }

}
