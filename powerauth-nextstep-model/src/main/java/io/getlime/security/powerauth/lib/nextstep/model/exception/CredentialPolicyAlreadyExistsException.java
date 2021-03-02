package io.getlime.security.powerauth.lib.nextstep.model.exception;

/**
 * Exception for case when credential policy already exists.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class CredentialPolicyAlreadyExistsException extends NextStepServiceException {

    public static final String CODE = "CREDENTIAL_POLICY_ALREADY_EXISTS";

    /**
     * Constructor with error message.
     * @param message Error message.
     */
    public CredentialPolicyAlreadyExistsException(String message) {
        super(message);
    }

}
