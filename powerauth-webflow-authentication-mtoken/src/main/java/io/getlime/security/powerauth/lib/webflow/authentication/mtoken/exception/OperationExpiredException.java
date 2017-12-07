package io.getlime.security.powerauth.lib.webflow.authentication.mtoken.exception;

/**
 * Exception thrown when operation is expired.
 *
 * @author Roman Strobl, roman.strobl@lime-company.eu
 */
public class OperationExpiredException extends MobileAppApiException {

    public OperationExpiredException() {
        super("Operation expired in Mobile Token API component.");
    }
}
