package io.getlime.security.powerauth.lib.webflow.authentication.mtoken.exception;

/**
 * Exception thrown when list of pending operations could not be loaded.
 *
 * @author Roman Strobl, roman.strobl@lime-company.eu
 */
public class PendingOperationListFailedException extends MobileAppApiException {

    public PendingOperationListFailedException() {
        super("Unable to download pending operations in Mobile Token API component.");
    }
}
