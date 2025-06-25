/*
 * PowerAuth Web Flow and related software components
 * Copyright (C) 2017 Wultra s.r.o.
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
package io.getlime.security.powerauth.lib.dataadapter.model.entity;

import com.wultra.core.rest.model.base.entity.Error;

import java.util.List;

/**
 * Error model, used to represent error responses.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class DataAdapterError extends Error {

    private List<String> validationErrors;
    private Integer remainingAttempts;

    /**
     * Response codes for different authentication failures.
     */
    public static class Code extends Error.Code {

        /**
         * User authentication failed.
         */
        public static final String AUTHENTICATION_FAILED = "AUTHENTICATION_FAILED";

        /**
         * SMS authorization failed.
         */
        public static final String SMS_AUTHORIZATION_FAILED = "SMS_AUTHORIZATION_FAILED";

        /**
         * User was not found.
         */
        public static final String USER_NOT_FOUND = "USER_NOT_FOUND";

        /**
         * Invalid request.
         */
        public static final String INPUT_INVALID = "INPUT_INVALID";

        /**
         * Operation context is invalid.
         */
        public static final String OPERATION_CONTEXT_INVALID = "OPERATION_CONTEXT_INVALID";

        /**
         * Consent data is invalid.
         */
        public static final String CONSENT_DATA_INVALID = "CONSENT_DATA_INVALID";

        /**
         * Error caused by client.
         */
        public static final String DATA_ADAPTER_CLIENT_ERROR = "DATA_ADAPTER_CLIENT_ERROR";

        /**
         * Error caused by remote error.
         */
        public static final String REMOTE_ERROR = "REMOTE_ERROR";

        /**
         * Communication error.
         */
        public static final String COMMUNICATION_ERROR = "COMMUNICATION_ERROR";
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
