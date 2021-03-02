package io.getlime.security.powerauth.lib.nextstep.model.exception;

/**
 * Exception for case when organization already exists.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class OrganizationAlreadyExistsException extends NextStepServiceException {

    public static final String CODE = "ORGANIZATION_ALREADY_EXISTS";

    /**
     * Constructor with error message.
     * @param message Error message.
     */
    public OrganizationAlreadyExistsException(String message) {
        super(message);
    }

}
