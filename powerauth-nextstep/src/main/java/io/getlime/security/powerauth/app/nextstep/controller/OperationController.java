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
import io.getlime.security.powerauth.lib.nextstep.model.entity.OperationDisplayDetails;
import io.getlime.security.powerauth.lib.nextstep.model.entity.OperationHistory;
import io.getlime.security.powerauth.lib.nextstep.model.request.CreateOperationRequest;
import io.getlime.security.powerauth.lib.nextstep.model.request.GetOperationDetailRequest;
import io.getlime.security.powerauth.lib.nextstep.model.request.GetPendingOperationsRequest;
import io.getlime.security.powerauth.lib.nextstep.model.request.UpdateOperationRequest;
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
 * @author Petr Dvorak
 */
@Controller
public class OperationController {

    private OperationPersistenceService operationPersistenceService;
    private StepResolutionService stepResolutionService;

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
        // resolve response based on dynamic step definitions
        CreateOperationResponse response = stepResolutionService.resolveNextStepResponse(request.getRequestObject());

        // persist new operation
        operationPersistenceService.createOperation(request.getRequestObject(), response);

        return new ObjectResponse<>(response);
    }

    /**
     * Update operation with given ID with a previous authentication step result.
     *
     * @param request Update operation request.
     * @return Update operation response.
     */
    @RequestMapping(value = "/operation", method = RequestMethod.PUT)
    public @ResponseBody ObjectResponse<UpdateOperationResponse> updateOperation(@RequestBody ObjectRequest<UpdateOperationRequest> request) {
        // resolve response based on dynamic step definitions
        UpdateOperationResponse response = stepResolutionService.resolveNextStepResponse(request.getRequestObject());

        // persist operation update
        operationPersistenceService.updateOperation(request.getRequestObject(), response);

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

        GetOperationDetailRequest requestObject = request.getRequestObject();

        GetOperationDetailResponse response = new GetOperationDetailResponse();

        OperationEntity operation = operationPersistenceService.getOperation(requestObject.getOperationId());
        if (operation == null) {
            throw new IllegalArgumentException("Invalid operationId: " + requestObject.getOperationId());
        }
        response.setOperationId(operation.getOperationId());
        response.setOperationName(operation.getOperationName());
        response.setUserId(operation.getUserId());
        response.setOperationData(operation.getOperationData());
        if (operation.getResult() != null) {
            response.setResult(operation.getResult());
        }
        assignDisplayDetails(response, operation);

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
            response.setOperationName(operation.getOperationName());
            response.setUserId(operation.getUserId());
            response.setOperationData(operation.getOperationData());
            if (operation.getResult() != null) {
                response.setResult(operation.getResult());
            }
            assignDisplayDetails(response, operation);
            response.setTimestampCreated(operation.getTimestampCreated());
            response.setTimestampExpires(operation.getTimestampExpires());
            responseList.add(response);
        }
        return new ObjectResponse<>(responseList);
    }

    /**
     * In case operation entity has serialized display details, attempt to deserialize the
     * object and assign it to the response with operation detail.
     * @param response Reponse to be enriched by operation detail.
     * @param operation Database entity representing operation.
     */
    private void assignDisplayDetails(GetOperationDetailResponse response, OperationEntity operation) {
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
    }

}
