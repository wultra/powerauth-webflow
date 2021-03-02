package io.getlime.security.powerauth.lib.nextstep.model.exception;

/**
 * Exception for case when hashing configuration already exists.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class HashConfigAlreadyExistsException extends NextStepServiceException {

    public static final String CODE = "HASHING_CONFIG_ALREADY_EXISTS";

    /**
     * Constructor with error message.
     * @param message Error message.
     */
    public HashConfigAlreadyExistsException(String message) {
        super(message);
    }

}
