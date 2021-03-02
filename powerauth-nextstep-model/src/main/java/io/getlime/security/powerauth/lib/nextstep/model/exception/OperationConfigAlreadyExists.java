package io.getlime.security.powerauth.lib.nextstep.model.exception;

/**
 * Exception for case when operation config already exists.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class OperationConfigAlreadyExists extends NextStepServiceException {

    public static final String CODE = "OPERATION_CONFIG_ALREADY_EXISTS";

    /**
     * Constructor with error message.
     * @param message Error message.
     */
    public OperationConfigAlreadyExists(String message) {
        super(message);
    }

}
