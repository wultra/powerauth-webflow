package io.getlime.security.powerauth.lib.nextstep.model.exception;

/**
 * Exception for case when credential is not active.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class CredentialNotActiveException extends NextStepServiceException {

    public static final String CODE = "CREDENTIAL_NOT_ACTIVE";

    /**
     * Constructor with error message.
     * @param message Error message.
     */
    public CredentialNotActiveException(String message) {
        super(message);
    }

}
