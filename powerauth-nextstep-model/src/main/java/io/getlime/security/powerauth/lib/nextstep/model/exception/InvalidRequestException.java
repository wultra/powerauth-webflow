package io.getlime.security.powerauth.lib.nextstep.model.exception;

/**
 * Exception for case when request is invalid.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class InvalidRequestException extends NextStepServiceException {

    public static final String CODE = "INVALID_REQUEST";

    /**
     * Constructor with error message.
     * @param message Error message.
     */
    public InvalidRequestException(String message) {
        super(message);
    }

    /**
     * Constructor with cause.
     * @param cause Original exception.
     */
    public InvalidRequestException(Throwable cause) {
        super(cause);
    }

}
