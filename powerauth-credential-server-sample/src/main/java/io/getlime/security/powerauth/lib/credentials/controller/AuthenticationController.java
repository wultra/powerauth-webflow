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
package io.getlime.security.powerauth.lib.credentials.controller;

import io.getlime.security.powerauth.lib.credentials.model.entity.ErrorModel;
import io.getlime.security.powerauth.lib.credentials.model.request.AuthenticationRequest;
import io.getlime.security.powerauth.lib.credentials.model.response.AuthenticationResponse;
import io.getlime.security.powerauth.lib.credentials.validation.AuthenticationRequestValidator;
import io.getlime.security.powerauth.lib.nextstep.model.base.Request;
import io.getlime.security.powerauth.lib.nextstep.model.base.Response;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.ValidationUtils;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller class which handles user authentication.
 * @author Roman Strobl
 */
@RestController
public class AuthenticationController {

    /**
     * Authenticate user with given username and password.
     * @param request Authenticate user request.
     * @return Response with success or error code and error details in case authentication failed.
     */
    @RequestMapping(name = "/authenticate", method = RequestMethod.POST)
    public ResponseEntity<Response<?>> authenticate(@RequestBody Request<AuthenticationRequest> request) throws MethodArgumentNotValidException, NoSuchMethodException {
        AuthenticationRequest authenticationRequest = request.getRequestObject();

        // input validation is handled by AuthenticationRequestValidator
        // validation is invoked manually because of the generified Request object
        AuthenticationRequestValidator validator = new AuthenticationRequestValidator();
        BeanPropertyBindingResult result = new BeanPropertyBindingResult(authenticationRequest, "authenticationRequest");
        ValidationUtils.invokeValidator(validator, authenticationRequest, result);
        if (result.hasErrors()) {
            // getEnclosingMethod() on new object returns a reference to current method
            MethodParameter methodParam = new MethodParameter(new Object(){}.getClass().getEnclosingMethod(),0);
            throw new MethodArgumentNotValidException(methodParam, result);
        }

        // here will be the real authentication - call to the backend providing authentication
        if ("test".equals(authenticationRequest.getUsername())
                && "test".equals(authenticationRequest.getPassword())) {
            // supply real user id in constructor
            AuthenticationResponse responseOK = new AuthenticationResponse("12345678");
            Response response = new Response<>(Response.Status.OK, responseOK);
            return new ResponseEntity<Response<?>>(response, HttpStatus.OK);
        } else {
            // regular authentication failed error
            ErrorModel error = new ErrorModel(ErrorModel.ResponseCode.AUTHENTICATION_FAILED, "User authentication failed");
            Response response = new Response<>(Response.Status.ERROR, error);
            return new ResponseEntity<Response<?>>(response, HttpStatus.UNAUTHORIZED);
            // handle other possible authentication errors here
            // ...
        }
    }
}
