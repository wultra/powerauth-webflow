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
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handler for QR Code related exceptions.
 *
 * @author Roman Strobl, roman.strobl@lime-company.eu
 */

@ControllerAdvice
@Order(Ordered.LOWEST_PRECEDENCE-1)
public class QRCodeExceptionResolver {

    /**
     * Exception handler for illegal states (e.g. activation gone missing and invalid formData)
     *
     * @return Response with error details.
     */
    @ExceptionHandler(QRCodeInvalidDataException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public @ResponseBody ErrorResponse handleQRCodeInvalidDataException(QRCodeInvalidDataException ex) {
        Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Error occurred in Mobile Token API component", ex);
        Error error = new Error(Error.Code.ERROR_GENERIC, ex.getMessage());
        return new ErrorResponse(error);
    }


}
