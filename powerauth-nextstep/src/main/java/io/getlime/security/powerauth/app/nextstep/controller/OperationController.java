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

import io.getlime.security.powerauth.app.nextstep.configuration.NextStepServerConfiguration;
import io.getlime.security.powerauth.app.nextstep.repository.AuthMethodRepository;
import io.getlime.security.powerauth.app.nextstep.repository.model.entity.AuthMethodEntity;
import io.getlime.security.powerauth.app.nextstep.repository.model.entity.OperationEntity;
import io.getlime.security.powerauth.app.nextstep.repository.model.entity.OperationHistoryEntity;
import io.getlime.security.powerauth.app.nextstep.service.OperationPersistenceService;
import io.getlime.security.powerauth.app.nextstep.service.StepResolutionService;
import io.getlime.security.powerauth.app.nextstep.service.UserPrefsService;
import io.getlime.security.powerauth.lib.nextstep.model.base.Request;
import io.getlime.security.powerauth.lib.nextstep.model.base.Response;
import io.getlime.security.powerauth.lib.nextstep.model.entity.OperationHistory;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthMethod;
import io.getlime.security.powerauth.lib.nextstep.model.request.CreateOperationRequest;
import io.getlime.security.powerauth.lib.nextstep.model.request.GetOperationDetailRequest;
import io.getlime.security.powerauth.lib.nextstep.model.request.GetPendingOperationsRequest;
import io.getlime.security.powerauth.lib.nextstep.model.request.UpdateOperationRequest;
import io.getlime.security.powerauth.lib.nextstep.model.response.CreateOperationResponse;
import io.getlime.security.powerauth.lib.nextstep.model.response.GetAuthMethodsResponse;
import io.getlime.security.powerauth.lib.nextstep.model.response.GetOperationDetailResponse;
import io.getlime.security.powerauth.lib.nextstep.model.response.UpdateOperationResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Controller class related to PowerAuth activation management.
 *
 * @author Petr Dvorak
 */
@Controller
public class OperationController {

    private OperationPersistenceService operationPersistenceService;
    private StepResolutionService stepResolutionService;
    private NextStepServerConfiguration nextStepServerConfiguration;
    private AuthMethodRepository authMethodRepository;
    private UserPrefsService userPrefsService;

    @Autowired
    public OperationController(OperationPersistenceService operationPersistenceService,
                               StepResolutionService stepResolutionService, NextStepServerConfiguration nextStepServerConfiguration,
                               AuthMethodRepository authMethodRepository, UserPrefsService userPrefsService) {
        this.operationPersistenceService = operationPersistenceService;
        this.stepResolutionService = stepResolutionService;
        this.nextStepServerConfiguration = nextStepServerConfiguration;
        this.authMethodRepository = authMethodRepository;
        this.userPrefsService = userPrefsService;
    }

    /**
     * Create a new operation with given name and data.
     *
     * @param request Create operation request.
     * @return Create operation response.
     */
    @RequestMapping(value = "/operation", method = RequestMethod.POST)
    public @ResponseBody
    Response<CreateOperationResponse> createOperation(@RequestBody Request<CreateOperationRequest> request) {
        // resolve response based on dynamic step definitions
        CreateOperationResponse response = stepResolutionService.resolveNextStepResponse(request.getRequestObject());

        // persist new operation
        operationPersistenceService.createOperation(request.getRequestObject(), response);

        return new Response<>(Response.Status.OK, response);
    }

    /**
     * Update operation with given ID with a previous authentication step result.
     *
     * @param request Update operation request.
     * @return Update operation response.
     */
    @RequestMapping(value = "/operation", method = RequestMethod.PUT)
    public @ResponseBody
    Response<UpdateOperationResponse> updateOperation(@RequestBody Request<UpdateOperationRequest> request) {
        // resolve response based on dynamic step definitions
        UpdateOperationResponse response = stepResolutionService.resolveNextStepResponse(request.getRequestObject());

        // persist operation update
        operationPersistenceService.updateOperation(request.getRequestObject(), response);

        return new Response<>(Response.Status.OK, response);
    }

    /**
     * Get detail of an operation with given ID.
     *
     * @param request Get operation detail request.
     * @return Get operation detail response.
     */
    @RequestMapping(value = "/operation/detail", method = RequestMethod.POST)
    public @ResponseBody
    Response<GetOperationDetailResponse> operationDetail(@RequestBody Request<GetOperationDetailRequest> request) {

        GetOperationDetailRequest requestObject = request.getRequestObject();

        GetOperationDetailResponse response = new GetOperationDetailResponse();

        OperationEntity operation = operationPersistenceService.getOperation(requestObject.getOperationId());
        if (operation == null) {
            throw new IllegalArgumentException("Invalid operationId: " + requestObject.getOperationId());
        }
        response.setOperationId(operation.getOperationId());
        response.setUserId(operation.getUserId());
        response.setOperationData(operation.getOperationData());
        if (operation.getResult() != null) {
            response.setResult(operation.getResult());
        }

        for (OperationHistoryEntity history: operation.getOperationHistory()) {
            OperationHistory h = new OperationHistory();
            h.setAuthMethod(history.getRequestAuthMethod());
            h.setAuthResult(history.getResponseResult());
            response.getHistory().add(h);
        }

        response.setTimestampCreated(operation.getTimestampCreated());
        response.setTimestampExpires(operation.getTimestampExpires());
        return new Response<>(Response.Status.OK, response);
    }


    /**
     * Get the list of pending operations for user.
     *
     * @param request Get pending operations request.
     * @return List with operation details.
     */
    @RequestMapping(value = "/user/operation/list", method = RequestMethod.POST)
    public @ResponseBody
    Response<List<GetOperationDetailResponse>> getPendingOperations(@RequestBody Request<GetPendingOperationsRequest> request) {

        GetPendingOperationsRequest requestObject = request.getRequestObject();

        List<GetOperationDetailResponse> responseList = new ArrayList<>();

        List<OperationEntity> operations = operationPersistenceService.getPendingOperations(requestObject.getUserId(), requestObject.getAuthMethod());
        if (operations == null) {
            throw new IllegalArgumentException("Invalid query for pending operations, userId: " + requestObject.getUserId()
                    + ", authMethod: " + requestObject.getAuthMethod());
        }
        for (OperationEntity operation : operations) {
            GetOperationDetailResponse response = new GetOperationDetailResponse();
            response.setOperationId(operation.getOperationId());
            response.setUserId(operation.getUserId());
            response.setOperationData(operation.getOperationData());
            if (operation.getResult() != null) {
                response.setResult(operation.getResult());
            }
            response.setTimestampCreated(operation.getTimestampCreated());
            response.setTimestampExpires(operation.getTimestampExpires());
            responseList.add(response);
        }
        return new Response<>(Response.Status.OK, responseList);
    }

    /**
     * Get all authentication methods supported by Next Step server.
     *
     * @return List of authentication methods wrapped in GetAuthMethodResponse.
     */
    @RequestMapping(value = "/auth-method", method = RequestMethod.GET)
    public @ResponseBody
    Response<GetAuthMethodsResponse> getAuthMethods() {
        List<AuthMethodEntity> authMethods = authMethodRepository.findAllAuthMethods();
        List<AuthMethod> responseList = new ArrayList<>();
        if (authMethods == null) {
            throw new IllegalArgumentException("No authentication method is configured in Next Step server.");
        }
        GetAuthMethodsResponse response = new GetAuthMethodsResponse();
        for (AuthMethodEntity authMethod : authMethods) {
            responseList.add(authMethod.getAuthMethod());
        }
        response.setAuthMethods(responseList);
        return new Response<>(Response.Status.OK, response);
    }

    /**
     * Get all enabled authentication methods for given user.
     *
     * @param userId User ID
     * @return List of enabled authentication methods for given user wrapped in GetAuthMethodResponse.
     */
    @RequestMapping(value = "/user/{userId}/auth-method", method = RequestMethod.GET)
    public @ResponseBody
    Response<GetAuthMethodsResponse> getAuthMethodsEnabledForUser(@PathVariable String userId) {
        List<AuthMethod> authMethods = userPrefsService.listAuthMethodsEnabledForUser(userId);
        GetAuthMethodsResponse response = new GetAuthMethodsResponse();
        response.setAuthMethods(authMethods);
        return new Response<>(Response.Status.OK, response);
    }

    /**
     * Enable an authentication method for given user.
     *
     * @param userId     User ID
     * @param authMethod Authentication method
     * @return List of enabled authentication methods for given user wrapped in GetAuthMethodResponse.
     */
    @RequestMapping(value = "/user/{userId}/auth-method/{authMethod}", method = RequestMethod.POST)
    public @ResponseBody
    Response<GetAuthMethodsResponse> enableAuthMethodForUser(@PathVariable String userId, @PathVariable AuthMethod authMethod) {
        userPrefsService.updateAuthMethodForUser(userId, authMethod, true);
        List<AuthMethod> authMethods = userPrefsService.listAuthMethodsEnabledForUser(userId);
        GetAuthMethodsResponse response = new GetAuthMethodsResponse();
        response.setAuthMethods(authMethods);
        return new Response<>(Response.Status.OK, response);
    }

    /**
     * Disable an authentication method for given user.
     *
     * @param userId     User ID
     * @param authMethod Authentication method
     * @return List of enabled authentication methods for given user wrapped in GetAuthMethodResponse.
     */
    @RequestMapping(value = "/user/{userId}/auth-method/{authMethod}", method = RequestMethod.DELETE)
    public @ResponseBody
    Response<GetAuthMethodsResponse> disableAuthMethodForUser(@PathVariable String userId, @PathVariable AuthMethod authMethod) {
        userPrefsService.updateAuthMethodForUser(userId, authMethod, false);
        List<AuthMethod> authMethods = userPrefsService.listAuthMethodsEnabledForUser(userId);
        GetAuthMethodsResponse response = new GetAuthMethodsResponse();
        response.setAuthMethods(authMethods);
        return new Response<>(Response.Status.OK, response);
    }

}
