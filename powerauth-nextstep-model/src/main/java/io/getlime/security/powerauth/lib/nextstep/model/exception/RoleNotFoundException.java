package io.getlime.security.powerauth.lib.nextstep.model.exception;

/**
 * Exception for case when role is not found.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class RoleNotFoundException extends NextStepServiceException {

    public static final String CODE = "ROLE_NOT_FOUND";

    /**
     * Constructor with error message.
     * @param message Error message.
     */
    public RoleNotFoundException(String message) {
        super(message);
    }

}
