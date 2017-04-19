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
import io.getlime.security.powerauth.lib.credentials.model.enumeration.AuthenticationType;
import io.getlime.security.powerauth.lib.credentials.model.request.AuthenticationRequest;
import io.getlime.security.powerauth.lib.credentials.model.response.AuthenticationResponse;
import io.getlime.security.powerauth.lib.nextstep.model.base.Request;
import io.getlime.security.powerauth.lib.nextstep.model.base.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
     * @param authRequest Authenticate user request.
     * @return Response with success or error code and error details in case authentication failed.
     */
    @RequestMapping(name = "/authenticate", method = RequestMethod.POST)
    public ResponseEntity<Response<?>> authenticate(@RequestBody Request<AuthenticationRequest> authRequest) {
        // input handling
        AuthenticationRequest request = authRequest.getRequestObject();
        if (request.getUsername() == null || request.getUsername().isEmpty() || request.getUsername().length() > 30) {
            ErrorModel error = new ErrorModel(ErrorModel.ResponseCode.USERNAME_FORMAT_INVALID, "Incorrect username format");
            Response response = new Response<>(Response.Status.ERROR, error);
            return new ResponseEntity<Response<?>>(response, HttpStatus.BAD_REQUEST);
        }

        if (request.getPassword() == null || request.getPassword().isEmpty() || request.getPassword().length() > 30) {
            ErrorModel error = new ErrorModel(ErrorModel.ResponseCode.PASSWORD_FORMAT_INVALID, "Incorrect password format");
            Response response = new Response<>(Response.Status.ERROR, error);
            return new ResponseEntity<Response<?>>(response, HttpStatus.BAD_REQUEST);
        }

        // other authentication methods are reserved for future use
        if (request.getType() != AuthenticationType.BASIC) {
            ErrorModel error = new ErrorModel(ErrorModel.ResponseCode.AUTH_METHOD_UNSUPPORTED, "Invalid authentication method");
            Response response = new Response<>(Response.Status.ERROR, error);
            return new ResponseEntity<Response<?>>(response, HttpStatus.BAD_REQUEST);
        }

        // here will be the real authentication
        if ("test".equals(request.getUsername())
                && "test".equals(request.getPassword())) {
            // supply user id in constructor
            AuthenticationResponse responseOK = new AuthenticationResponse("12345678");
            Response response = new Response<>(Response.Status.OK, responseOK);
            return new ResponseEntity<Response<?>>(response, HttpStatus.OK);
        } else {
            // regular authentication failed error
            ErrorModel error = new ErrorModel(ErrorModel.ResponseCode.AUTH_FAIL, "User authentication failed");
            Response response = new Response<>(Response.Status.ERROR, error);
            return new ResponseEntity<Response<?>>(response, HttpStatus.UNAUTHORIZED);
            // handle other possible authentication errors here
            // ...
        }
    }
}
