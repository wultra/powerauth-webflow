/*
 * PowerAuth Web Flow and related software components
 * Copyright (C) 2017 Wultra s.r.o.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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
    @Operation(summary = "Create an authentication method")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Authentication method was created"),
            @ApiResponse(responseCode = "400", description = "Invalid request, error codes: REQUEST_VALIDATION_FAILED, AUTH_METHOD_ALREADY_EXISTS"),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
    })
    @PostMapping("auth-method")
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
    @Operation(summary = "Get authentication method list")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Authentication method list sent in response"),
            @ApiResponse(responseCode = "400", description = "Invalid request, error codes: REQUEST_VALIDATION_FAILED, INVALID_CONFIGURATION"),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
    })
    @GetMapping("auth-method")
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
    @Operation(summary = "Get authentication method list")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Authentication method list sent in response"),
            @ApiResponse(responseCode = "400", description = "Invalid request, error codes: REQUEST_VALIDATION_FAILED, INVALID_CONFIGURATION"),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
    })
    @PostMapping("auth-method/list")
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
    @Operation(summary = "Get authentication method list for user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Authentication method list sent in response"),
            @ApiResponse(responseCode = "400", description = "Invalid request, error codes: REQUEST_VALIDATION_FAILED, INVALID_CONFIGURATION"),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
    })
    @GetMapping("user/auth-method")
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
    @Operation(summary = "Get authentication method list for user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Authentication method list sent in response"),
            @ApiResponse(responseCode = "400", description = "Invalid request, error codes: REQUEST_VALIDATION_FAILED, INVALID_CONFIGURATION"),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
    })
    @PostMapping("user/auth-method/list")
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
     * @throws InvalidRequestException Thrown when request is invalid.
     * @throws InvalidConfigurationException Thrown when Next Step configuration is invalid.
     */
    @Operation(summary = "Enable an authentication method for user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Authentication method was enabled"),
            @ApiResponse(responseCode = "400", description = "Invalid request, error codes: REQUEST_VALIDATION_FAILED, INVALID_REQUEST, INVALID_CONFIGURATION"),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
    })
    @PostMapping("user/auth-method")
    public ObjectResponse<GetUserAuthMethodsResponse> enableAuthMethodForUser(@Valid @RequestBody ObjectRequest<UpdateAuthMethodRequest> request) throws InvalidRequestException, InvalidConfigurationException {
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
    @Operation(summary = "Disable an authentication method for user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Authentication method was disabled"),
            @ApiResponse(responseCode = "400", description = "Invalid request, error codes: REQUEST_VALIDATION_FAILED, INVALID_REQUEST, INVALID_CONFIGURATION"),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
    })
    @PostMapping("user/auth-method/delete")
    public ObjectResponse<GetUserAuthMethodsResponse> disableAuthMethodForUser(@Valid @RequestBody ObjectRequest<UpdateAuthMethodRequest> request) throws InvalidRequestException, InvalidConfigurationException {
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
    @Operation(summary = "Get list of authentication methods enabled for user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Authentication method list sent in response"),
            @ApiResponse(responseCode = "400", description = "Invalid request, error codes: REQUEST_VALIDATION_FAILED, INVALID_CONFIGURATION"),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
    })
    @GetMapping("user/auth-method/enabled")
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
    @Operation(summary = "Get list of authentication methods enabled for user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Authentication method list sent in response"),
            @ApiResponse(responseCode = "400", description = "Invalid request, error codes: REQUEST_VALIDATION_FAILED, INVALID_CONFIGURATION"),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
    })
    @PostMapping("user/auth-method/enabled/list")
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
    @Operation(summary = "Delete an authentication method")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Authentication method was deleted"),
            @ApiResponse(responseCode = "400", description = "Invalid request, error codes: REQUEST_VALIDATION_FAILED, AUTH_METHOD_NOT_FOUND, DELETE_NOT_ALLOWED"),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
    })
    @PostMapping("auth-method/delete")
    public ObjectResponse<DeleteAuthMethodResponse> deleteAuthMethod(@Valid @RequestBody ObjectRequest<DeleteAuthMethodRequest> request) throws AuthMethodNotFoundException, DeleteNotAllowedException {
        logger.info("Received deleteAuthMethod request, authentication method: {}", request.getRequestObject().getAuthMethod());
        final DeleteAuthMethodResponse response = authMethodService.deleteAuthMethod(request.getRequestObject());
        logger.info("The deleteAuthMethod request succeeded, authentication method: {}", request.getRequestObject().getAuthMethod());
        return new ObjectResponse<>(response);
    }

}
