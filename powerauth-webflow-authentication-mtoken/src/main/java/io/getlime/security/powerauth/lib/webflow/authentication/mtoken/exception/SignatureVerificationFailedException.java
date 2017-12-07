package io.getlime.security.powerauth.lib.webflow.authentication.mtoken.exception;

/**
 * Exception thrown when signature could not be verified.
 *
 * @author Roman Strobl, roman.strobl@lime-company.eu
 */
public class SignatureVerificationFailedException extends MobileAppApiException {

    public SignatureVerificationFailedException() {
        super("Unable to verify signature in Mobile Token API component.");
    }
}
