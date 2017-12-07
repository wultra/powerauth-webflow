package io.getlime.security.powerauth.lib.webflow.authentication.mtoken.exception;

/**
 * Exception thrown when request object is invalid.
 *
 * @author Petr Dvorak, petr@lime-company.eu
 */
public class InvalidRequestObjectException extends MobileAppApiException {

    public InvalidRequestObjectException() {
        super("Invalid request object sent to Mobile Token API component.");
    }
}
