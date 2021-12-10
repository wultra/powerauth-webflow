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

package io.getlime.security.powerauth.lib.webflow.authentication.exception;

import com.wultra.core.audit.base.Audit;
import com.wultra.core.audit.base.model.AuditDetail;
import io.getlime.core.rest.model.base.entity.Error;
import io.getlime.core.rest.model.base.response.ErrorResponse;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthStepResult;
import io.getlime.security.powerauth.lib.webflow.authentication.base.AuthStepResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Controller advice responsible for authentication exceptions.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@ControllerAdvice
@Order(AuthenticationExceptionResolver.PRECEDENCE)
public class AuthenticationExceptionResolver {

    static final int PRECEDENCE = -102;

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationExceptionResolver.class);

    private static final AuditDetail AUDIT_DETAIL_BAD_REQUEST = new AuditDetail("BAD_REQUEST");
    private final Audit audit;

    private static final String ERROR_INVALID_REQUEST = "error.invalidRequest";

    /**
     * Exception resolver constructor.
     * @param audit Audit interface.
     */
    @Autowired
    public AuthenticationExceptionResolver(Audit audit) {
        this.audit = audit;
    }

    /**
     * Handling of AuthStepException.
     *
     * @param ex Exception.
     * @return Response with error details.
     */
    @ExceptionHandler(AuthStepException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public @ResponseBody ErrorResponse handleMethodNotValidException(AuthStepException ex) {
        logger.warn("Error occurred in Web Flow server: {}", ex.getMessage());
        audit.warn("Error occurred in Web Flow server: {}", AUDIT_DETAIL_BAD_REQUEST, ex);
        // Web Flow returns message ID for front-end localization instead of message.
        final Error error = new Error(Error.Code.ERROR_GENERIC, ex.getMessageId());
        return new ErrorResponse(error);
    }

    /**
     * Handling of MethodArgumentNotValidException.
     *
     * @param ex Exception.
     * @return Response with error details.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public @ResponseBody AuthStepResponse handleMethodNotValidException(MethodArgumentNotValidException ex) {
        logger.warn("Validation error occurred in Web Flow server: {}", ex.getMessage());
        audit.warn("Validation error occurred in Web Flow server: {}", AUDIT_DETAIL_BAD_REQUEST, ex);
        AuthStepResponse response = new AuthStepResponse();
        response.setResult(AuthStepResult.AUTH_FAILED);
        if (ex.getBindingResult().getFieldError() != null) {
            response.setMessage(ex.getBindingResult().getFieldError().getDefaultMessage());
        } else {
            response.setMessage(ERROR_INVALID_REQUEST);
        }
        return response;
    }
}
