package io.getlime.security.powerauth.lib.webflow.authentication.mtoken.exception;

import io.getlime.security.powerauth.rest.api.base.exception.PowerAuthAuthenticationException;

/**
 * Exception thrown when activation is not configured.
 *
 * @author Roman Strobl, roman.strobl@lime-company.eu
 */
public class ActivationNotAvailableException extends PowerAuthAuthenticationException {

    public ActivationNotAvailableException() {
        super("Activation is not configured in Mobile Token API component.");
    }
}
