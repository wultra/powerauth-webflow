package io.getlime.security.powerauth.lib.nextstep.model.exception;

/**
 * Exception for case when operation is not found.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class OperationNotFoundException extends NextStepServiceException {

    public static final String CODE = "OPERATION_NOT_FOUND";

    /**
     * Constructor with error message.
     * @param message Error message.
     */
    public OperationNotFoundException(String message) {
        super(message);
    }

}
