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

package io.getlime.security.powerauth.app.webflow.exception;

import com.wultra.core.audit.base.Audit;
import com.wultra.core.audit.base.model.AuditDetail;
import io.getlime.core.rest.model.base.entity.Error;
import io.getlime.core.rest.model.base.response.ErrorResponse;
import io.getlime.security.powerauth.lib.webflow.authentication.exception.AuthStepException;
import io.getlime.security.powerauth.lib.webflow.authentication.service.AuthenticationManagementService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.InsufficientAuthenticationException;
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

    private final Logger logger = LoggerFactory.getLogger(DefaultExceptionResolver.class);

    private AuthenticationManagementService authenticationManagementService;

    private static final AuditDetail AUDIT_DETAIL_UNEXPECTED_ERROR = new AuditDetail("UNEXPECTED_ERROR");
    private static final AuditDetail AUDIT_DETAIL_BAD_REQUEST = new AuditDetail("BAD_REQUEST");
    private static final AuditDetail AUDIT_DETAIL_UNAUTHORIZED = new AuditDetail("AUTHORIZATION");
    private final Audit audit;

    /**
     * Initialization of the class with default configuration.
     *
     * @param authenticationManagementService Service managing authentication context.
     * @param audit Audit interface.
     */
    @Autowired
    public DefaultExceptionResolver(AuthenticationManagementService authenticationManagementService, Audit audit) {
        this.authenticationManagementService = authenticationManagementService;
        this.audit = audit;
    }

    /**
     * Default exception handler, for unexpected errors.
     * @param t Throwable.
     * @return Response with error details.
     */
    @ExceptionHandler(Throwable.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public @ResponseBody ErrorResponse handleDefaultException(Throwable t) {
        logger.error("Error occurred in Web Flow server", t);
        audit.error("Error occurred in Web Flow server", AUDIT_DETAIL_UNEXPECTED_ERROR, t);
        final Error error = new Error(Error.Code.ERROR_GENERIC, "error.unknown");
        return new ErrorResponse(error);
    }

    /**
     * Exception handler for AuthStepException.
     * @param ex Exception.
     * @return Response with error details.
     */
    @ExceptionHandler(AuthStepException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public @ResponseBody ErrorResponse handleAuthStepException(AuthStepException ex) {
        logger.warn("Error occurred in Web Flow server: {}", ex.getMessage());
        audit.warn("Error occurred in Web Flow server", AUDIT_DETAIL_BAD_REQUEST, ex);
        // Web Flow returns message ID for front-end localization instead of message.
        final Error error = new Error(Error.Code.ERROR_GENERIC, ex.getMessageId());
        return new ErrorResponse(error);
    }

    /**
     * Handling of unauthorized Exception.
     * @param ex Exception.
     * @return Response with error details.
     */
    @ExceptionHandler(InsufficientAuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public String handleUnauthorizedException(InsufficientAuthenticationException ex) {
        logger.warn("Error occurred in Web Flow server: {}", ex.getMessage());
        audit.warn("Error occurred in Web Flow server", AUDIT_DETAIL_UNAUTHORIZED, ex);
        authenticationManagementService.clearContext();
        return "redirect:/oauth/error";
    }


    /**
     * Handling of client authentication exception.
     * @param ex Exception.
     * @return Response with error details.
     */
    @ExceptionHandler(TlsClientAuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public @ResponseBody ErrorResponse handleTlsClientAuthenticationException(TlsClientAuthenticationException ex) {
        logger.warn("Error occurred in Web Flow server: {}", ex.getMessage());
        audit.warn("Error occurred in Web Flow server", AUDIT_DETAIL_UNAUTHORIZED, ex);
        final Error error = new Error(Error.Code.ERROR_GENERIC, ex.getMessageId());
        return new ErrorResponse(error);
    }
}
