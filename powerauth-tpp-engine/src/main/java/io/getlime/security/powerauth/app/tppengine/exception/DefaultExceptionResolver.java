/*
 * Copyright 2019 Wultra s.r.o.
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

package io.getlime.security.powerauth.app.tppengine.exception;

import io.getlime.core.rest.model.base.entity.Error;
import io.getlime.core.rest.model.base.response.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Controller advice responsible for default exception resolving.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
@ControllerAdvice
public class DefaultExceptionResolver {

    private static final Logger logger = LoggerFactory.getLogger(DefaultExceptionResolver.class);

    /**
     * Default exception handler, for unexpected errors.
     * @param t Throwable.
     * @return Response with error details.
     */
    @ExceptionHandler(Throwable.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public @ResponseBody ErrorResponse handleDefaultException(Throwable t) {
        logger.error("Error occurred in TPP engine server", t);
        Error error = new Error(Error.Code.ERROR_GENERIC, "error.unknown");
        return new ErrorResponse(error);
    }

    /**
     * Exception thrown in case consent was not found.
     * @param t Exception thrown when consent is not found.
     * @return Response with error details.
     */
    @ExceptionHandler(ConsentNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public @ResponseBody ErrorResponse handleConsentNotFoundException(ConsentNotFoundException t) {
        logger.error("Consent with ID {} was not found", t.getId(), t);
        return new ErrorResponse(new ConsentError("consent.missing"));
    }

}