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

package io.getlime.security.powerauth.lib.nextstep.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wultra.core.rest.client.base.RestClientException;
import io.getlime.core.rest.model.base.entity.Error;
import io.getlime.core.rest.model.base.response.ErrorResponse;
import io.getlime.core.rest.model.base.response.ObjectResponse;
import io.getlime.security.powerauth.lib.nextstep.model.entity.error.CredentialValidationError;
import io.getlime.security.powerauth.lib.nextstep.model.entity.error.ExtendedError;
import io.getlime.security.powerauth.lib.nextstep.model.entity.error.NextStepError;

/**
 * Class representing a Next Step client exception created when calling the Next Step REST API.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class NextStepClientException extends Exception {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private Error error;

    /**
     * Default constructor.
     */
    public NextStepClientException() {
        super();
        this.error = new NextStepError();
    }

    /**
     * Constructor with error.
     * @param error Error.
     */
    public NextStepClientException(Error error) {
        super(error != null ? error.getMessage() : null);
        this.error = error;
    }

    /**
     * Constructor with message and error.
     * @param message Message.
     * @param error Error.
     */
    public NextStepClientException(String message, Error error) {
        super(message);
        this.error = error;
    }

    /**
     * Constructor with message, cause and error.
     * @param message Message.
     * @param cause Cause.
     * @param error Error.
     */
    public NextStepClientException(String message, Throwable cause, Error error) {
        super(message, cause);
        this.error = error;
    }

    /**
     * Constructor with cause and error.
     * @param cause Cause.
     * @param error Error.
     */
    public NextStepClientException(Throwable cause, Error error) {
        super(cause);
        this.error = error;
    }

    /**
     * Constructor with all details.
     * @param message Message.
     * @param cause Cause.
     * @param enableSuppression Whether suppression is enabled.
     * @param writableStackTrace Writeable stacktrace.
     * @param error Error.
     */
    public NextStepClientException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace, Error error) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.error = error;
    }

    /**
     * Set the error.
     * @param error Error.
     */
    public void setError(Error error) {
        this.error = error;
    }

    /**
     * Get the error.
     * @return Error.
     */
    public Error getError() {
        return error;
    }

    /**
     * Get the original Next Step error.
     * @return Original Next Step error.
     */
    public Error getNextStepError() {
        Throwable cause = getCause();
        if (!(cause instanceof RestClientException)) {
            return null;
        }
        RestClientException ex = (RestClientException) cause;
        if (ex.getErrorResponse() == null) {
            try {
                ErrorResponse errorResponse = objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES).readValue(ex.getResponse(), ErrorResponse.class);
                if (errorResponse != null && errorResponse.getResponseObject() != null) {
                    switch (errorResponse.getResponseObject().getCode()) {
                        case "CREDENTIAL_VALIDATION_FAILED":
                            ObjectResponse<CredentialValidationError> validationErrorResponse = objectMapper.readValue(ex.getResponse(), new TypeReference<ObjectResponse<CredentialValidationError>>(){});
                            return validationErrorResponse.getResponseObject();

                        case "REQUEST_VALIDATION_FAILED":
                            ObjectResponse<ExtendedError> extendedErrorResponse = objectMapper.readValue(ex.getResponse(), new TypeReference<ObjectResponse<ExtendedError>>(){});
                            return extendedErrorResponse.getResponseObject();

                        default:
                            return null;
                    }
                }
            } catch (JsonProcessingException ex2) {
                // Ignore unknown responses
                return null;
            }
            return null;
        }
        return ex.getErrorResponse().getResponseObject();
    }

}