package io.getlime.security.powerauth.lib.webflow.authentication.mtoken.exception;

import io.getlime.security.powerauth.lib.webflow.authentication.exception.AuthStepException;

/**
 * Exception for states with invalid data.
 *
 * @author Roman Strobl, roman.strobl@lime-company.eu
 */
public class OfflineModeInvalidDataException extends AuthStepException {

    public OfflineModeInvalidDataException(String message) {
        super(message, "offlineMode.invalidData");
    }

}
