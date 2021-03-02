package io.getlime.security.powerauth.lib.nextstep.model.exception;

/**
 * Exception for case when credential definition already exists.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class CredentialDefinitionAlreadyExistsException extends NextStepServiceException {

    public static final String CODE = "CREDENTIAL_DEFINITION_ALREADY_EXISTS";

    /**
     * Constructor with error message.
     * @param message Error message.
     */
    public CredentialDefinitionAlreadyExistsException(String message) {
        super(message);
    }

}
