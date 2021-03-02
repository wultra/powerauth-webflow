package io.getlime.security.powerauth.lib.nextstep.model.exception;

/**
 * Exception for case when Next Step configuration is invalid.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class InvalidConfigurationException extends NextStepServiceException {

    public static final String CODE = "INVALID_CONFIGURATION";

    /**
     * Constructor with error message.
     * @param message Error message.
     */
    public InvalidConfigurationException(String message) {
        super(message);
    }

    /**
     * Constructor with cause.
     * @param cause Original exception.
     */
    public InvalidConfigurationException(Throwable cause) {
        super(cause);
    }

}
