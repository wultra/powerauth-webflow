package io.getlime.security.powerauth.lib.webflow.authentication.mtoken.exception;

import io.getlime.security.powerauth.rest.api.base.exception.PowerAuthAuthenticationException;

/**
 * Exception thrown when operation is expired.
 *
 * @author Roman Strobl, roman.strobl@lime-company.eu
 */
public class OperationExpiredException extends PowerAuthAuthenticationException {

    public OperationExpiredException() {
        super("Operation expired in Mobile Token API component.");
    }
}
