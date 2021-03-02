package io.getlime.security.powerauth.lib.nextstep.model.exception;

/**
 * Exception for case when application already exists.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class ApplicationAlreadyExistsException extends NextStepServiceException {

    public static final String CODE = "APPLICATION_ALREADY_EXISTS";

    /**
     * Constructor with error message.
     * @param message Error message.
     */
    public ApplicationAlreadyExistsException(String message) {
        super(message);
    }

}
