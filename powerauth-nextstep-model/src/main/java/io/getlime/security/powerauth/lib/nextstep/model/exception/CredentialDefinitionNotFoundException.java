package io.getlime.security.powerauth.lib.nextstep.model.exception;

/**
 * Exception for case when credential definition is not found.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class CredentialDefinitionNotFoundException extends NextStepServiceException {

    public static final String CODE = "CREDENTIAL_DEFINITION_NOT_FOUND";

    /**
     * Constructor with error message.
     * @param message Error message.
     */
    public CredentialDefinitionNotFoundException(String message) {
        super(message);
    }

}
