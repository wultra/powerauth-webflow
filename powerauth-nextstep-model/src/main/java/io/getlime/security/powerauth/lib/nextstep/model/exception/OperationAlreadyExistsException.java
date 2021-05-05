package io.getlime.security.powerauth.lib.nextstep.model.exception;

/**
 * Exception for case when operation already exists.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class OperationAlreadyExistsException extends NextStepServiceException {

    /**
     * Operation aleady exists.
     */
    public static final String CODE = "OPERATION_ALREADY_EXISTS";

    /**
     * Constructor with error message.
     * @param message Error message.
     */
    public OperationAlreadyExistsException(String message) {
        super(message);
    }

}
