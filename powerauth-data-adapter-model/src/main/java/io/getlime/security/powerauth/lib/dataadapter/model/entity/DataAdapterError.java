/*
 * Copyright 2017 Lime - HighTech Solutions s.r.o.
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
package io.getlime.security.powerauth.lib.dataadapter.model.entity;

import io.getlime.core.rest.model.base.entity.Error;

import java.util.List;

/**
 * Error model, used to represent error responses.
 *
 * @author Roman Strobl
 */
public class DataAdapterError extends Error {

    private List<String> validationErrors;
    private Integer remainingAttempts;

    /**
     * Response codes for different authentication failures.
     */
    public class Code extends Error.Code {
        public static final String AUTHENTICATION_FAILED = "AUTHENTICATION_FAILED";
        public static final String SMS_AUTHORIZATION_FAILED = "SMS_AUTHORIZATION_FAILED";
        public static final String INPUT_INVALID = "INPUT_INVALID";
        public static final String REMOTE_ERROR = "REMOTE_ERROR";
    }

    /**
     * Default constructor.
     */
    public DataAdapterError() {
        super();
    }

    /**
     * Constructor accepting code and message.
     *
     * @param code    Error code.
     * @param message Error message.
     */
    public DataAdapterError(String code, String message) {
        super(code, message);
    }

    /**
     * Get the list with validation errors.
     * @return Validation errors.
     */
    public List<String> getValidationErrors() {
        return validationErrors;
    }

    /**
     * Set the list with validation errors.
     * @param validationErrors Validation errors.
     */
    public void setValidationErrors(List<String> validationErrors) {
        this.validationErrors = validationErrors;
    }

    /**
     * Get number of remaining authentication attempts.
     * @return Number of remaining attempts.
     */
    public Integer getRemainingAttempts() {
        return remainingAttempts;
    }

    /**
     * Set number of remaining authentication attempts.
     * @param remainingAttempts Number of remaining attempts.
     */
    public void setRemainingAttempts(Integer remainingAttempts) {
        this.remainingAttempts = remainingAttempts;
    }
}
