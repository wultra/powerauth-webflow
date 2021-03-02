package io.getlime.security.powerauth.lib.nextstep.model.exception;

/**
 * Exception for case when hashing configuration is not found.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class HashConfigNotFoundException extends NextStepServiceException {

    public static final String CODE = "HASHING_CONFIG_NOT_FOUND";

    /**
     * Constructor with error message.
     * @param message Error message.
     */
    public HashConfigNotFoundException(String message) {
        super(message);
    }

}
