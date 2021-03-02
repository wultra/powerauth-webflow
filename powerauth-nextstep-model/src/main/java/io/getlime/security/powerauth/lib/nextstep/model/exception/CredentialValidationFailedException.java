package io.getlime.security.powerauth.lib.nextstep.model.exception;

import io.getlime.security.powerauth.lib.nextstep.model.entity.CredentialValidationError;

/**
 * Exception for case when credential validation fails.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class CredentialValidationFailedException extends NextStepServiceException {

    public static final String CODE = "CREDENTIAL_VALIDATION_FAILED";

    private final CredentialValidationError error;

    /**
     * Constructor with message and error details.
     * @param message Error message.
     * @param error Object with error information.
     */
    public CredentialValidationFailedException(String message, CredentialValidationError error) {
        super(message);
        this.error = error;
    }

    /**
     * Get error detail information.
     * @return Error detail information.
     */
    public CredentialValidationError getError() {
        return error;
    }

}
