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
import com.wultra.core.rest.model.base.entity.Error;
import com.wultra.core.rest.model.base.response.ErrorResponse;
import com.wultra.core.rest.model.base.response.ObjectResponse;
import io.getlime.security.powerauth.lib.nextstep.model.entity.error.CredentialValidationError;
import io.getlime.security.powerauth.lib.nextstep.model.entity.error.ExtendedError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class representing a Next Step client exception created when calling the Next Step REST API.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class NextStepClientException extends Exception {

    private static final Logger logger = LoggerFactory.getLogger(NextStepClientException.class);

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Default constructor.
     */
    public NextStepClientException() {
        super();
    }

    /**
     * Constructor with message and error.
     * @param message Message.
     */
    public NextStepClientException(String message) {
        super(message);
    }

    /**
     * Constructor with message, cause and error.
     * @param message Message.
     * @param cause Cause.
     */
    public NextStepClientException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructor with cause and error.
     * @param cause Cause.
     */
    public NextStepClientException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructor with all details.
     * @param message Message.
     * @param cause Cause.
     * @param enableSuppression Whether suppression is enabled.
     * @param writableStackTrace Writeable stacktrace.
     */
    public NextStepClientException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    /**
     * Get the error.
     * @return Error.
     */
    public Error getError() {
        final Throwable cause = getCause();
        if (!(cause instanceof final RestClientException ex)) {
            final String message = cause != null ? cause.getMessage() : "General error without explicit cause";
            return new Error(Error.Code.ERROR_GENERIC, message);
        }
        if (ex.getErrorResponse() == null) {
            logger.trace("Wultra Java Core lib did not parse ErrorResponse for {}", ex.getResponse());
            try {
                // TODO (racansky, 2022-12-06) workaround until https://github.com/wultra/lime-java-core/issues/32
                ErrorResponse errorResponse = objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES).readValue(ex.getResponse(), ErrorResponse.class);
                if (errorResponse != null && errorResponse.getResponseObject() != null) {
                    switch (errorResponse.getResponseObject().getCode()) {
                        case "CREDENTIAL_VALIDATION_FAILED" -> {
                            ObjectResponse<CredentialValidationError> validationErrorResponse = objectMapper.readValue(ex.getResponse(), new TypeReference<>() {
                            });
                            return validationErrorResponse.getResponseObject();
                        }
                        case "REQUEST_VALIDATION_FAILED" -> {
                            ObjectResponse<ExtendedError> extendedErrorResponse = objectMapper.readValue(ex.getResponse(), new TypeReference<>() {
                            });
                            return extendedErrorResponse.getResponseObject();
                        }
                        default -> {
                            return null;
                        }
                    }
                }
            } catch (JsonProcessingException ex2) {
                logger.debug("Problem to deserialize error response", ex2);
                // Ignore unknown responses
                return null;
            }
            return null;
        }
        return ex.getErrorResponse().getResponseObject();
    }

}