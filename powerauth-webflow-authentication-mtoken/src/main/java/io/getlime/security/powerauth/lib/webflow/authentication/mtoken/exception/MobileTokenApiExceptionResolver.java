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
package io.getlime.security.powerauth.lib.webflow.authentication.mtoken.exception;

import io.getlime.core.rest.model.base.entity.Error;
import io.getlime.core.rest.model.base.response.ErrorResponse;
import io.getlime.security.powerauth.lib.nextstep.model.exception.OperationAlreadyFailedException;
import io.getlime.security.powerauth.lib.nextstep.model.exception.OperationAlreadyFinishedException;
import io.getlime.security.powerauth.rest.api.base.exception.PowerAuthAuthenticationException;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handler for mobile token related exceptions.
 *
 * @author Petr Dvorak, petr@lime-company.eu
 */

@ControllerAdvice
@Order(MobileTokenApiExceptionResolver.PRECEDENCE)
public class MobileTokenApiExceptionResolver {

    static final int PRECEDENCE = -101;

    /**
     * Exception handler for push registration related exception.
     *
     * @return Response with error details.
     */
    @ExceptionHandler(PushRegistrationFailedException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public @ResponseBody ErrorResponse handlePushRegistrationException(Throwable t) {
        Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Error occurred in Mobile Token API component", t);
        return new ErrorResponse(new Error("PUSH_REGISTRATION_FAILED", t.getMessage()));
    }

    /**
     * Exception handler for invalid request object exception.
     *
     * @return Response with error details.
     */
    @ExceptionHandler(InvalidRequestObjectException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public @ResponseBody ErrorResponse handleInvalidRequestObjectException(Throwable t) {
        Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Error occurred in Mobile Token API component", t);
        return new ErrorResponse(new Error("INVALID_REQUEST", t.getMessage()));
    }

    /**
     * Exception handler for invalid activation exception.
     *
     * @return Response with error details.
     */
    @ExceptionHandler(InvalidActivationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public @ResponseBody ErrorResponse handleInvalidActivationException(Throwable t) {
        Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Error occurred in Mobile Token API component", t);
        return new ErrorResponse(new Error("INVALID_ACTIVATION", t.getMessage()));
    }

    /**
     * Exception handler for PowerAuth authentication exception.
     *
     * @return Response with error details.
     */
    @ExceptionHandler(PowerAuthAuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public @ResponseBody ErrorResponse handlePowerAuthAuthenticationException(Throwable t) {
        Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Error occurred in Mobile Token API component", t);
        return new ErrorResponse(new Error("POWERAUTH_AUTH_FAIL", t.getMessage()));
    }

    /**
     * Exception handler for operation already finished exception.
     *
     * @return Response with error details.
     */
    @ExceptionHandler(OperationAlreadyFinishedException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public @ResponseBody ErrorResponse handleOperationAlreadyFinishedException(Throwable t) {
        Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Error occurred in Mobile Token API component", t);
        return new ErrorResponse(new Error("OPERATION_ALREADY_FINISHED", t.getMessage()));
    }

    /**
     * Exception handler for operation already failed exception.
     *
     * @return Response with error details.
     */
    @ExceptionHandler(OperationAlreadyFailedException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public @ResponseBody ErrorResponse handleOperationAlreadyFailedException(Throwable t) {
        Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Error occurred in Mobile Token API component", t);
        return new ErrorResponse(new Error("OPERATION_ALREADY_FAILED", t.getMessage()));
    }

    /**
     * Exception handler for expiration.
     *
     * @return Response with error details.
     */
    @ExceptionHandler(OperationExpiredException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public @ResponseBody ErrorResponse handleOperationExpiredException(Throwable t) {
        Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Error occurred in Mobile Token API component", t);
        return new ErrorResponse(new Error("OPERATION_EXPIRED", t.getMessage()));
    }
}
