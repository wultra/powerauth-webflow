package io.getlime.security.powerauth.lib.nextstep.model.exception;

/**
 * Exception for case when role already exists.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class RoleAlreadyExistsException extends NextStepServiceException {

    public static final String CODE = "ROLE_ALREADY_EXISTS";

    /**
     * Constructor with error message.
     * @param message Error message.
     */
    public RoleAlreadyExistsException(String message) {
        super(message);
    }

}
