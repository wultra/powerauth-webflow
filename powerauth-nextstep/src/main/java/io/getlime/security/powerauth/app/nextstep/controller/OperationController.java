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
import io.getlime.core.rest.model.base.request.ObjectRequest;
import io.getlime.core.rest.model.base.response.ObjectResponse;
import io.getlime.security.powerauth.app.nextstep.repository.model.entity.OperationEntity;
import io.getlime.security.powerauth.app.nextstep.repository.model.entity.OperationHistoryEntity;
import io.getlime.security.powerauth.app.nextstep.service.OperationPersistenceService;
import io.getlime.security.powerauth.app.nextstep.service.StepResolutionService;
import io.getlime.security.powerauth.lib.nextstep.model.entity.AuthStep;
import io.getlime.security.powerauth.lib.nextstep.model.entity.OperationFormData;
import io.getlime.security.powerauth.lib.nextstep.model.entity.OperationHistory;
import io.getlime.security.powerauth.lib.nextstep.model.exception.NextStepServiceException;
import io.getlime.security.powerauth.lib.nextstep.model.request.*;
import io.getlime.security.powerauth.lib.nextstep.model.response.CreateOperationResponse;
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
 * Controller class related to Next Step operations.
 *
 * @author Petr Dvorak, petr@lime-company.eu
 */
@Controller
public class OperationController {

    private OperationPersistenceService operationPersistenceService;
    private StepResolutionService stepResolutionService;

    /**
     * Controller constructor.
     * @param operationPersistenceService Operation persistence service.
     * @param stepResolutionService Step resolution service.
     */
    @Autowired
    public OperationController(OperationPersistenceService operationPersistenceService,
                               StepResolutionService stepResolutionService) {
        this.operationPersistenceService = operationPersistenceService;
        this.stepResolutionService = stepResolutionService;
    }

    /**
     * Create a new operation with given name and data.
     *
     * @param request Create operation request.
     * @return Create operation response.
     */
    @RequestMapping(value = "/operation", method = RequestMethod.POST)
    public @ResponseBody ObjectResponse<CreateOperationResponse> createOperation(@RequestBody ObjectRequest<CreateOperationRequest> request) {
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Received createOperation request, operation ID: {0}, operation name: {1}", new String[] {request.getRequestObject().getOperationId(), request.getRequestObject().getOperationName()});
        // resolve response based on dynamic step definitions
        CreateOperationResponse response = stepResolutionService.resolveNextStepResponse(request.getRequestObject());

        // persist new operation
        operationPersistenceService.createOperation(request.getRequestObject(), response);

        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "The createOperation request succeeded, operation ID: {0}, result: {1}", new String[]{response.getOperationId(), response.getResult().toString()});
        for (AuthStep step: response.getSteps()) {
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Next authentication method for operation ID: {0}, authentication method: {1}", new String[]{response.getOperationId(), step.getAuthMethod().toString()});
        }
        return new ObjectResponse<>(response);
    }

    /**
     * Update operation with given ID with a previous authentication step result.
     *
     * @param request Update operation request.
     * @return Update operation response.
     * @throws NextStepServiceException Thrown when next step resolution fails.
     */
    @RequestMapping(value = "/operation", method = RequestMethod.PUT)
    public @ResponseBody ObjectResponse<UpdateOperationResponse> updateOperation(@RequestBody ObjectRequest<UpdateOperationRequest> request) throws NextStepServiceException {
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Received updateOperation request, operation ID: {0}", request.getRequestObject().getOperationId());
        // resolve response based on dynamic step definitions
        UpdateOperationResponse response = stepResolutionService.resolveNextStepResponse(request.getRequestObject());

        // persist operation update
        operationPersistenceService.updateOperation(request.getRequestObject(), response);

        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "The updateOperation request succeeded, operation ID: {0}, result: {1}", new String[]{response.getOperationId(), response.getResult().toString()});
        for (AuthStep step: response.getSteps()) {
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Next authentication method for operation ID: {0}, authentication method: {1}", new String[]{response.getOperationId(), step.getAuthMethod().toString()});
        }
        return new ObjectResponse<>(response);
    }

    /**
     * Get detail of an operation with given ID.
     *
     * @param request Get operation detail request.
     * @return Get operation detail response.
     */
    @RequestMapping(value = "/operation/detail", method = RequestMethod.POST)
    public @ResponseBody ObjectResponse<GetOperationDetailResponse> operationDetail(@RequestBody ObjectRequest<GetOperationDetailRequest> request) {
        // Log level is FINE to avoid flooding logs, this endpoint is used all the time.
        Logger.getLogger(this.getClass().getName()).log(Level.FINE, "Received operationDetail request, operation ID: {0}", request.getRequestObject().getOperationId());

        GetOperationDetailRequest requestObject = request.getRequestObject();

        GetOperationDetailResponse response = new GetOperationDetailResponse();

        OperationEntity operation = operationPersistenceService.getOperation(requestObject.getOperationId());
        if (operation == null) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Invalid operationId: " + requestObject.getOperationId());
            throw new IllegalArgumentException("Invalid operationId: " + requestObject.getOperationId());
        }
        response.setOperationId(operation.getOperationId());
        response.setOperationName(operation.getOperationName());
        response.setUserId(operation.getUserId());
        response.setOperationData(operation.getOperationData());
        if (operation.getResult() != null) {
            response.setResult(operation.getResult());
        }
        assignFormData(response, operation);

        for (OperationHistoryEntity history: operation.getOperationHistory()) {
            OperationHistory h = new OperationHistory();
            h.setAuthMethod(history.getRequestAuthMethod());
            h.setRequestAuthStepResult(history.getRequestAuthStepResult());
            h.setAuthResult(history.getResponseResult());
            response.getHistory().add(h);
        }

        // set chosen authentication method
        OperationHistoryEntity currentHistory = operation.getCurrentOperationHistoryEntity();
        if (currentHistory != null) {
            response.setChosenAuthMethod(currentHistory.getChosenAuthMethod());
        }

        // add steps from current response
        response.getSteps().addAll(operationPersistenceService.getResponseAuthSteps(operation));

        // set number of remaining authentication attempts
        response.setRemainingAttempts(stepResolutionService.getNumberOfRemainingAttempts(operation));

        response.setTimestampCreated(operation.getTimestampCreated());
        response.setTimestampExpires(operation.getTimestampExpires());

        Logger.getLogger(this.getClass().getName()).log(Level.FINE, "The operationDetail request succeeded, operation ID: {0}", response.getOperationId());
        return new ObjectResponse<>(response);
    }


    /**
     * Get the list of pending operations for user.
     *
     * @param request Get pending operations request.
     * @return List with operation details.
     */
    @RequestMapping(value = "/user/operation/list", method = RequestMethod.POST)
    public @ResponseBody ObjectResponse<List<GetOperationDetailResponse>> getPendingOperations(@RequestBody ObjectRequest<GetPendingOperationsRequest> request) {
        // Log level is FINE to avoid flooding logs, this endpoint is used all the time.
        Logger.getLogger(this.getClass().getName()).log(Level.FINE, "Received getPendingOperations request, user ID: {0}, authentication method: {1}", new String[] {request.getRequestObject().getUserId(), request.getRequestObject().getAuthMethod().toString()});

        GetPendingOperationsRequest requestObject = request.getRequestObject();

        List<GetOperationDetailResponse> responseList = new ArrayList<>();

        List<OperationEntity> operations = operationPersistenceService.getPendingOperations(requestObject.getUserId(), requestObject.getAuthMethod());
        if (operations == null) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Invalid query for pending operations, userId: " + requestObject.getUserId()
                    + ", authMethod: " + requestObject.getAuthMethod());
            throw new IllegalArgumentException("Invalid query for pending operations, userId: " + requestObject.getUserId()
                    + ", authMethod: " + requestObject.getAuthMethod());
        }
        for (OperationEntity operation : operations) {
            GetOperationDetailResponse response = new GetOperationDetailResponse();
            response.setOperationId(operation.getOperationId());
            response.setOperationName(operation.getOperationName());
            response.setUserId(operation.getUserId());
            response.setOperationData(operation.getOperationData());
            if (operation.getResult() != null) {
                response.setResult(operation.getResult());
            }
            assignFormData(response, operation);
            response.setTimestampCreated(operation.getTimestampCreated());
            response.setTimestampExpires(operation.getTimestampExpires());
            responseList.add(response);
        }

        Logger.getLogger(this.getClass().getName()).log(Level.FINE, "The getPendingOperations request succeeded, operation list size: ", responseList.size());
        return new ObjectResponse<>(responseList);
    }

    /**
     * Update operation with updated form data.
     *
     * @param request Update operation request.
     * @return Update operation response.
     */
    @RequestMapping(value = "/operation/formData", method = RequestMethod.PUT)
    public @ResponseBody ObjectResponse updateOperationFormData(@RequestBody ObjectRequest<UpdateFormDataRequest> request) {
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Received updateOperationFormData request, operation ID: {0}", request.getRequestObject().getOperationId());
        // persist operation form data update
        operationPersistenceService.updateFormData(request.getRequestObject());
        Logger.getLogger(this.getClass().getName()).log(Level.FINE, "The updateOperationFormData request succeeded");
        return new ObjectResponse();
    }

    /**
     * Update operation with chosen authentication method.
     * @param request Update operation request.
     * @return Update operation response.
     */
    @RequestMapping(value = "/operation/chosenAuthMethod", method = RequestMethod.PUT)
    public @ResponseBody ObjectResponse updateChosenAuthMethod(@RequestBody ObjectRequest<UpdateChosenAuthMethodRequest> request) {
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Received updateChosenAuthMethod request, operation ID: {0}, chosen authentication method: {1}", new String[]{request.getRequestObject().getOperationId(), request.getRequestObject().getChosenAuthMethod().toString()});
        // persist operation form data update
        operationPersistenceService.updateChosenAuthMethod(request.getRequestObject());
        Logger.getLogger(this.getClass().getName()).log(Level.FINE, "The updateChosenAuthMethod request succeeded");
        return new ObjectResponse();
    }

    /**
     * In case operation entity has serialized form data, attempt to deserialize the
     * object and assign it to the response with operation detail.
     * @param response Response to be enriched by operation detail.
     * @param operation Database entity representing operation.
     */
    private void assignFormData(GetOperationDetailResponse response, OperationEntity operation) {
        if (operation.getOperationFormData() != null) {
            //TODO: This needs to be written better, see issue #39.
            OperationFormData formData = null;
            try {
                formData = new ObjectMapper().readValue(operation.getOperationFormData(), OperationFormData.class);
            } catch (IOException ex) {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Error while deserializing operation display formData", ex);
            }
            response.setFormData(formData);
        }
    }

}
