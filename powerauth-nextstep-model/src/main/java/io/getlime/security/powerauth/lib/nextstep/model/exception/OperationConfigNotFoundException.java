package io.getlime.security.powerauth.lib.nextstep.model.exception;

/**
 * Exception for case when operation is not configured.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class OperationConfigNotFoundException extends NextStepServiceException {

    public static final String CODE = "OPERATION_CONFIG_NOT_FOUND";

    /**
     * Constructor with error message.
     * @param message Error message.
     */
    public OperationConfigNotFoundException(String message) {
        super(message);
    }

}
