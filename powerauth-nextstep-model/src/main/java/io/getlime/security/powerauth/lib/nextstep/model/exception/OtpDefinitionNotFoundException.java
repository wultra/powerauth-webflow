package io.getlime.security.powerauth.lib.nextstep.model.exception;

import io.getlime.core.rest.model.base.entity.Error;

/**
 * Exception for case when one time password definition is not found.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class OtpDefinitionNotFoundException extends NextStepServiceException {

    public static final String CODE = "OTP_DEFINITION_NOT_FOUND";

    private Error error;

    /**
     * Constructor with error message.
     * @param message Error message.
     */
    public OtpDefinitionNotFoundException(String message) {
        super(message);
    }

    /**
     * Constructor with cause.
     * @param cause Original exception.
     */
    public OtpDefinitionNotFoundException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructor with cause and error details.
     * @param cause Original exception.
     * @param error Object with error information.
     */
    public OtpDefinitionNotFoundException(Throwable cause, Error error) {
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
