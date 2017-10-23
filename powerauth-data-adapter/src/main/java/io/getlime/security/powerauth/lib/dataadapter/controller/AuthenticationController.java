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
package io.getlime.security.powerauth.lib.dataadapter.controller;

import io.getlime.core.rest.model.base.request.ObjectRequest;
import io.getlime.core.rest.model.base.response.ObjectResponse;
import io.getlime.security.powerauth.lib.dataadapter.api.DataAdapter;
import io.getlime.security.powerauth.lib.dataadapter.exception.AuthenticationFailedException;
import io.getlime.security.powerauth.lib.dataadapter.exception.UserNotFoundException;
import io.getlime.security.powerauth.lib.dataadapter.impl.validation.AuthenticationRequestValidator;
import io.getlime.security.powerauth.lib.dataadapter.model.request.AuthenticationRequest;
import io.getlime.security.powerauth.lib.dataadapter.model.request.UserDetailRequest;
import io.getlime.security.powerauth.lib.dataadapter.model.response.AuthenticationResponse;
import io.getlime.security.powerauth.lib.dataadapter.model.response.UserDetailResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.ValidationUtils;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Controller class which handles user authentication.
 *
 * @author Roman Strobl
 */
@Controller
@RequestMapping("/api/auth/user")
public class AuthenticationController {

    private final DataAdapter dataAdapter;

    @Autowired
    public AuthenticationController(DataAdapter dataAdapter) {
        this.dataAdapter = dataAdapter;
    }

    /**
     * Authenticate user with given username and password.
     *
     * @param request Authenticate user request.
     * @return Response with authenticated user ID.
     * @throws AuthenticationFailedException In case that authentication fails.
     * @throws MethodArgumentNotValidException In case form parameters are not valid.
     */
    @RequestMapping(value = "/authenticate", method = RequestMethod.POST)
    public @ResponseBody ObjectResponse<AuthenticationResponse> authenticate(@RequestBody ObjectRequest<AuthenticationRequest> request) throws MethodArgumentNotValidException, AuthenticationFailedException {
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
        dataAdapter.authenticateUser(authenticationRequest.getUsername(), authenticationRequest.getPassword());
        AuthenticationResponse response = new AuthenticationResponse(authenticationRequest.getUsername());
        return new ObjectResponse<>(response);
    }

    /**
     * Fetch user details based on user ID.
     *
     * @param request Request with user ID.
     * @return Response with user details.
     */
    @RequestMapping(value = "/info", method = RequestMethod.POST)
    public @ResponseBody ObjectResponse<UserDetailResponse> fetchUserDetail(@RequestBody ObjectRequest<UserDetailRequest> request) throws MethodArgumentNotValidException, UserNotFoundException {
        UserDetailRequest userDetailRequest = request.getRequestObject();
        UserDetailResponse response = dataAdapter.fetchUserDetail(userDetailRequest.getId());
        return new ObjectResponse<>(response);
    }


}
