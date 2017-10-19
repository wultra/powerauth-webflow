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

package io.getlime.security.powerauth.app.nextstep.controller;

import io.getlime.core.rest.model.base.request.ObjectRequest;
import io.getlime.core.rest.model.base.response.ObjectResponse;
import io.getlime.security.powerauth.app.nextstep.service.AuthMethodService;
import io.getlime.security.powerauth.lib.nextstep.model.entity.AuthMethodDetail;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthMethod;
import io.getlime.security.powerauth.lib.nextstep.model.request.GetAuthMethodsRequest;
import io.getlime.security.powerauth.lib.nextstep.model.request.UpdateAuthMethodRequest;
import io.getlime.security.powerauth.lib.nextstep.model.response.GetAuthMethodsResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * Controller class related to Next Step authentication methods and user preferences.
 *
 * @author Roman Strobl, roman.strobl@lime-company.eu
 */
@Controller
public class AuthMethodController {

    private AuthMethodService authMethodService;

    @Autowired
    public AuthMethodController(AuthMethodService authMethodService) {
        this.authMethodService = authMethodService;
    }

    /**
     * Get all authentication methods supported by Next Step server.
     *
     * @param request Get auth methods request. Use null userId in request.
     * @return List of authentication methods wrapped in GetAuthMethodResponse.
     */
    @RequestMapping(value = "/auth-method/list", method = RequestMethod.POST)
    public @ResponseBody ObjectResponse<GetAuthMethodsResponse> getAuthMethods(@RequestBody ObjectRequest<GetAuthMethodsRequest> request) {
        GetAuthMethodsRequest requestObject = request.getRequestObject();
        String userId = requestObject.getUserId();
        if (userId != null) {
            throw new IllegalArgumentException("Parameter userId is not null in request object, however null value was expected.");
        }
        List<AuthMethodDetail> authMethods = authMethodService.listAuthMethods();
        if (authMethods == null || authMethods.isEmpty()) {
            throw new IllegalStateException("No authentication method is configured in Next Step server.");
        }
        GetAuthMethodsResponse response = new GetAuthMethodsResponse();
        response.setAuthMethods(authMethods);
        return new ObjectResponse<>(response);
    }

    /**
     * Get all enabled authentication methods for given user.
     *
     * @param request Get auth methods request. Use non-null userId in request.
     * @return List of enabled authentication methods for given user wrapped in GetAuthMethodResponse.
     */
    @RequestMapping(value = "/user/auth-method/list", method = RequestMethod.POST)
    public @ResponseBody ObjectResponse<GetAuthMethodsResponse> getAuthMethodsEnabledForUser(@RequestBody ObjectRequest<GetAuthMethodsRequest> request) {
        GetAuthMethodsRequest requestObject = request.getRequestObject();
        // userId can be null - in this case default setting is returned when user is not known
        String userId = requestObject.getUserId();
        List<AuthMethodDetail> authMethods = authMethodService.listAuthMethodsEnabledForUser(userId);
        GetAuthMethodsResponse response = new GetAuthMethodsResponse();
        response.setAuthMethods(authMethods);
        return new ObjectResponse<>(response);
    }

    /**
     * Enable an authentication method for given user.
     *
     * @param request Update auth method request. Use non-null userId in request and specify authMethod.
     * @return List of enabled authentication methods for given user wrapped in GetAuthMethodResponse.
     */
    @RequestMapping(value = "/user/auth-method", method = RequestMethod.POST)
    public @ResponseBody ObjectResponse<GetAuthMethodsResponse> enableAuthMethodForUser(@RequestBody ObjectRequest<UpdateAuthMethodRequest> request) {
        UpdateAuthMethodRequest requestObject = request.getRequestObject();
        String userId = requestObject.getUserId();
        if (userId == null) {
            throw new IllegalArgumentException("Parameter userId is null in request object when enabling authentication method.");
        }
        AuthMethod authMethod = requestObject.getAuthMethod();
        if (authMethod == null) {
            throw new IllegalArgumentException("Parameter authMethod is null in request object when enabling authentication method.");
        }
        authMethodService.updateAuthMethodForUser(userId, authMethod, true);
        List<AuthMethodDetail> authMethods = authMethodService.listAuthMethodsEnabledForUser(userId);
        GetAuthMethodsResponse response = new GetAuthMethodsResponse();
        response.setAuthMethods(authMethods);
        return new ObjectResponse<>(response);
    }

    /**
     * Disable an authentication method for given user.
     *
     * @param request Update auth method request. Use non-null userId in request and specify authMethod.
     * @return List of enabled authentication methods for given user wrapped in GetAuthMethodResponse.
     */
    @RequestMapping(value = "/user/auth-method", method = RequestMethod.DELETE)
    public @ResponseBody ObjectResponse<GetAuthMethodsResponse> disableAuthMethodForUser(@RequestBody ObjectRequest<UpdateAuthMethodRequest> request) {
        UpdateAuthMethodRequest requestObject = request.getRequestObject();
        String userId = requestObject.getUserId();
        if (userId == null) {
            throw new IllegalArgumentException("Parameter userId is null in request object when disabling authentication method.");
        }
        AuthMethod authMethod = requestObject.getAuthMethod();
        if (authMethod == null) {
            throw new IllegalArgumentException("Parameter authMethod is null in request object when disabling authentication method.");
        }
        authMethodService.updateAuthMethodForUser(userId, authMethod, false);
        List<AuthMethodDetail> authMethods = authMethodService.listAuthMethodsEnabledForUser(userId);
        GetAuthMethodsResponse response = new GetAuthMethodsResponse();
        response.setAuthMethods(authMethods);
        return new ObjectResponse<>(response);
    }

}
