package io.getlime.security.powerauth.lib.nextstep.model.exception;

import io.getlime.core.rest.model.base.entity.Error;

/**
 * Exception for case when operation is not configured.
 *
 * @author Roman Strobl, roman.strobl@lime-company.eu
 */
public class OperationNotConfiguredException extends NextStepServiceException {

    public static final String CODE = "OPERATION_NOT_CONFIGURED";

    private Error error;

    /**
     * Constructor with error message.
     * @param message Error message.
     */
    public OperationNotConfiguredException(String message) {
        super(message);
    }

    /**
     * Constructor with cause.
     * @param cause Original exception.
     */
    public OperationNotConfiguredException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructor with cause and error details.
     * @param cause Original exception.
     * @param error Object with error information.
     */
    public OperationNotConfiguredException(Throwable cause, Error error) {
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
