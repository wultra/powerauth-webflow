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
import io.getlime.security.powerauth.lib.nextstep.model.exception.*;
import io.getlime.security.powerauth.lib.nextstep.model.request.*;
import io.getlime.security.powerauth.lib.nextstep.model.response.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Nullable;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.List;
import java.util.Map;

/**
 * REST controller class related to Next Step authentication methods and user preferences.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@RestController
@Validated
public class AuthMethodController {

    private static final Logger logger = LoggerFactory.getLogger(AuthMethodController.class);

    private final AuthMethodService authMethodService;

    /**
     * REST controller constructor.
     * @param authMethodService Authentication method service.
     */
    @Autowired
    public AuthMethodController(AuthMethodService authMethodService) {
        this.authMethodService = authMethodService;
    }

    /**
     * Create an authentication method.
     * @param request Create authentication method request.
     * @return Create authentication method response.
     * @throws AuthMethodAlreadyExistsException Thrown when authentication method already exists.
     */
    @RequestMapping(value = "auth-method", method = RequestMethod.POST)
    public ObjectResponse<CreateAuthMethodResponse> createAuthMethod(@Valid @RequestBody ObjectRequest<CreateAuthMethodRequest> request) throws AuthMethodAlreadyExistsException {
        logger.info("Received createAuthMethod request, authentication method: {}", request.getRequestObject().getAuthMethod());
        final CreateAuthMethodResponse response = authMethodService.createAuthMethod(request.getRequestObject());
        logger.info("The createAuthMethod request succeeded, authentication method: {}", request.getRequestObject().getAuthMethod());
        return new ObjectResponse<>(response);
    }

    /**
     * Get all authentication methods supported by Next Step server.
     *
     * @return List of authentication methods wrapped in GetAuthMethodResponse.
     * @throws InvalidConfigurationException Thrown when Next Step configuration is invalid.
     */
    @RequestMapping(value = "auth-method", method = RequestMethod.GET)
    public ObjectResponse<GetAuthMethodsResponse> getAuthMethodList() throws InvalidConfigurationException {
        logger.info("Received getAuthMethodList request");
        final List<AuthMethodDetail> authMethods = authMethodService.listAuthMethods();
        if (authMethods == null || authMethods.isEmpty()) {
            logger.error("No authentication method is configured in Next Step server");
            throw new InvalidConfigurationException("No authentication method is configured in Next Step server.");
        }
        final GetAuthMethodsResponse response = new GetAuthMethodsResponse();
        response.getAuthMethods().addAll(authMethods);
        logger.debug("The getAuthMethodList request succeeded, available authentication method list size: {}", authMethods.size());
        return new ObjectResponse<>(response);
    }

    /**
     * Get all authentication methods supported by Next Step server using POST method.
     *
     * @param request Get auth methods request.
     * @return List of authentication methods wrapped in GetAuthMethodResponse.
     * @throws InvalidConfigurationException Thrown when Next Step configuration is invalid.
     */
    @RequestMapping(value = "auth-method/list", method = RequestMethod.POST)
    public ObjectResponse<GetAuthMethodsResponse> getAuthMethodListPost(@Valid @RequestBody ObjectRequest<GetAuthMethodListRequest> request) throws InvalidConfigurationException {
        logger.info("Received getAuthMethodListPost request");
        final List<AuthMethodDetail> authMethods = authMethodService.listAuthMethods();
        if (authMethods == null || authMethods.isEmpty()) {
            logger.error("No authentication method is configured in Next Step server");
            throw new InvalidConfigurationException("No authentication method is configured in Next Step server.");
        }
        final GetAuthMethodsResponse response = new GetAuthMethodsResponse();
        response.getAuthMethods().addAll(authMethods);
        logger.debug("The getAuthMethodListPost request succeeded, available authentication method list size: {}", authMethods.size());
        return new ObjectResponse<>(response);
    }

    /**
     * Get all enabled authentication methods for given user. In case user ID is null, list of auth methods enabled by default is returned.
     *
     * @param userId User ID.
     * @return List of enabled authentication methods for given user wrapped in GetAuthMethodResponse.
     * @throws InvalidConfigurationException Thrown when Next Step configuration is invalid.
     */
    @RequestMapping(value = "user/auth-method", method = RequestMethod.GET)
    public ObjectResponse<GetUserAuthMethodsResponse> getAuthMethodsEnabledForUser(@RequestParam @Nullable @Size(min = 1, max = 256) String userId) throws InvalidConfigurationException {
        // Log level is FINE to avoid flooding logs, this endpoint is used all the time.
        logger.debug("Received getAuthMethodsEnabledForUser request, user ID: {}", userId);
        // userId can be null - in this case default setting is returned when user is not known
        final List<UserAuthMethodDetail> userAuthMethods = authMethodService.listAuthMethodsEnabledForUser(userId);
        final GetUserAuthMethodsResponse response = new GetUserAuthMethodsResponse();
        response.getUserAuthMethods().addAll(userAuthMethods);
        logger.debug("The getAuthMethodsEnabledForUser request succeeded, available authentication list size: {}", userAuthMethods.size());
        return new ObjectResponse<>(response);
    }

    /**
     * Get all enabled authentication methods for given user using POST method.
     *
     * @param request Get auth methods request. In case user ID is null, list of auth methods enabled by default is returned.
     * @return List of enabled authentication methods for given user wrapped in GetAuthMethodResponse.
     * @throws InvalidConfigurationException Thrown when Next Step configuration is invalid.
     */
    @RequestMapping(value = "user/auth-method/list", method = RequestMethod.POST)
    public ObjectResponse<GetUserAuthMethodsResponse> getAuthMethodsEnabledForUserPost(@Valid @RequestBody ObjectRequest<GetUserAuthMethodsRequest> request) throws InvalidConfigurationException {
        // Log level is FINE to avoid flooding logs, this endpoint is used all the time.
        logger.debug("Received getAuthMethodsEnabledForUserPost request, user ID: {}", request.getRequestObject().getUserId());
        final GetUserAuthMethodsRequest requestObject = request.getRequestObject();
        // userId can be null - in this case default setting is returned when user is not known
        final String userId = requestObject.getUserId();
        final List<UserAuthMethodDetail> userAuthMethods = authMethodService.listAuthMethodsEnabledForUser(userId);
        final GetUserAuthMethodsResponse response = new GetUserAuthMethodsResponse();
        response.getUserAuthMethods().addAll(userAuthMethods);
        logger.debug("The getAuthMethodsEnabledForUserPost request succeeded, available authentication list size: {}", userAuthMethods.size());
        return new ObjectResponse<>(response);
    }

    /**
     * Enable an authentication method for given user.
     *
     * @param request Update auth method request. Use non-null user ID in request and specify authMethod.
     * @return List of enabled authentication methods for given user wrapped in GetAuthMethodResponse.
     * @throws InvalidConfigurationException Thrown when Next Step configuration is invalid.
     * @throws InvalidRequestException Thrown when request is invalid.
     */
    @RequestMapping(value = "user/auth-method", method = RequestMethod.POST)
    public ObjectResponse<GetUserAuthMethodsResponse> enableAuthMethodForUser(@Valid @RequestBody ObjectRequest<UpdateAuthMethodRequest> request) throws InvalidConfigurationException, InvalidRequestException {
        logger.info("Received enableAuthMethodForUser request, user ID: {}, authentication method: {}", request.getRequestObject().getUserId(), request.getRequestObject().getAuthMethod().toString());
        final UpdateAuthMethodRequest requestObject = request.getRequestObject();
        final String userId = requestObject.getUserId();
        if (userId == null) {
            logger.error("Parameter userId is null in request object when enabling authentication method");
            throw new InvalidRequestException("Parameter userId is null in request object when enabling authentication method");
        }
        final AuthMethod authMethod = requestObject.getAuthMethod();
        if (authMethod == null) {
            logger.error("Parameter authMethod is null in request object when enabling authentication method");
            throw new InvalidRequestException("Parameter authMethod is null in request object when enabling authentication method");
        }
        final Map<String, String> config = requestObject.getConfig();
        authMethodService.updateAuthMethodForUser(userId, authMethod, true, config);
        final List<UserAuthMethodDetail> userAuthMethods = authMethodService.listAuthMethodsEnabledForUser(userId);
        final GetUserAuthMethodsResponse response = new GetUserAuthMethodsResponse();
        response.getUserAuthMethods().addAll(userAuthMethods);
        logger.debug("The enableAuthMethodForUser request succeeded");
        return new ObjectResponse<>(response);
    }

    /**
     * Disable an authentication method for given user.
     *
     * @param request Update auth method request. Use non-null user ID in request and specify authMethod.
     * @return List of enabled authentication methods for given user wrapped in GetAuthMethodResponse.
     * @throws InvalidConfigurationException Thrown when Next Step configuration is invalid.
     * @throws InvalidRequestException Thrown when request is invalid.
     */
    @RequestMapping(value = "user/auth-method/delete", method = RequestMethod.POST)
    public ObjectResponse<GetUserAuthMethodsResponse> disableAuthMethodForUser(@Valid @RequestBody ObjectRequest<UpdateAuthMethodRequest> request) throws InvalidConfigurationException, InvalidRequestException {
        return disableAuthMethodForUserImpl(request);
    }

    private ObjectResponse<GetUserAuthMethodsResponse> disableAuthMethodForUserImpl(ObjectRequest<UpdateAuthMethodRequest> request) throws InvalidConfigurationException, InvalidRequestException {
        logger.info("Received disableAuthMethodForUser request, user ID: {}, authentication method: {}", request.getRequestObject().getUserId(), request.getRequestObject().getAuthMethod().toString());
        final UpdateAuthMethodRequest requestObject = request.getRequestObject();
        final String userId = requestObject.getUserId();
        if (userId == null) {
            logger.error("Parameter userId is null in request object when disabling authentication method");
            throw new InvalidRequestException("Parameter userId is null in request object when disabling authentication method");
        }
        final AuthMethod authMethod = requestObject.getAuthMethod();
        if (authMethod == null) {
            logger.error("Parameter authMethod is null in request object when disabling authentication method");
            throw new InvalidRequestException("Parameter authMethod is null in request object when disabling authentication method");
        }
        final Map<String, String> config = requestObject.getConfig();
        authMethodService.updateAuthMethodForUser(userId, authMethod, false, config);
        final List<UserAuthMethodDetail> userAuthMethods = authMethodService.listAuthMethodsEnabledForUser(userId);
        final GetUserAuthMethodsResponse response = new GetUserAuthMethodsResponse();
        response.getUserAuthMethods().addAll(userAuthMethods);
        logger.debug("The disableAuthMethodForUser request succeeded");
        return new ObjectResponse<>(response);
    }

    /**
     * Get enabled method list.
     * @param userId User ID.
     * @param operationName Operation name.
     * @return Get enabled method list response.
     * @throws InvalidConfigurationException Thrown when configuration is invalid.
     */
    @RequestMapping(value = "user/auth-method/enabled", method = RequestMethod.GET)
    public ObjectResponse<GetEnabledMethodListResponse> getEnabledMethodList(@RequestParam @NotBlank @Size(min = 1, max = 256) String userId, @RequestParam @NotBlank @Size(min = 2, max = 256) String operationName) throws InvalidConfigurationException {
        // Log level is FINE to avoid flooding logs, this endpoint is used all the time.
        logger.debug("Received getEnabledMethodList request, user ID: {}", userId);
        GetEnabledMethodListRequest request = new GetEnabledMethodListRequest();
        request.setUserId(userId);
        request.setOperationName(operationName);
        final GetEnabledMethodListResponse response = authMethodService.getEnabledMethodList(request);
        logger.debug("The getEnabledMethodList request succeeded, available authentication method list size: {}", response.getEnabledAuthMethods().size());
        return new ObjectResponse<>(response);
    }

    /**
     * Get enabled method list using POST method.
     * @param request Get enabled method list request.
     * @return Get enabled method list response.
     * @throws InvalidConfigurationException Thrown when configuration is invalid.
     */
    @RequestMapping(value = "user/auth-method/enabled/list", method = RequestMethod.POST)
    public ObjectResponse<GetEnabledMethodListResponse> getEnabledMethodListPost(@Valid @RequestBody ObjectRequest<GetEnabledMethodListRequest> request) throws InvalidConfigurationException {
        // Log level is FINE to avoid flooding logs, this endpoint is used all the time.
        logger.debug("Received getEnabledMethodListPost request, user ID: {}", request.getRequestObject().getUserId());
        final GetEnabledMethodListResponse response = authMethodService.getEnabledMethodList(request.getRequestObject());
        logger.debug("The getEnabledMethodListPost request succeeded, available authentication method list size: {}", response.getEnabledAuthMethods().size());
        return new ObjectResponse<>(response);
    }

    /**
     * Delete an authentication method.
     * @param request Delete authentication method request.
     * @return Delete authentication method response.
     * @throws AuthMethodNotFoundException Thrown when authentication method is invalid.
     * @throws DeleteNotAllowedException Thrown when delete action is not allowed.
     */
    @RequestMapping(value = "auth-method/delete", method = RequestMethod.POST)
    public ObjectResponse<DeleteAuthMethodResponse> deleteAuthMethod(@Valid @RequestBody ObjectRequest<DeleteAuthMethodRequest> request) throws AuthMethodNotFoundException, DeleteNotAllowedException {
        logger.info("Received deleteAuthMethod request, authentication method: {}", request.getRequestObject().getAuthMethod());
        final DeleteAuthMethodResponse response = authMethodService.deleteAuthMethod(request.getRequestObject());
        logger.info("The deleteAuthMethod request succeeded, authentication method: {}", request.getRequestObject().getAuthMethod());
        return new ObjectResponse<>(response);
    }

}
