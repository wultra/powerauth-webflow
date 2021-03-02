package io.getlime.security.powerauth.lib.nextstep.model.exception;

/**
 * Exception for case when organization is not found.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class OrganizationNotFoundException extends NextStepServiceException {

    public static final String CODE = "ORGANIZATION_NOT_FOUND";

    /**
     * Constructor with error message.
     * @param message Error message.
     */
    public OrganizationNotFoundException(String message) {
        super(message);
    }

}
