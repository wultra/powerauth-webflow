package io.getlime.security.powerauth.lib.webflow.authentication.mtoken.exception;

/**
 * Exception thrown when activation is not configured.
 *
 * @author Roman Strobl, roman.strobl@lime-company.eu
 */
public class ActivationNotAvailableException extends MobileAppApiException {

    public ActivationNotAvailableException() {
        super("Activation is not configured in Mobile Token API component.");
    }
}
