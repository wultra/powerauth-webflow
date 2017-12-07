package io.getlime.security.powerauth.lib.webflow.authentication.mtoken.exception;

/**
 * Exception thrown when activation is invalid.
 *
 * @author Roman Strobl, roman.strobl@lime-company.eu
 */
public class InvalidActivationException extends MobileAppApiException {

    public InvalidActivationException() {
        super("Invalid activation found in Mobile Token API component.");
    }
}
