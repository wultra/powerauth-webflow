package io.getlime.security.powerauth.lib.webflow.authentication.mtoken.exception;

/**
 * Exception thrown when operation could not be canceled.
 *
 * @author Roman Strobl, roman.strobl@lime-company.eu
 */
public class CancelOperationFailedException extends MobileAppApiException {

    public CancelOperationFailedException() {
        super("Unable to cancel operation in Mobile Token API component.");
    }
}
