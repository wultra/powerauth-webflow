/*
 * PowerAuth Web Flow and related software components
 * Copyright (C) 2019 Wultra s.r.o.
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

package io.getlime.security.powerauth.app.tppengine.errorhandling;

import com.wultra.core.audit.base.Audit;
import com.wultra.core.audit.base.model.AuditDetail;
import io.getlime.core.rest.model.base.entity.Error;
import io.getlime.core.rest.model.base.response.ErrorResponse;
import io.getlime.security.powerauth.app.tppengine.errorhandling.error.ConsentError;
import io.getlime.security.powerauth.app.tppengine.errorhandling.error.TppAppError;
import io.getlime.security.powerauth.app.tppengine.errorhandling.exception.ConsentNotFoundException;
import io.getlime.security.powerauth.app.tppengine.errorhandling.exception.TppAppNotFoundException;
import io.getlime.security.powerauth.app.tppengine.errorhandling.exception.TppNotFoundException;
import io.getlime.security.powerauth.app.tppengine.errorhandling.exception.UnableToCreateAppException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Controller advice responsible for default exception resolving.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
@ControllerAdvice
public class DefaultExceptionResolver {

    private static final Logger logger = LoggerFactory.getLogger(DefaultExceptionResolver.class);

    private static final AuditDetail AUDIT_DETAIL_UNEXPECTED_ERROR = new AuditDetail("UNEXPECTED_ERROR");
    private static final AuditDetail AUDIT_DETAIL_BAD_REQUEST = new AuditDetail("BAD_REQUEST");
    private final Audit audit;

    /**
     * Exception resolver constructor.
     * @param audit Audit interface.
     */
    @Autowired
    public DefaultExceptionResolver(Audit audit) {
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
        logger.error("Error occurred in TPP engine server", t);
        audit.error("Error occurred in TPP engine server", AUDIT_DETAIL_UNEXPECTED_ERROR, t);
        Error error = new Error(Error.Code.ERROR_GENERIC, "error.unknown");
        return new ErrorResponse(error);
    }

    /**
     * Default exception handler, for unexpected errors.
     * @param t Throwable.
     * @return Response with error details.
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public @ResponseBody ErrorResponse handleParameterException(MissingServletRequestParameterException t) {
        logger.warn("Missing request parameter", t);
        audit.warn("Missing request parameter", AUDIT_DETAIL_BAD_REQUEST, t);
        Error error = new Error(Error.Code.ERROR_GENERIC, t.getMessage());
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
        audit.error("Consent with ID {} was not found", AUDIT_DETAIL_BAD_REQUEST, t);
        return new ErrorResponse(new ConsentError("consent.missing"));
    }

    /**
     * Exception thrown in case TPP app was not found.
     * @param t Exception thrown when TPP app is not found.
     * @return Response with error details.
     */
    @ExceptionHandler(TppAppNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public @ResponseBody ErrorResponse handleTppAppNotFoundException(TppAppNotFoundException t) {
        logger.error("App with client ID '{}' was not found", t.getId(), t);
        audit.error("App with client ID '{}' was not found", AUDIT_DETAIL_BAD_REQUEST, t.getId(), t);
        return new ErrorResponse(new TppAppError("tpp.app.notFound"));
    }

    /**
     * Exception thrown in case TPP was not found.
     * @param t Exception thrown when TPP is not found.
     * @return Response with error details.
     */
    @ExceptionHandler(TppNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public @ResponseBody ErrorResponse handleTppNotFoundException(TppNotFoundException t) {
        logger.error("TPP was not found for provided license info: {}", t.getLicenseInfo(), t);
        audit.error("TPP was not found for provided license info: {}", AUDIT_DETAIL_BAD_REQUEST, t.getLicenseInfo(), t);
        return new ErrorResponse(new TppAppError("tpp.notFound"));
    }

    /**
     * Exception thrown in case TPP app cannot be created.
     * @param t Exception thrown when TPP app cannot be created.
     * @return Response with error details.
     */
    @ExceptionHandler(UnableToCreateAppException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public @ResponseBody ErrorResponse handleUnableToCreateAppException(UnableToCreateAppException t) {
        logger.error("Unable to create an app due to request errors: {}", t.getErrors(), t);
        audit.error("Unable to create an app due to request errors: {}", AUDIT_DETAIL_BAD_REQUEST, t.getErrors(), t);
        return new ErrorResponse(new TppAppError("tpp.app.unableToCreate", t.getErrors()));
    }

    /**
     * Exception handler for no resource found.
     *
     * @param e Exception.
     * @return Response with error details.
     */
    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public @ResponseBody ErrorResponse handleNoResourceFoundException(final NoResourceFoundException e) {
        logger.warn("Error occurred when calling an API: {}", e.getMessage());
        logger.debug("Exception detail: ", e);
        return new ErrorResponse("ERROR_NOT_FOUND", "Resource not found.");
    }
}
