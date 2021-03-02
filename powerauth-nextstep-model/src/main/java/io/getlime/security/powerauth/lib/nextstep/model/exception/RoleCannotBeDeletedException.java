package io.getlime.security.powerauth.lib.nextstep.model.exception;

/**
 * Exception for case when role cannot be deleted.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class RoleCannotBeDeletedException extends NextStepServiceException {

    public static final String CODE = "ROLE_CANNOT_BE_DELETED";

    /**
     * Constructor with error message.
     * @param message Error message.
     */
    public RoleCannotBeDeletedException(String message) {
        super(message);
    }

}
