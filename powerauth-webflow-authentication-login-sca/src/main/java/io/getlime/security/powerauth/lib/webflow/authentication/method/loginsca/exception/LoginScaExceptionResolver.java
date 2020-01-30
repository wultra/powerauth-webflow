/*
 * Copyright 2020 Wultra s.r.o.
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
package io.getlime.security.powerauth.lib.webflow.authentication.method.loginsca.exception;

import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthStepResult;
import io.getlime.security.powerauth.lib.webflow.authentication.method.loginsca.model.response.LoginScaAuthResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
@Order(LoginScaExceptionResolver.PRECEDENCE)
public class LoginScaExceptionResolver {

    static final int PRECEDENCE = -102;

    private static final Logger logger = LoggerFactory.getLogger(LoginScaExceptionResolver.class);

    private static final String ERROR_INVALID_REQUEST = "error.invalidRequest";

    /**
     * Handling of MethodArgumentNotValidException.
     *
     * @param ex Exception.
     * @return Response with error details.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.OK) public @ResponseBody LoginScaAuthResponse handleAuthStepException(MethodArgumentNotValidException ex) {
        logger.debug("Validation error occurred in Web Flow server: {}", ex.getMessage());
        LoginScaAuthResponse response = new LoginScaAuthResponse();
        response.setResult(AuthStepResult.AUTH_FAILED);
        if (ex.getBindingResult().getFieldError() != null) {
            response.setMessage(ex.getBindingResult().getFieldError().getDefaultMessage());
        } else {
            response.setMessage(ERROR_INVALID_REQUEST);
        }
        return response;
    }
}
