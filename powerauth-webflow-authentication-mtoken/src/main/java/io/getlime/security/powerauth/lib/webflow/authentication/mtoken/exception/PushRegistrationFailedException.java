package io.getlime.security.powerauth.lib.webflow.authentication.mtoken.exception;

import io.getlime.security.powerauth.rest.api.base.exception.PowerAuthAuthenticationException;

/**
 * Exception thrown when push registration fails.
 *
 * @author Petr Dvorak, petr@lime-company.eu
 */
public class PushRegistrationFailedException extends PowerAuthAuthenticationException {

    public PushRegistrationFailedException() {
        super("Push registration failed in Mobile Token API component.");
    }

}
