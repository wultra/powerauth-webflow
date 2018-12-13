/*
 * Copyright 2017 Wultra s.r.o.
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
import io.getlime.security.powerauth.lib.nextstep.model.entity.UserAuthMethodDetail;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthMethod;
import io.getlime.security.powerauth.lib.nextstep.model.request.GetAuthMethodsRequest;
import io.getlime.security.powerauth.lib.nextstep.model.request.GetUserAuthMethodsRequest;
import io.getlime.security.powerauth.lib.nextstep.model.request.UpdateAuthMethodRequest;
import io.getlime.security.powerauth.lib.nextstep.model.response.GetAuthMethodsResponse;
import io.getlime.security.powerauth.lib.nextstep.model.response.GetUserAuthMethodsResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller class related to Next Step authentication methods and user preferences.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Controller
public class AuthMethodController {

    private AuthMethodService authMethodService;

    /**
     * Controller constructor.
     * @param authMethodService Authentication method service.
     */
    @Autowired
    public AuthMethodController(AuthMethodService authMethodService) {
        this.authMethodService = authMethodService;
    }

    /**
     * Get all authentication methods supported by Next Step server.
     *
     * @param request Get auth methods request. Use null user ID in request.
     * @return List of authentication methods wrapped in GetAuthMethodResponse.
     */
    @RequestMapping(value = "/auth-method/list", method = RequestMethod.POST)
    public @ResponseBody ObjectResponse<GetAuthMethodsResponse> getAuthMethods(@RequestBody ObjectRequest<GetAuthMethodsRequest> request) {
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Received getAuthMethods request");
        List<AuthMethodDetail> authMethods = authMethodService.listAuthMethods();
        if (authMethods == null || authMethods.isEmpty()) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "No authentication method is configured in Next Step server");
            throw new IllegalStateException("No authentication method is configured in Next Step server.");
        }
        GetAuthMethodsResponse response = new GetAuthMethodsResponse();
        response.setAuthMethods(authMethods);
        Logger.getLogger(this.getClass().getName()).log(Level.FINE, "The getAuthMethods request succeeded, available authentication method count: {0}", authMethods.size());
        return new ObjectResponse<>(response);
    }

    /**
     * Get all enabled authentication methods for given user.
     *
     * @param request Get auth methods request. In case user ID is null, list of auth methods enabled by default is returned.
     * @return List of enabled authentication methods for given user wrapped in GetAuthMethodResponse.
     */
    @RequestMapping(value = "/user/auth-method/list", method = RequestMethod.POST)
    public @ResponseBody ObjectResponse<GetUserAuthMethodsResponse> getAuthMethodsEnabledForUser(@RequestBody ObjectRequest<GetUserAuthMethodsRequest> request) {
        // Log level is FINE to avoid flooding logs, this endpoint is used all the time.
        Logger.getLogger(this.getClass().getName()).log(Level.FINE, "Received getAuthMethodsEnabledForUser request, user ID: {0}", request.getRequestObject().getUserId());
        GetUserAuthMethodsRequest requestObject = request.getRequestObject();
        // userId can be null - in this case default setting is returned when user is not known
        String userId = requestObject.getUserId();
        List<UserAuthMethodDetail> userAuthMethods = authMethodService.listAuthMethodsEnabledForUser(userId);
        GetUserAuthMethodsResponse response = new GetUserAuthMethodsResponse();
        response.setUserAuthMethods(userAuthMethods);
        Logger.getLogger(this.getClass().getName()).log(Level.FINE, "The getAuthMethodsEnabledForUser request succeeded, available authentication method count: {0}", userAuthMethods.size());
        return new ObjectResponse<>(response);
    }

    /**
     * Enable an authentication method for given user.
     *
     * @param request Update auth method request. Use non-null user ID in request and specify authMethod.
     * @return List of enabled authentication methods for given user wrapped in GetAuthMethodResponse.
     */
    @RequestMapping(value = "/user/auth-method", method = RequestMethod.POST)
    public @ResponseBody ObjectResponse<GetUserAuthMethodsResponse> enableAuthMethodForUser(@RequestBody ObjectRequest<UpdateAuthMethodRequest> request) {
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Received enableAuthMethodForUser request, user ID: {0}, authentication method: {1}", new String[] {request.getRequestObject().getUserId(), request.getRequestObject().getAuthMethod().toString()});
        UpdateAuthMethodRequest requestObject = request.getRequestObject();
        String userId = requestObject.getUserId();
        if (userId == null) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Parameter userId is null in request object when enabling authentication method");
            throw new IllegalArgumentException("Parameter userId is null in request object when enabling authentication method");
        }
        AuthMethod authMethod = requestObject.getAuthMethod();
        if (authMethod == null) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Parameter authMethod is null in request object when enabling authentication method");
            throw new IllegalArgumentException("Parameter authMethod is null in request object when enabling authentication method");
        }
        Map<String, String> config = requestObject.getConfig();
        authMethodService.updateAuthMethodForUser(userId, authMethod, true, config);
        List<UserAuthMethodDetail> userAuthMethods = authMethodService.listAuthMethodsEnabledForUser(userId);
        GetUserAuthMethodsResponse response = new GetUserAuthMethodsResponse();
        response.setUserAuthMethods(userAuthMethods);
        Logger.getLogger(this.getClass().getName()).log(Level.FINE, "The enableAuthMethodForUser request succeeded");
        return new ObjectResponse<>(response);
    }

    /**
     * Disable an authentication method for given user.
     *
     * @param request Update auth method request. Use non-null user ID in request and specify authMethod.
     * @return List of enabled authentication methods for given user wrapped in GetAuthMethodResponse.
     */
    @RequestMapping(value = "/user/auth-method", method = RequestMethod.DELETE)
    public @ResponseBody ObjectResponse<GetUserAuthMethodsResponse> disableAuthMethodForUser(@RequestBody ObjectRequest<UpdateAuthMethodRequest> request) {
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Received disableAuthMethodForUser request, user ID: {0}, authentication method: {1}", new String[] {request.getRequestObject().getUserId(), request.getRequestObject().getAuthMethod().toString()});
        UpdateAuthMethodRequest requestObject = request.getRequestObject();
        String userId = requestObject.getUserId();
        if (userId == null) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Parameter userId is null in request object when disabling authentication method");
            throw new IllegalArgumentException("Parameter userId is null in request object when disabling authentication method");
        }
        AuthMethod authMethod = requestObject.getAuthMethod();
        if (authMethod == null) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Parameter authMethod is null in request object when disabling authentication method");
            throw new IllegalArgumentException("Parameter authMethod is null in request object when disabling authentication method");
        }
        Map<String, String> config = requestObject.getConfig();
        authMethodService.updateAuthMethodForUser(userId, authMethod, false, config);
        List<UserAuthMethodDetail> userAuthMethods = authMethodService.listAuthMethodsEnabledForUser(userId);
        GetUserAuthMethodsResponse response = new GetUserAuthMethodsResponse();
        response.setUserAuthMethods(userAuthMethods);
        Logger.getLogger(this.getClass().getName()).log(Level.FINE, "The disableAuthMethodForUser request succeeded");
        return new ObjectResponse<>(response);
    }

}
