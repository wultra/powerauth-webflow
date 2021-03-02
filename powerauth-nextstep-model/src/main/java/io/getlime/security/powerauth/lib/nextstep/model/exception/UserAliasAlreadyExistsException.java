package io.getlime.security.powerauth.lib.nextstep.model.exception;

/**
 * Exception for case when user alias already exists.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class UserAliasAlreadyExistsException extends NextStepServiceException {

    public static final String CODE = "USER_ALIAS_ALREADY_EXISTS";

    /**
     * Constructor with error message.
     * @param message Error message.
     */
    public UserAliasAlreadyExistsException(String message) {
        super(message);
    }

}
