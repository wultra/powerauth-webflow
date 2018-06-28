/*
 * Copyright 2018 Lime - HighTech Solutions s.r.o.
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
package io.getlime.security.powerauth.lib.webflow.authentication.mtoken.errorhandling;

import io.getlime.core.rest.model.base.entity.Error;
import io.getlime.core.rest.model.base.response.ErrorResponse;
import io.getlime.security.powerauth.lib.mtoken.model.enumeration.ErrorCode;
import io.getlime.security.powerauth.lib.nextstep.model.exception.OperationAlreadyCanceledException;
import io.getlime.security.powerauth.lib.nextstep.model.exception.OperationAlreadyFailedException;
import io.getlime.security.powerauth.lib.nextstep.model.exception.OperationAlreadyFinishedException;
import io.getlime.security.powerauth.lib.webflow.authentication.exception.OperationTimeoutException;
import io.getlime.security.powerauth.lib.webflow.authentication.mtoken.errorhandling.exception.InvalidActivationException;
import io.getlime.security.powerauth.lib.webflow.authentication.mtoken.errorhandling.exception.InvalidRequestObjectException;
import io.getlime.security.powerauth.lib.webflow.authentication.mtoken.errorhandling.exception.PushRegistrationFailedException;
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
@Order(MobileApiExceptionResolver.PRECEDENCE)
public class MobileApiExceptionResolver {

    static final int PRECEDENCE = -101;

    private ErrorResponse error(String code, Throwable t) {
        Logger.getLogger(this.getClass().getName()).log(Level.WARNING, "Error occurred in Mobile Token API component", t);
        return new ErrorResponse(new Error(code, t.getMessage()));
    }

    /**
     * Exception handler for push registration related exception.
     * @param t Throwable.
     * @return Response with error details.
     */
    @ExceptionHandler(PushRegistrationFailedException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public @ResponseBody ErrorResponse handlePushRegistrationException(Throwable t) {
        return error(ErrorCode.PUSH_REGISTRATION_FAILED, t);
    }

    /**
     * Exception handler for invalid request object exception.
     * @param t Throwable.
     * @return Response with error details.
     */
    @ExceptionHandler(InvalidRequestObjectException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public @ResponseBody ErrorResponse handleInvalidRequestObjectException(Throwable t) {
        return error(ErrorCode.INVALID_REQUEST, t);
    }

    /**
     * Exception handler for invalid activation exception.
     * @param t Throwable.
     * @return Response with error details.
     */
    @ExceptionHandler(InvalidActivationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public @ResponseBody ErrorResponse handleInvalidActivationException(Throwable t) {
        return error(ErrorCode.INVALID_ACTIVATION, t);
    }

    /**
     * Exception handler for PowerAuth authentication exception.
     * @param t Throwable.
     * @return Response with error details.
     */
    @ExceptionHandler(PowerAuthAuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public @ResponseBody ErrorResponse handlePowerAuthAuthenticationException(Throwable t) {
        return error(ErrorCode.POWERAUTH_AUTH_FAIL, t);
    }

    /**
     * Exception handler for operation already finished exception.
     * @param t Throwable.
     * @return Response with error details.
     */
    @ExceptionHandler(OperationAlreadyFinishedException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public @ResponseBody ErrorResponse handleOperationAlreadyFinishedException(Throwable t) {
        return error(ErrorCode.OPERATION_ALREADY_FINISHED, t);
    }

    /**
     * Exception handler for operation already failed exception.
     * @param t Throwable.
     * @return Response with error details.
     */
    @ExceptionHandler(OperationAlreadyFailedException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public @ResponseBody ErrorResponse handleOperationAlreadyFailedException(Throwable t) {
        return error(ErrorCode.OPERATION_ALREADY_FAILED, t);
    }

    /**
     * Exception handler for canceled operations.
     * @param t Throwable.
     * @return Response with error details.
     */
    @ExceptionHandler(OperationAlreadyCanceledException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public @ResponseBody ErrorResponse handleOperationCanceledException(Throwable t) {
        return error(ErrorCode.OPERATION_ALREADY_CANCELED, t);
    }

    /**
     * Exception handler for operation timeout exception.
     * @param t Throwable.
     * @return Response with error details.
     */
    @ExceptionHandler(OperationTimeoutException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public @ResponseBody ErrorResponse handleOperationTimeoutException(Throwable t) {
        return error(ErrorCode.OPERATION_EXPIRED, t);
    }

}
