package io.getlime.security.powerauth.lib.nextstep.model.exception;

/**
 * Exception for case when user alias is not found.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class UserAliasNotFoundException extends NextStepServiceException {

    public static final String CODE = "USER_ALIAS_NOT_FOUND";

    /**
     * Constructor with error message.
     * @param message Error message.
     */
    public UserAliasNotFoundException(String message) {
        super(message);
    }

}
