package io.getlime.security.powerauth.lib.nextstep.model.exception;

/**
 * Exception for case when operation data is invalid.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class InvalidOperationDataException extends NextStepServiceException {

    public static final String CODE = "INVALID_OPERATION_DATA";

    /**
     * Constructor with error message.
     * @param message Error message.
     */
    public InvalidOperationDataException(String message) {
        super(message);
    }

}
