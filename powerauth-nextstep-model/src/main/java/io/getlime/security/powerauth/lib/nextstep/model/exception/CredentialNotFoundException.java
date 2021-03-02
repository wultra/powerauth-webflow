package io.getlime.security.powerauth.lib.nextstep.model.exception;

/**
 * Exception for case when credential is not found.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class CredentialNotFoundException extends NextStepServiceException {

    public static final String CODE = "CREDENTIAL_NOT_FOUND";

    /**
     * Constructor with error message.
     * @param message Error message.
     */
    public CredentialNotFoundException(String message) {
        super(message);
    }

}
