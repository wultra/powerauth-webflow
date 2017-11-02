package io.getlime.security.powerauth.lib.webflow.authentication.mtoken.exception;

import io.getlime.security.powerauth.rest.api.base.exception.PowerAuthAuthenticationException;

/**
 * Exception thrown when operation could not be canceled.
 *
 * @author Roman Strobl, roman.strobl@lime-company.eu
 */
public class CancelOperationFailedException extends PowerAuthAuthenticationException {

    public CancelOperationFailedException() {
        super("Unable to cancel operation in Mobile Token API component.");
    }
}
