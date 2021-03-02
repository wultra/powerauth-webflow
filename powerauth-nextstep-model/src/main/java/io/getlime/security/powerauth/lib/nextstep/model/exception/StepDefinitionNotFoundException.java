package io.getlime.security.powerauth.lib.nextstep.model.exception;

/**
 * Exception for case when step definition is not found.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class StepDefinitionNotFoundException extends NextStepServiceException {

    public static final String CODE = "STEP_DEFINITION_NOT_FOUND";

    /**
     * Constructor with error message.
     * @param message Error message.
     */
    public StepDefinitionNotFoundException(String message) {
        super(message);
    }

}
