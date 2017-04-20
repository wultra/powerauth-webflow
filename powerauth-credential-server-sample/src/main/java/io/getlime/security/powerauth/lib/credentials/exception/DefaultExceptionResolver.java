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

package io.getlime.security.powerauth.lib.credentials.exception;

import io.getlime.security.powerauth.lib.credentials.model.entity.ErrorModel;
import io.getlime.security.powerauth.lib.nextstep.model.base.Response;
import org.springframework.http.HttpStatus;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Controller advice responsible for default exception resolving.
 *
 * @author Roman Strobl
 */
@ControllerAdvice
public class DefaultExceptionResolver {

    /**
     * Handling of unexpected errors.
     * @return Response with ErrorModel.
     */
    @ExceptionHandler(Throwable.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public @ResponseBody
    Response<ErrorModel> handleDefaultException(Throwable t) {
        ErrorModel error = new ErrorModel();
        error.setCode(ErrorModel.Code.ERROR_GENERIC);
        error.setMessage("Unknown Error");
        return new Response<>(Response.Status.ERROR, error);
    }

    /**
     * Handling of validation errors.
     * @return Response with ErrorModel.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public @ResponseBody
    Response<ErrorModel> handleDefaultException(MethodArgumentNotValidException ex) {
        ErrorModel error = new ErrorModel();
        error.setCode(ErrorModel.Code.INPUT_INVALID);
        StringBuilder errorBuilder = new StringBuilder();
        for (ObjectError objError: ex.getBindingResult().getAllErrors()) {
            for (String code: objError.getCodes()) {
                int lastErrorBuilderLength = errorBuilder.length();
                switch (code) {
                    case "username.empty":
                        errorBuilder.append("username is empty");
                        break;
                    case "password.empty":
                        errorBuilder.append("password is empty");
                        break;
                    case "username.long":
                        errorBuilder.append("username length exceeded maximum number of characters");
                        break;
                    case "password.long":
                        errorBuilder.append("password length exceeded maximum number of characters");
                        break;
                    case "type.unsupported":
                        errorBuilder.append("authentication type is not supported");
                        break;
                    default:
                        continue;
                }
                // add comma but only if some error message was added
                if (lastErrorBuilderLength!=errorBuilder.length()) {
                    errorBuilder.append(", ");
                }
            }
        }
        String errors;
        if (errorBuilder.length()>=2) {
            // strip trailing comma
            errors = errorBuilder.substring(0, errorBuilder.length()-2);
        } else {
            errors = errorBuilder.toString();
        }
        error.setMessage("Input validation failed: "+errors);
        return new Response<>(Response.Status.ERROR, error);
    }

}
