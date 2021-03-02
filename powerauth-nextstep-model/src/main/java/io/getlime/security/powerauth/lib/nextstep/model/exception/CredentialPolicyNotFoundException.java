package io.getlime.security.powerauth.lib.nextstep.model.exception;

/**
 * Exception for case when credential policy is not found.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class CredentialPolicyNotFoundException extends NextStepServiceException {

    public static final String CODE = "CREDENTIAL_POLICY_NOT_FOUND";

    /**
     * Constructor with error message.
     * @param message Error message.
     */
    public CredentialPolicyNotFoundException(String message) {
        super(message);
    }

}
