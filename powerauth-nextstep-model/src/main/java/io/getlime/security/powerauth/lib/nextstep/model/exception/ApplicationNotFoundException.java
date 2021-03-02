package io.getlime.security.powerauth.lib.nextstep.model.exception;

/**
 * Exception for case when application is not found.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class ApplicationNotFoundException extends NextStepServiceException {

    public static final String CODE = "APPLICATION_NOT_FOUND";

    /**
     * Constructor with error message.
     * @param message Error message.
     */
    public ApplicationNotFoundException(String message) {
        super(message);
    }

}
