package io.getlime.security.powerauth.lib.webflow.authentication.mtoken.exception;

/**
 * Exception thrown when push registration fails.
 *
 * @author Petr Dvorak, petr@lime-company.eu
 */
public class PushRegistrationFailedException extends MobileAppApiException {

    public PushRegistrationFailedException() {
        super("Push registration failed in Mobile Token API component.");
    }

}
