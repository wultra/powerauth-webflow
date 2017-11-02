package io.getlime.security.powerauth.lib.webflow.authentication.mtoken.exception;

import io.getlime.security.powerauth.rest.api.base.exception.PowerAuthAuthenticationException;

/**
 * Exception thrown when request object is invalid.
 *
 * @author Petr Dvorak, petr@lime-company.eu
 */
public class InvalidRequestObjectException extends PowerAuthAuthenticationException {

    public InvalidRequestObjectException() {
        super("Invalid request object sent to Mobile Token API component.");
    }
}
