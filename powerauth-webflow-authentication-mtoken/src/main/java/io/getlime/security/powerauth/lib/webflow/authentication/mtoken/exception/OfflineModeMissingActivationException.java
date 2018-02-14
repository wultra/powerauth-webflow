package io.getlime.security.powerauth.lib.webflow.authentication.mtoken.exception;

import io.getlime.security.powerauth.lib.webflow.authentication.exception.AuthStepException;

/**
 * Exception for state when activation is missing.
 *
 * @author Roman Strobl, roman.strobl@lime-company.eu
 */
public class OfflineModeMissingActivationException extends AuthStepException {

    public OfflineModeMissingActivationException(String message) {
        super(message, "offlineMode.noActivation");
    }

}
