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
package io.getlime.security.powerauth.lib.nextstep.model.entity.error;

import io.getlime.core.rest.model.base.entity.Error;
import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.CredentialValidationFailure;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Credential validation error with list of validation failures.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class CredentialValidationError extends Error implements Serializable {

    private static final long serialVersionUID = -4674388987180219329L;

    /**
     * List of validation failures.
     */
    private final List<CredentialValidationFailure> validationFailures = new ArrayList<>();

    /**
     * Default constructor.
     */
    public CredentialValidationError() {
        super();
    }

    /**
     * Constructor accepting code and message.
     *
     * @param code    Error code.
     * @param message Error message.
     */
    public CredentialValidationError(String code, String message) {
        super(code, message);
    }

    /**
     * Constructor accepting code, message and credential validation failures.
     *
     * @param code Error code.
     * @param message Error message.
     * @param validationFailures Validation failures.
     */
    public CredentialValidationError(String code, String message, List<CredentialValidationFailure> validationFailures) {
        super(code, message);
        this.validationFailures.addAll(validationFailures);
    }

    /**
     * Get validation failures.
     * @return Validation failures.
     */
    public List<CredentialValidationFailure> getValidationFailures() {
        return validationFailures;
    }
}