package io.getlime.security.powerauth.lib.nextstep.model.exception;

/**
 * Exception for case when credential is not blocked.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class CredentialNotBlockedException extends NextStepServiceException {

    public static final String CODE = "CREDENTIAL_NOT_BLOCKED";

    /**
     * Constructor with error message.
     * @param message Error message.
     */
    public CredentialNotBlockedException(String message) {
        super(message);
    }

}
