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

import io.getlime.security.powerauth.lib.credentials.exception.AuthenticationFailedException;
import io.getlime.security.powerauth.lib.credentials.model.request.AuthenticationRequest;
import io.getlime.security.powerauth.lib.credentials.model.request.UserDetailRequest;
import io.getlime.security.powerauth.lib.credentials.model.response.AuthenticationResponse;
import io.getlime.security.powerauth.lib.credentials.model.response.UserDetailResponse;
import io.getlime.security.powerauth.lib.credentials.validation.AuthenticationRequestValidator;
import io.getlime.security.powerauth.lib.nextstep.model.base.Request;
import io.getlime.security.powerauth.lib.nextstep.model.base.Response;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.ValidationUtils;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

/**
 * Controller class which handles user authentication.
 * @author Roman Strobl
 */
@Controller
public class AuthenticationController {

    /**
     * Authenticate user with given username and password.
     * @param request Authenticate user request.
     * @return Response with success or error code and error details in case authentication failed.
     */
    @RequestMapping(value = "/authenticate", method = RequestMethod.POST)
    public @ResponseBody Response<AuthenticationResponse> authenticate(@RequestBody Request<AuthenticationRequest> request) throws MethodArgumentNotValidException, AuthenticationFailedException {
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
        if ("test".equals(authenticationRequest.getUsername()) && "test".equals(authenticationRequest.getPassword())) {
            AuthenticationResponse responseOK = new AuthenticationResponse("12345678");
            return new Response<>(Response.Status.OK, responseOK);
        } else {
            throw new AuthenticationFailedException("User authentication failed");
        }
    }

    @RequestMapping(value = "/userInfo", method = RequestMethod.POST)
    public @ResponseBody Response<UserDetailResponse> fetchUserDetail(@RequestBody Request<UserDetailRequest> request) throws MethodArgumentNotValidException, AuthenticationFailedException {
        UserDetailRequest userDetailRequest = request.getRequestObject();
        String userId = userDetailRequest.getId();

        // Fetch user details here ...

        UserDetailResponse responseObject = new UserDetailResponse();
        responseObject.setId(userId);
        responseObject.setGivenName("Joe");
        responseObject.setFamilyName("Doe");
        return new Response<>(Response.Status.OK, responseObject);
    }


}
