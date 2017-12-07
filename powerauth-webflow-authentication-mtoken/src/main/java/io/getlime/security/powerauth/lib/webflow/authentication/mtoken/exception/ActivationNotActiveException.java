package io.getlime.security.powerauth.lib.webflow.authentication.mtoken.exception;

/**
 * Exception thrown when activation is not active.
 *
 * @author Roman Strobl, roman.strobl@lime-company.eu
 */
public class ActivationNotActiveException extends MobileAppApiException {

    public ActivationNotActiveException() {
        super("Activation is not active in Mobile Token API component.");
    }
}
