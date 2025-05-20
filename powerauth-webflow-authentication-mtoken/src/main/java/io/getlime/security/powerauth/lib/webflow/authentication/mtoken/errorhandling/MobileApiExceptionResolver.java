/*
 * PowerAuth Web Flow and related software components
 * Copyright (C) 2018 Wultra s.r.o.
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
package io.getlime.security.powerauth.lib.webflow.authentication.mtoken.errorhandling;

import com.wultra.core.audit.base.Audit;
import com.wultra.core.audit.base.model.AuditDetail;
import com.wultra.core.rest.model.base.entity.Error;
import com.wultra.core.rest.model.base.response.ErrorResponse;
import io.getlime.security.powerauth.lib.mtoken.model.enumeration.ErrorCode;
import io.getlime.security.powerauth.lib.webflow.authentication.exception.*;
import io.getlime.security.powerauth.lib.webflow.authentication.mtoken.errorhandling.exception.*;
import io.getlime.security.powerauth.rest.api.spring.exception.PowerAuthAuthenticationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Handler for mobile token related exceptions.
 *
 * @author Petr Dvorak, petr@wultra.com
 */

@ControllerAdvice
@Order(MobileApiExceptionResolver.PRECEDENCE)
public class MobileApiExceptionResolver {

    static final int PRECEDENCE = -101;

    private final Logger logger = LoggerFactory.getLogger(MobileApiExceptionResolver.class);

    private static final AuditDetail AUDIT_DETAIL_BAD_REQUEST = new AuditDetail("BAD_REQUEST");
    private final Audit audit;

    /**
     * Exception resolver constructor.
     * @param audit Audit interface.
     */
    @Autowired
    public MobileApiExceptionResolver(Audit audit) {
        this.audit = audit;
    }


    private ErrorResponse error(String code, Throwable t) {
        logger.warn("Error occurred in Mobile Token API component", t);
        audit.warn("Error occurred in Mobile Token API component", AUDIT_DETAIL_BAD_REQUEST, t);
        return new ErrorResponse(new Error(code, t.getMessage()));
    }

    private ErrorResponse error(String code, AuthStepException e) {
        logger.warn("Error occurred", e);
        audit.warn("Error occurred", AUDIT_DETAIL_BAD_REQUEST, e);
        return new ErrorResponse(new Error(code, e.getMessageId()));
    }

    /**
     * Exception handler for push registration related exception.
     * @param t Throwable.
     * @return Response with error details.
     */
    @ExceptionHandler(PushRegistrationFailedException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
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
     * Exception handler for activation not active exception.
     * @param t Throwable.
     * @return Response with error details.
     */
    @ExceptionHandler(ActivationNotActiveException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public @ResponseBody ErrorResponse handleActivationNotActiveException(Throwable t) {
        return error(ErrorCode.ACTIVATION_NOT_ACTIVE, t);
    }


    /**
     * Exception handler for activation not configured exception.
     * @param t Throwable.
     * @return Response with error details.
     */
    @ExceptionHandler(ActivationNotConfiguredException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public @ResponseBody ErrorResponse handleActivationNotConfiguredException(Throwable t) {
        return error(ErrorCode.ACTIVATION_NOT_CONFIGURED, t);
    }

    /**
     * Exception handler for invalid activation exception.
     * @param t Throwable.
     * @return Response with error details.
     */
    @ExceptionHandler(InvalidActivationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public @ResponseBody ErrorResponse handleInvalidActivationException(Throwable t) {
        // Special handling of invalid activation exception because this is a very common error
        logger.info(t.getMessage());
        return new ErrorResponse(new Error(ErrorCode.INVALID_ACTIVATION, t.getMessage()));
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
    @ExceptionHandler(OperationIsAlreadyFinished.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public @ResponseBody ErrorResponse handleOperationAlreadyFinishedException(Throwable t) {
        return error(ErrorCode.OPERATION_ALREADY_FINISHED, t);
    }

    /**
     * Exception handler for operation already failed exception.
     * @param t Throwable.
     * @return Response with error details.
     */
    @ExceptionHandler(OperationIsAlreadyFailedException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public @ResponseBody ErrorResponse handleOperationAlreadyFailedException(Throwable t) {
        return error(ErrorCode.OPERATION_ALREADY_FAILED, t);
    }

    /**
     * Exception handler for canceled operations.
     * @param t Throwable.
     * @return Response with error details.
     */
    @ExceptionHandler(OperationIsAlreadyCanceledException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public @ResponseBody ErrorResponse handleOperationCanceledException(Throwable t) {
        return error(ErrorCode.OPERATION_ALREADY_CANCELED, t);
    }

    /**
     * Exception handler for operation timeout exception.
     * @param e Authentication step exception.
     * @return Response with error details.
     */
    @ExceptionHandler(OperationTimeoutException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public @ResponseBody ErrorResponse handleOperationTimeoutException(AuthStepException e) {
        return error(ErrorCode.OPERATION_EXPIRED, e);
    }

}
