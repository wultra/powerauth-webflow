package io.getlime.security.powerauth.lib.nextstep.model.exception;

import io.getlime.core.rest.model.base.entity.Error;

/**
 * Exception for case when generation algorithm for OTP is not supported.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class OtpGenAlgorithmNotSupportedException extends NextStepServiceException {

    public static final String CODE = "OTP_GEN_ALGORITHM_NOT_SUPPORTED";

    private Error error;

    /**
     * Constructor with error message.
     * @param message Error message.
     */
    public OtpGenAlgorithmNotSupportedException(String message) {
        super(message);
    }

    /**
     * Constructor with cause.
     * @param cause Original exception.
     */
    public OtpGenAlgorithmNotSupportedException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructor with cause and error details.
     * @param cause Original exception.
     * @param error Object with error information.
     */
    public OtpGenAlgorithmNotSupportedException(Throwable cause, Error error) {
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
