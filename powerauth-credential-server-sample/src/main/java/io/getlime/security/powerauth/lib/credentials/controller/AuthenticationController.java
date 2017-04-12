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
package io.getlime.security.powerauth.lib.credentialServer.controller;

import io.getlime.security.powerauth.lib.credentialServer.model.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Roman Strobl
 */
@RestController
public class AuthenticationController {

    @RequestMapping(name = "/authenticate", method = RequestMethod.POST)
    public ResponseEntity<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest authRequest) {
        // input handling
        if (authRequest.getUsername()==null || authRequest.getUsername().isEmpty() || authRequest.getUsername().length()>30) {
            ErrorResponse errorResponse = new ErrorResponse(ErrorResponse.ResponseCode.USERNAME_FORMAT_INVALID, "Invalid request: username format is invalid");
            AuthenticationResponse response = new AuthenticationResponseError(HttpStatus.BAD_REQUEST, errorResponse);
            return new ResponseEntity<>(response, response.getHttpStatus());
        }

        if (authRequest.getPassword()==null || authRequest.getPassword().isEmpty() || authRequest.getPassword().length()>30) {
            ErrorResponse errorResponse = new ErrorResponse(ErrorResponse.ResponseCode.PASSWORD_FORMAT_INVALID, "Invalid request: password format is invalid");
            AuthenticationResponse response = new AuthenticationResponseError(HttpStatus.BAD_REQUEST, errorResponse);
            return new ResponseEntity<>(response, response.getHttpStatus());
        }

        // other authentication methods are reserved for future use
        if (authRequest.getType()!= AuthenticationType.BASIC) {
            ErrorResponse errorResponse = new ErrorResponse(ErrorResponse.ResponseCode.AUTH_METHOD_UNSUPPORTED, "Invalid request: method is not supported");
            AuthenticationResponse response = new AuthenticationResponseError(HttpStatus.BAD_REQUEST, errorResponse);
            return new ResponseEntity<>(response, response.getHttpStatus());
        }

        try {
            // here will be the real authentication
            if ("test".equals(authRequest.getUsername())
                    && "test".equals(authRequest.getPassword())) {
                // supply user id in constructor
                AuthenticationResponse responseSuccess = new AuthenticationResponseSuccess("12345678");
                return new ResponseEntity<>(responseSuccess, responseSuccess.getHttpStatus());
            } else {
                // regular authentication failed error
                ErrorResponse errorResponse = new ErrorResponse(ErrorResponse.ResponseCode.AUTH_FAIL, "User authentication failed");
                AuthenticationResponse response = new AuthenticationResponseError(HttpStatus.UNAUTHORIZED, errorResponse);
                return new ResponseEntity<>(response, response.getHttpStatus());
                // handle other possible authentication errors here
                // ...
            }
        } catch (Throwable t) {
            // unexpected authentication error
            ErrorResponse fatalErrorResponse = new ErrorResponse(ErrorResponse.ResponseCode.INTERNAL_SERVER_ERROR, t.toString());
            AuthenticationResponse response = new AuthenticationResponseError(HttpStatus.INTERNAL_SERVER_ERROR, fatalErrorResponse);
            return new ResponseEntity<>(response, response.getHttpStatus());
        }
    }
}
