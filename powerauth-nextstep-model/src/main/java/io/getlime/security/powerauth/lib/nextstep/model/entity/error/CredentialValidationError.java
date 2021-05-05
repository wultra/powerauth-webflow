/*
 * Copyright 2021 Wultra s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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