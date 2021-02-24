package io.getlime.security.powerauth.lib.nextstep.model.exception;

import io.getlime.core.rest.model.base.entity.Error;

/**
 * Exception for case when credential is not blocked.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class CredentialNotBlockedException extends NextStepServiceException {

    public static final String CODE = "CREDENTIAL_NOT_BLOCKED";

    private Error error;

    /**
     * Constructor with error message.
     * @param message Error message.
     */
    public CredentialNotBlockedException(String message) {
        super(message);
    }

    /**
     * Constructor with cause.
     * @param cause Original exception.
     */
    public CredentialNotBlockedException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructor with cause and error details.
     * @param cause Original exception.
     * @param error Object with error information.
     */
    public CredentialNotBlockedException(Throwable cause, Error error) {
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
