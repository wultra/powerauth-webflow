package io.getlime.security.powerauth.lib.nextstep.model.exception;

import io.getlime.core.rest.model.base.entity.Error;

/**
 * Exception for case when operation is not valid.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class OperationNotValidException extends NextStepServiceException {

    public static final String CODE = "OPERATION_NOT_VALID";

    private Error error;

    /**
     * Constructor with error message.
     * @param message Error message.
     */
    public OperationNotValidException(String message) {
        super(message);
    }

    /**
     * Constructor with cause.
     * @param cause Original exception.
     */
    public OperationNotValidException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructor with cause and error details.
     * @param cause Original exception.
     * @param error Object with error information.
     */
    public OperationNotValidException(Throwable cause, Error error) {
        super(cause);
        this.error = error;
    }

    /**
     * Get error detail information.
     * @return Error detail information.
     */
    public Error getError() {
        return error;
    }
}