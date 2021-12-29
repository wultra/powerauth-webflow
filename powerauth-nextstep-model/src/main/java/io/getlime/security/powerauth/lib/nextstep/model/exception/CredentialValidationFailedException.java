/*
 * PowerAuth Web Flow and related software components
 * Copyright (C) 2021 Wultra s.r.o.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package io.getlime.security.powerauth.lib.nextstep.model.exception;

import io.getlime.security.powerauth.lib.nextstep.model.entity.error.CredentialValidationError;

/**
 * Exception for case when credential validation fails.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class CredentialValidationFailedException extends NextStepServiceException {

    /**
     * Credential validation failed.
     */
    public static final String CODE = "CREDENTIAL_VALIDATION_FAILED";

    /**
     * Credential validation error.
     */
    private final CredentialValidationError error;

    /**
     * Constructor with message and error details.
     * @param message Error message.
     * @param error Object with error information.
     */
    public CredentialValidationFailedException(String message, CredentialValidationError error) {
        super(message);
        this.error = error;
    }

    /**
     * Get error detail information.
     * @return Error detail information.
     */
    public CredentialValidationError getError() {
        return error;
    }

}
