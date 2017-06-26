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

import com.fasterxml.jackson.databind.ObjectMapper;
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
import io.getlime.security.powerauth.lib.nextstep.model.entity.OperationDisplayDetails;
import io.getlime.security.powerauth.lib.nextstep.model.entity.OperationHistory;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthMethod;
import io.getlime.security.powerauth.lib.nextstep.model.request.*;
import io.getlime.security.powerauth.lib.nextstep.model.response.CreateOperationResponse;
import io.getlime.security.powerauth.lib.nextstep.model.response.GetAuthMethodsResponse;
import io.getlime.security.powerauth.lib.nextstep.model.response.GetOperationDetailResponse;
import io.getlime.security.powerauth.lib.nextstep.model.response.UpdateOperationResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

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
        if (operation.getOperationDisplayDetails() != null) {
            //TODO: This needs to be written better, see issue #39.
            OperationDisplayDetails details = null;
            try {
                details = new ObjectMapper().readValue(operation.getOperationDisplayDetails(), OperationDisplayDetails.class);
            } catch (IOException ex) {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Error while deserializing operation display details", ex);
            }
            response.setDisplayDetails(details);
        }

        for (OperationHistoryEntity history: operation.getOperationHistory()) {
            OperationHistory h = new OperationHistory();
            h.setAuthMethod(history.getRequestAuthMethod());
            h.setAuthResult(history.getResponseResult());
            response.getHistory().add(h);
        }

        // add steps from current response
        response.getSteps().addAll(operationPersistenceService.getResponseAuthSteps(operation));

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
            if (operation.getOperationDisplayDetails() != null) {
                //TODO: This needs to be written better, see issue #39.
                OperationDisplayDetails details = null;
                try {
                    details = new ObjectMapper().readValue(operation.getOperationDisplayDetails(), OperationDisplayDetails.class);
                } catch (IOException ex) {
                    Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Error while deserializing operation display details", ex);
                }
                response.setDisplayDetails(details);
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
     * @param request Get auth methods request. Use null userId in request.
     * @return List of authentication methods wrapped in GetAuthMethodResponse.
     */
    @RequestMapping(value = "/auth-method/list", method = RequestMethod.POST)
    public @ResponseBody
    Response<GetAuthMethodsResponse> getAuthMethods(@RequestBody Request<GetAuthMethodsRequest> request) {
        GetAuthMethodsRequest requestObject = request.getRequestObject();
        String userId = requestObject.getUserId();
        if (userId != null) {
            throw new IllegalArgumentException("Parameter userId is not null in request object, however null value was expected.");
        }
        List<AuthMethodEntity> authMethods = authMethodRepository.findAllAuthMethods();
        List<AuthMethod> responseList = new ArrayList<>();
        if (authMethods == null || authMethods.isEmpty()) {
            throw new IllegalStateException("No authentication method is configured in Next Step server.");
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
     * @param request Get auth methods request. Use non-null userId in request.
     * @return List of enabled authentication methods for given user wrapped in GetAuthMethodResponse.
     */
    @RequestMapping(value = "/user/auth-method/list", method = RequestMethod.POST)
    public @ResponseBody
    Response<GetAuthMethodsResponse> getAuthMethodsEnabledForUser(@RequestBody Request<GetAuthMethodsRequest> request) {
        GetAuthMethodsRequest requestObject = request.getRequestObject();
        String userId = requestObject.getUserId();
        if (userId == null) {
            throw new IllegalArgumentException("Parameter userId is null in request object.");
        }
        List<AuthMethod> authMethods = userPrefsService.listAuthMethodsEnabledForUser(userId);
        GetAuthMethodsResponse response = new GetAuthMethodsResponse();
        response.setAuthMethods(authMethods);
        return new Response<>(Response.Status.OK, response);
    }

    /**
     * Enable an authentication method for given user.
     *
     * @param request Update auth method request. Use non-null userId in request and specify authMethod.
     * @return List of enabled authentication methods for given user wrapped in GetAuthMethodResponse.
     */
    @RequestMapping(value = "/user/auth-method", method = RequestMethod.POST)
    public @ResponseBody
    Response<GetAuthMethodsResponse> enableAuthMethodForUser(@RequestBody Request<UpdateAuthMethodRequest> request) {
        UpdateAuthMethodRequest requestObject = request.getRequestObject();
        String userId = requestObject.getUserId();
        if (userId == null) {
            throw new IllegalArgumentException("Parameter userId is null in request object.");
        }
        AuthMethod authMethod = requestObject.getAuthMethod();
        if (authMethod == null) {
            throw new IllegalArgumentException("Parameter authMethod is null in request object.");
        }
        userPrefsService.updateAuthMethodForUser(userId, authMethod, true);
        List<AuthMethod> authMethods = userPrefsService.listAuthMethodsEnabledForUser(userId);
        GetAuthMethodsResponse response = new GetAuthMethodsResponse();
        response.setAuthMethods(authMethods);
        return new Response<>(Response.Status.OK, response);
    }

    /**
     * Disable an authentication method for given user.
     *
     * @param request Update auth method request. Use non-null userId in request and specify authMethod.
     * @return List of enabled authentication methods for given user wrapped in GetAuthMethodResponse.
     */
    @RequestMapping(value = "/user/auth-method", method = RequestMethod.DELETE)
    public @ResponseBody
    Response<GetAuthMethodsResponse> disableAuthMethodForUser(@RequestBody Request<UpdateAuthMethodRequest> request) {
        UpdateAuthMethodRequest requestObject = request.getRequestObject();
        String userId = requestObject.getUserId();
        if (userId == null) {
            throw new IllegalArgumentException("Parameter userId is null in request object.");
        }
        AuthMethod authMethod = requestObject.getAuthMethod();
        if (authMethod == null) {
            throw new IllegalArgumentException("Parameter authMethod is null in request object.");
        }
        userPrefsService.updateAuthMethodForUser(userId, authMethod, false);
        List<AuthMethod> authMethods = userPrefsService.listAuthMethodsEnabledForUser(userId);
        GetAuthMethodsResponse response = new GetAuthMethodsResponse();
        response.setAuthMethods(authMethods);
        return new Response<>(Response.Status.OK, response);
    }

}
