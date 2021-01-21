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
import io.getlime.security.powerauth.lib.nextstep.model.request.*;
import io.getlime.security.powerauth.lib.nextstep.model.response.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * REST controller class related to Next Step authentication methods and user preferences.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@RestController
public class AuthMethodController {

    private static final Logger logger = LoggerFactory.getLogger(AuthMethodController.class);

    private final AuthMethodService authMethodService;

    /**
     * Controller constructor.
     * @param authMethodService Authentication method service.
     */
    @Autowired
    public AuthMethodController(AuthMethodService authMethodService) {
        this.authMethodService = authMethodService;
    }

    @RequestMapping(value = "auth-method", method = RequestMethod.POST)
    public ObjectResponse<CreateAuthMethodResponse> createAuthMethod(@RequestBody ObjectRequest<CreateAuthMethodRequest> request) {
        return new ObjectResponse<>(new CreateAuthMethodResponse());
    }

    /**
     * Get all authentication methods supported by Next Step server.
     *
     * @param request Get auth methods request. Use null user ID in request.
     * @return List of authentication methods wrapped in GetAuthMethodResponse.
     */
    @RequestMapping(value = "auth-method/list", method = RequestMethod.POST)
    public ObjectResponse<GetAuthMethodsResponse> getAuthMethods(@RequestBody ObjectRequest<GetAuthMethodListRequest> request) {
        logger.info("Received getAuthMethods request");
        List<AuthMethodDetail> authMethods = authMethodService.listAuthMethods();
        if (authMethods == null || authMethods.isEmpty()) {
            logger.error("No authentication method is configured in Next Step server");
            throw new IllegalStateException("No authentication method is configured in Next Step server.");
        }
        GetAuthMethodsResponse response = new GetAuthMethodsResponse();
        response.getAuthMethods().addAll(authMethods);
        logger.debug("The getAuthMethods request succeeded, available authentication method count: {}", authMethods.size());
        return new ObjectResponse<>(response);
    }

    /**
     * Get all enabled authentication methods for given user.
     *
     * @param request Get auth methods request. In case user ID is null, list of auth methods enabled by default is returned.
     * @return List of enabled authentication methods for given user wrapped in GetAuthMethodResponse.
     */
    @RequestMapping(value = "user/auth-method/list", method = RequestMethod.POST)
    public ObjectResponse<GetUserAuthMethodsResponse> getAuthMethodsEnabledForUser(@RequestBody ObjectRequest<GetUserAuthMethodsRequest> request) {
        // Log level is FINE to avoid flooding logs, this endpoint is used all the time.
        logger.debug("Received getAuthMethodsEnabledForUser request, user ID: {}", request.getRequestObject().getUserId());
        GetUserAuthMethodsRequest requestObject = request.getRequestObject();
        // userId can be null - in this case default setting is returned when user is not known
        String userId = requestObject.getUserId();
        List<UserAuthMethodDetail> userAuthMethods = authMethodService.listAuthMethodsEnabledForUser(userId);
        GetUserAuthMethodsResponse response = new GetUserAuthMethodsResponse();
        response.getUserAuthMethods().addAll(userAuthMethods);
        logger.debug("The getAuthMethodsEnabledForUser request succeeded, available authentication method count: {}", userAuthMethods.size());
        return new ObjectResponse<>(response);
    }

    /**
     * Enable an authentication method for given user.
     *
     * @param request Update auth method request. Use non-null user ID in request and specify authMethod.
     * @return List of enabled authentication methods for given user wrapped in GetAuthMethodResponse.
     */
    @RequestMapping(value = "user/auth-method", method = RequestMethod.POST)
    public ObjectResponse<GetUserAuthMethodsResponse> enableAuthMethodForUser(@RequestBody ObjectRequest<UpdateAuthMethodRequest> request) {
        logger.info("Received enableAuthMethodForUser request, user ID: {}, authentication method: {}", request.getRequestObject().getUserId(), request.getRequestObject().getAuthMethod().toString());
        UpdateAuthMethodRequest requestObject = request.getRequestObject();
        String userId = requestObject.getUserId();
        if (userId == null) {
            logger.error("Parameter userId is null in request object when enabling authentication method");
            throw new IllegalArgumentException("Parameter userId is null in request object when enabling authentication method");
        }
        AuthMethod authMethod = requestObject.getAuthMethod();
        if (authMethod == null) {
            logger.error("Parameter authMethod is null in request object when enabling authentication method");
            throw new IllegalArgumentException("Parameter authMethod is null in request object when enabling authentication method");
        }
        Map<String, String> config = requestObject.getConfig();
        authMethodService.updateAuthMethodForUser(userId, authMethod, true, config);
        List<UserAuthMethodDetail> userAuthMethods = authMethodService.listAuthMethodsEnabledForUser(userId);
        GetUserAuthMethodsResponse response = new GetUserAuthMethodsResponse();
        response.getUserAuthMethods().addAll(userAuthMethods);
        logger.debug("The enableAuthMethodForUser request succeeded");
        return new ObjectResponse<>(response);
    }

    /**
     * Disable an authentication method for given user.
     *
     * @param request Update auth method request. Use non-null user ID in request and specify authMethod.
     * @return List of enabled authentication methods for given user wrapped in GetAuthMethodResponse.
     */
    @RequestMapping(value = "user/auth-method/delete", method = RequestMethod.POST)
    public ObjectResponse<GetUserAuthMethodsResponse> disableAuthMethodForUser(@RequestBody ObjectRequest<UpdateAuthMethodRequest> request) {
        return disableAuthMethodForUserImpl(request);
    }

    private ObjectResponse<GetUserAuthMethodsResponse> disableAuthMethodForUserImpl(ObjectRequest<UpdateAuthMethodRequest> request) {
        logger.info("Received disableAuthMethodForUser request, user ID: {}, authentication method: {}", request.getRequestObject().getUserId(), request.getRequestObject().getAuthMethod().toString());
        UpdateAuthMethodRequest requestObject = request.getRequestObject();
        String userId = requestObject.getUserId();
        if (userId == null) {
            logger.error("Parameter userId is null in request object when disabling authentication method");
            throw new IllegalArgumentException("Parameter userId is null in request object when disabling authentication method");
        }
        AuthMethod authMethod = requestObject.getAuthMethod();
        if (authMethod == null) {
            logger.error("Parameter authMethod is null in request object when disabling authentication method");
            throw new IllegalArgumentException("Parameter authMethod is null in request object when disabling authentication method");
        }
        Map<String, String> config = requestObject.getConfig();
        authMethodService.updateAuthMethodForUser(userId, authMethod, false, config);
        List<UserAuthMethodDetail> userAuthMethods = authMethodService.listAuthMethodsEnabledForUser(userId);
        GetUserAuthMethodsResponse response = new GetUserAuthMethodsResponse();
        response.getUserAuthMethods().addAll(userAuthMethods);
        logger.debug("The disableAuthMethodForUser request succeeded");
        return new ObjectResponse<>(response);
    }

    @RequestMapping(value = "user/auth-method/enabled/list", method = RequestMethod.POST)
    public ObjectResponse<GetEnabledMethodListResponse> getEnabledMethodList(@RequestBody ObjectRequest<GetEnabledMethodListRequest> request) {
        return new ObjectResponse<>(new GetEnabledMethodListResponse());
    }

    @RequestMapping(value = "auth-method/delete", method = RequestMethod.POST)
    public ObjectResponse<DeleteAuthMethodResponse> deleteAuthMethod(@RequestBody ObjectRequest<DeleteAuthMethodRequest> request) {
        return new ObjectResponse<>(new DeleteAuthMethodResponse());
    }

}
