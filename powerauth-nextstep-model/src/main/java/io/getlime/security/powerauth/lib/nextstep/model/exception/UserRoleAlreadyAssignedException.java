package io.getlime.security.powerauth.lib.nextstep.model.exception;

/**
 * Exception for case when role is already assigned to a user identity.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class UserRoleAlreadyAssignedException extends NextStepServiceException {

    public static final String CODE = "USER_ROLE_ALREADY_ASSIGNED";

    /**
     * Constructor with error message.
     * @param message Error message.
     */
    public UserRoleAlreadyAssignedException(String message) {
        super(message);
    }

}
