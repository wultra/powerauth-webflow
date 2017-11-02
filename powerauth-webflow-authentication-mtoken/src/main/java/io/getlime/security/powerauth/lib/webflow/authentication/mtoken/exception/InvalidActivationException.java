package io.getlime.security.powerauth.lib.webflow.authentication.mtoken.exception;

import io.getlime.security.powerauth.rest.api.base.exception.PowerAuthAuthenticationException;

/**
 * Exception thrown when activation is invalid.
 *
 * @author Roman Strobl, roman.strobl@lime-company.eu
 */
public class InvalidActivationException extends PowerAuthAuthenticationException {

    public InvalidActivationException() {
        super("Invalid activation found in Mobile Token API component.");
    }
}
