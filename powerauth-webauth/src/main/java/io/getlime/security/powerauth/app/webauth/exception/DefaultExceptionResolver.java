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

package io.getlime.security.powerauth.app.webauth.exception;

import io.getlime.security.powerauth.lib.nextstep.model.base.Response;
import io.getlime.security.powerauth.lib.nextstep.model.entity.ErrorModel;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller advice responsible for default exception resolving.
 *
 * @author Petr Dvorak, petr@lime-company.eu
 */
@ControllerAdvice
public class DefaultExceptionResolver {

    /**
     * Default exception handler, for unexpected errors.
     *
     * @return Response with error details.
     */
    @ExceptionHandler(Throwable.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public @ResponseBody
    Response<ErrorModel> handleDefaultException(Throwable t) {
        Logger.getLogger(this.getClass().getName()).log(
                Level.SEVERE,
                "Error occurred in Web Auth server",
                t
        );
        ErrorModel error = new ErrorModel();
        error.setCode(ErrorModel.Code.ERROR_GENERIC);
        error.setMessage("error.unknown");
        return new Response<>(Response.Status.ERROR, error);
    }

}
