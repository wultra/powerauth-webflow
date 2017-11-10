package io.getlime.security.powerauth.lib.webflow.authentication.mtoken.exception;

import io.getlime.security.powerauth.rest.api.base.exception.PowerAuthAuthenticationException;

/**
 * Exception thrown when activation is not active.
 *
 * @author Roman Strobl, roman.strobl@lime-company.eu
 */
public class ActivationNotActiveException extends PowerAuthAuthenticationException {

    public ActivationNotActiveException() {
        super("Activation is not active in Mobile Token API component.");
    }
}
