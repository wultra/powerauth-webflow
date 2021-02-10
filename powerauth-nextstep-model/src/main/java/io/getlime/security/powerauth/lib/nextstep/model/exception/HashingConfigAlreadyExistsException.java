package io.getlime.security.powerauth.lib.nextstep.model.exception;

import io.getlime.core.rest.model.base.entity.Error;

/**
 * Exception for case when hashing configuration already exists.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class HashingConfigAlreadyExistsException extends NextStepServiceException {

    public static final String CODE = "HASHING_CONFIG_ALREADY_EXISTS";

    private Error error;

    /**
     * Constructor with error message.
     * @param message Error message.
     */
    public HashingConfigAlreadyExistsException(String message) {
        super(message);
    }

    /**
     * Constructor with cause.
     * @param cause Original exception.
     */
    public HashingConfigAlreadyExistsException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructor with cause and error details.
     * @param cause Original exception.
     * @param error Object with error information.
     */
    public HashingConfigAlreadyExistsException(Throwable cause, Error error) {
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
