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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.getlime.core.rest.model.base.request.ObjectRequest;
import io.getlime.core.rest.model.base.response.ObjectResponse;
import io.getlime.core.rest.model.base.response.Response;
import io.getlime.security.powerauth.app.nextstep.repository.model.entity.OperationAfsActionEntity;
import io.getlime.security.powerauth.app.nextstep.repository.model.entity.OperationEntity;
import io.getlime.security.powerauth.app.nextstep.repository.model.entity.OperationHistoryEntity;
import io.getlime.security.powerauth.app.nextstep.service.OperationConfigurationService;
import io.getlime.security.powerauth.app.nextstep.service.OperationPersistenceService;
import io.getlime.security.powerauth.app.nextstep.service.StepResolutionService;
import io.getlime.security.powerauth.lib.nextstep.model.entity.*;
import io.getlime.security.powerauth.lib.nextstep.model.exception.NextStepServiceException;
import io.getlime.security.powerauth.lib.nextstep.model.exception.OperationAlreadyExistsException;
import io.getlime.security.powerauth.lib.nextstep.model.exception.OperationNotConfiguredException;
import io.getlime.security.powerauth.lib.nextstep.model.exception.OperationNotFoundException;
import io.getlime.security.powerauth.lib.nextstep.model.request.*;
import io.getlime.security.powerauth.lib.nextstep.model.response.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Controller class related to Next Step operations.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
@Controller
public class OperationController {

    private static final Logger logger = LoggerFactory.getLogger(OperationController.class);

    private final OperationPersistenceService operationPersistenceService;
    private final OperationConfigurationService operationConfigurationService;
    private final StepResolutionService stepResolutionService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Controller constructor.
     * @param operationPersistenceService Operation persistence service.
     * @param operationConfigurationService Operation configuration service.
     * @param stepResolutionService Step resolution service.
     */
    @Autowired
    public OperationController(OperationPersistenceService operationPersistenceService, OperationConfigurationService operationConfigurationService,
                               StepResolutionService stepResolutionService) {
        this.operationPersistenceService = operationPersistenceService;
        this.operationConfigurationService = operationConfigurationService;
        this.stepResolutionService = stepResolutionService;
    }

    /**
     * Create a new operation with given name and data.
     *
     * @param request Create operation request.
     * @return Create operation response.
     * @throws OperationAlreadyExistsException Thrown when operation already exists.
     */
    @RequestMapping(value = "/operation", method = RequestMethod.POST)
    public @ResponseBody ObjectResponse<CreateOperationResponse> createOperation(@RequestBody ObjectRequest<CreateOperationRequest> request) throws OperationAlreadyExistsException {
        logger.info("Received createOperation request, operation ID: {}, operation name: {}", request.getRequestObject().getOperationId(), request.getRequestObject().getOperationName());
        // resolve response based on dynamic step definitions
        CreateOperationResponse response = stepResolutionService.resolveNextStepResponse(request.getRequestObject());

        // persist new operation
        operationPersistenceService.createOperation(request.getRequestObject(), response);

        logger.info("The createOperation request succeeded, operation ID: {}, result: {}", response.getOperationId(), response.getResult().toString());
        for (AuthStep step: response.getSteps()) {
            logger.info("Next authentication method for operation ID: {}, authentication method: {}", response.getOperationId(), step.getAuthMethod().toString());
        }
        return new ObjectResponse<>(response);
    }

    /**
     * Update operation with given ID with a previous authentication step result (PUT method).
     *
     * @param request Update operation request.
     * @return Update operation response.
     * @throws NextStepServiceException Thrown when next step resolution fails.
     */
    @RequestMapping(value = "/operation", method = RequestMethod.PUT)
    public @ResponseBody ObjectResponse<UpdateOperationResponse> updateOperation(@RequestBody ObjectRequest<UpdateOperationRequest> request) throws NextStepServiceException {
        return updateOperationImpl(request);
    }

    /**
     * Update operation with given ID with a previous authentication step result (POST method alternative).
     *
     * @param request Update operation request.
     * @return Update operation response.
     * @throws NextStepServiceException Thrown when next step resolution fails.
     */
    @RequestMapping(value = "/operation/update", method = RequestMethod.POST)
    public @ResponseBody ObjectResponse<UpdateOperationResponse> updateOperationPost(@RequestBody ObjectRequest<UpdateOperationRequest> request) throws NextStepServiceException {
        return updateOperationImpl(request);
    }

    private ObjectResponse<UpdateOperationResponse> updateOperationImpl(ObjectRequest<UpdateOperationRequest> request) throws NextStepServiceException {
        logger.info("Received updateOperation request, operation ID: {}", request.getRequestObject().getOperationId());
        // resolve response based on dynamic step definitions
        UpdateOperationResponse response = stepResolutionService.resolveNextStepResponse(request.getRequestObject());

        // persist operation update
        operationPersistenceService.updateOperation(request.getRequestObject(), response);

        logger.info("The updateOperation request succeeded, operation ID: {}, result: {}", response.getOperationId(), response.getResult().toString());
        for (AuthStep step: response.getSteps()) {
            logger.info("Next authentication method for operation ID: {}, authentication method: {}", response.getOperationId(), step.getAuthMethod().toString());
        }
        return new ObjectResponse<>(response);
    }

    /**
     * Assign user ID and organization ID to and operation.
     *
     * @param request Update operation user request.
     * @return Response.
     * @throws NextStepServiceException Thrown when update fails in Next Step.
     */
    @RequestMapping(value = "/operation/user", method = RequestMethod.PUT)
    public @ResponseBody Response updateOperationUser(@RequestBody ObjectRequest<UpdateOperationUserRequest> request) throws NextStepServiceException {
        return updateOperationUserImpl(request);
    }

    /**
     * Assign user ID and organization ID to and operation (POST alternative).
     *
     * @param request Update operation user request.
     * @return Response.
     * @throws NextStepServiceException Thrown when update fails in Next Step.
     */
    @RequestMapping(value = "/operation/user/update", method = RequestMethod.POST)
    public @ResponseBody Response updateOperationUserPost(@RequestBody ObjectRequest<UpdateOperationUserRequest> request) throws NextStepServiceException {
        return updateOperationUserImpl(request);
    }

    private Response updateOperationUserImpl(ObjectRequest<UpdateOperationUserRequest> request) throws NextStepServiceException {
        String operationId = request.getRequestObject().getOperationId();
        String userId = request.getRequestObject().getUserId();
        String organizationId = request.getRequestObject().getOrganizationId();
        logger.info("Received updateOperationUser request, operation ID: {}, user ID: {}, organization ID: {}", operationId, userId, organizationId);

        // persist operation user update
        operationPersistenceService.updateOperationUser(request.getRequestObject());

        logger.info("The updateOperationUser request succeeded, operation ID: {}, user ID: {}, organization ID: {}", operationId, userId, organizationId);
        return new Response();
    }

    /**
     * Get detail of an operation with given ID.
     *
     * @param request Get operation detail request.
     * @return Get operation detail response.
     * @throws OperationNotFoundException Thrown when operation does not exist.
     */
    @RequestMapping(value = "/operation/detail", method = RequestMethod.POST)
    public @ResponseBody ObjectResponse<GetOperationDetailResponse> operationDetail(@RequestBody ObjectRequest<GetOperationDetailRequest> request) throws OperationNotFoundException {
        // Log level is FINE to avoid flooding logs, this endpoint is used all the time.
        logger.debug("Received operationDetail request, operation ID: {}", request.getRequestObject().getOperationId());

        GetOperationDetailRequest requestObject = request.getRequestObject();

        GetOperationDetailResponse response = new GetOperationDetailResponse();

        OperationEntity operation = operationPersistenceService.getOperation(requestObject.getOperationId());
        response.setOperationId(operation.getOperationId());
        response.setOperationName(operation.getOperationName());
        response.setUserId(operation.getUserId());
        response.setOrganizationId(operation.getOrganizationId());
        response.setOperationData(operation.getOperationData());
        if (operation.getResult() != null) {
            response.setResult(operation.getResult());
        }
        assignFormData(response, operation);
        assignApplicationContext(response, operation);
        assignOperationHistory(response, operation);
        assignAfsActions(response, operation);

        // add steps from current response
        response.getSteps().addAll(operationPersistenceService.getResponseAuthSteps(operation));

        // set number of remaining authentication attempts
        response.setRemainingAttempts(stepResolutionService.getNumberOfRemainingAttempts(operation));

        response.setTimestampCreated(operation.getTimestampCreated());
        response.setTimestampExpires(operation.getTimestampExpires());

        logger.debug("The operationDetail request succeeded, operation ID: {}", response.getOperationId());
        return new ObjectResponse<>(response);
    }

    /**
     * Get configuration of an operation with given operation name.
     *
     * @param request Get operation configuration request.
     * @return Get operation configuration response.
     * @throws OperationNotConfiguredException Thrown when operation is not configured.
     */
    @RequestMapping(value = "/operation/config/detail", method = RequestMethod.POST)
    public @ResponseBody ObjectResponse<GetOperationConfigDetailResponse> getOperationConfigDetail(@RequestBody ObjectRequest<GetOperationConfigDetailRequest> request) throws OperationNotConfiguredException {
        // Log level is FINE to avoid flooding logs, this endpoint is used all the time.
        logger.debug("Received getOperationConfigDetail request, operation name: {}", request.getRequestObject().getOperationName());

        GetOperationConfigDetailRequest requestObject = request.getRequestObject();

        GetOperationConfigDetailResponse response = operationConfigurationService.getOperationConfig(requestObject.getOperationName());

        logger.debug("The getOperationConfigDetail request succeeded, operation name: {}", request.getRequestObject().getOperationName());
        return new ObjectResponse<>(response);
    }

    /**
     * Get configurations of all operations.
     *
     * @param request Get configurations of all operations request.
     * @return Get operation configurations response.
     */
    @RequestMapping(value = "/operation/config/list", method = RequestMethod.POST)
    public @ResponseBody ObjectResponse<GetOperationConfigListResponse> getOperationConfigList(@RequestBody ObjectRequest<GetOperationConfigListRequest> request) {
        // Log level is FINE to avoid flooding logs, this endpoint is used all the time.
        logger.debug("Received getOperationConfigList request");

        GetOperationConfigListResponse response = operationConfigurationService.getOperationConfigs();

        logger.debug("The getOperationConfigList request succeeded");
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
        logger.debug("Received getPendingOperations request, user ID: {}", request.getRequestObject().getUserId());

        GetPendingOperationsRequest requestObject = request.getRequestObject();

        List<GetOperationDetailResponse> responseList = new ArrayList<>();

        List<OperationEntity> operations = operationPersistenceService.getPendingOperations(requestObject.getUserId(), requestObject.isMobileTokenOnly());
        if (operations == null) {
            logger.error("Invalid query for pending operations, user ID: " + requestObject.getUserId());
            throw new IllegalArgumentException("Invalid query for pending operations, user ID: " + requestObject.getUserId());
        }
        for (OperationEntity operation : operations) {
            GetOperationDetailResponse response = new GetOperationDetailResponse();
            response.setOperationId(operation.getOperationId());
            response.setOperationName(operation.getOperationName());
            response.setUserId(operation.getUserId());
            response.setOrganizationId(operation.getOrganizationId());
            response.setOperationData(operation.getOperationData());
            if (operation.getResult() != null) {
                response.setResult(operation.getResult());
            }
            assignFormData(response, operation);
            assignApplicationContext(response, operation);
            assignOperationHistory(response, operation);
            response.setTimestampCreated(operation.getTimestampCreated());
            response.setTimestampExpires(operation.getTimestampExpires());
            responseList.add(response);
        }

        logger.debug("The getPendingOperations request succeeded, operation list size: {}", responseList.size());
        return new ObjectResponse<>(responseList);
    }

    /**
     * Update operation with updated form data (PUT method).
     *
     * @param request Update operation request.
     * @return Update operation response.
     * @throws OperationNotFoundException Thrown when operation is not found.
     */
    @RequestMapping(value = "/operation/formData", method = RequestMethod.PUT)
    public @ResponseBody Response updateOperationFormData(@RequestBody ObjectRequest<UpdateFormDataRequest> request) throws OperationNotFoundException {
        return updateOperationFormDataImpl(request);
    }

    /**
     * Update operation with updated form data (POST method alternative).
     *
     * @param request Update operation request.
     * @return Update operation response.
     * @throws OperationNotFoundException Thrown when operation is not found.
     */
    @RequestMapping(value = "/operation/formData/update", method = RequestMethod.POST)
    public @ResponseBody Response updateOperationFormDataPost(@RequestBody ObjectRequest<UpdateFormDataRequest> request) throws OperationNotFoundException {
        return updateOperationFormDataImpl(request);
    }

    private Response updateOperationFormDataImpl(ObjectRequest<UpdateFormDataRequest> request) throws OperationNotFoundException {
        logger.info("Received updateOperationFormData request, operation ID: {}", request.getRequestObject().getOperationId());
        // persist operation form data update
        operationPersistenceService.updateFormData(request.getRequestObject());
        logger.debug("The updateOperationFormData request succeeded");
        return new Response();
    }

    /**
     * Update operation with chosen authentication method (PUT method).
     * @param request Update operation request.
     * @return Update operation response.
     * @throws OperationNotFoundException Thrown when operation is not found.
     */
    @RequestMapping(value = "/operation/chosenAuthMethod", method = RequestMethod.PUT)
    public @ResponseBody Response updateChosenAuthMethod(@RequestBody ObjectRequest<UpdateChosenAuthMethodRequest> request) throws OperationNotFoundException {
        return updateChosenAuthMethodImpl(request);
    }

    /**
     * Update operation with chosen authentication method (POST method alternative).
     * @param request Update operation request.
     * @return Update operation response.
     * @throws OperationNotFoundException Thrown when operation is not found.
     */
    @RequestMapping(value = "/operation/chosenAuthMethod/update", method = RequestMethod.POST)
    public @ResponseBody Response updateChosenAuthMethodPost(@RequestBody ObjectRequest<UpdateChosenAuthMethodRequest> request) throws OperationNotFoundException {
        return updateChosenAuthMethodImpl(request);
    }

    private Response updateChosenAuthMethodImpl(ObjectRequest<UpdateChosenAuthMethodRequest> request) throws OperationNotFoundException {
        logger.info("Received updateChosenAuthMethod request, operation ID: {}, chosen authentication method: {}", request.getRequestObject().getOperationId(), request.getRequestObject().getChosenAuthMethod().toString());
        // persist operation form data update
        operationPersistenceService.updateChosenAuthMethod(request.getRequestObject());
        logger.debug("The updateChosenAuthMethod request succeeded");
        return new Response();
    }

    /**
     * Update application context for an operation (PUT method).
     * @param request Update application context request.
     * @return Response.
     * @throws OperationNotFoundException Thrown when operation is not found.
     */
    @RequestMapping(value = "/operation/application", method = RequestMethod.PUT)
    public @ResponseBody Response updateApplicationContext(@RequestBody ObjectRequest<UpdateApplicationContextRequest> request) throws OperationNotFoundException {
        return updateApplicationContextImpl(request);
    }

    /**
     * Update application context for an operation (POST method alternative).
     * @param request Update application context request.
     * @return Response.
     * @throws OperationNotFoundException Thrown when operation is not found.
     */
    @RequestMapping(value = "/operation/application/update", method = RequestMethod.POST)
    public @ResponseBody Response updateApplicationContextPost(@RequestBody ObjectRequest<UpdateApplicationContextRequest> request) throws OperationNotFoundException {
        return updateApplicationContextImpl(request);
    }

    @RequestMapping(value = "/operation/afs", method = RequestMethod.POST)
    public @ResponseBody Response createAfsAction(@RequestBody ObjectRequest<CreateAfsActionRequest> request) throws OperationNotFoundException {
        CreateAfsActionRequest afsRequest = request.getRequestObject();
        logger.info("Received createAfsAction request, operation ID: {}, AFS action: {}", afsRequest.getOperationId(), afsRequest.getAfsAction());
        // persist AFS action for operation
        operationPersistenceService.createAfsAction(afsRequest);
        logger.debug("The createAfsAction request succeeded");
        return new Response();

    }

    private Response updateApplicationContextImpl(ObjectRequest<UpdateApplicationContextRequest> request) throws OperationNotFoundException {
        logger.info("Received updateApplicationContext request, operation ID: {}", request.getRequestObject().getOperationId());
        // persist application context update
        operationPersistenceService.updateApplicationContext(request.getRequestObject());
        logger.debug("The updateApplicationContext request succeeded");
        return new Response();
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
                logger.error("Error while deserializing operation display formData", ex);
            }
            response.setFormData(formData);
        }
    }

    /**
     * In case operation entity has an application context, assign it to the operation.
     * The application extras are deserialized from JSON.
     * @param response Response to be enriched by application context.
     * @param operation Database entity representing operation.
     */
    private void assignApplicationContext(GetOperationDetailResponse response, OperationEntity operation) {
        if (operation.getApplicationId() != null) {
            ApplicationContext applicationContext = new ApplicationContext();
            applicationContext.setId(operation.getApplicationId());
            applicationContext.setName(operation.getApplicationName());
            applicationContext.setDescription(operation.getApplicationDescription());
            if (operation.getApplicationExtras() != null) {
                Map<String, Object> extras;
                try {
                    JavaType mapType = objectMapper.getTypeFactory().constructParametricType(Map.class, String.class, Object.class);
                    extras = objectMapper.readValue(operation.getApplicationExtras(), mapType);
                    applicationContext.getExtras().putAll(extras);
                } catch (IOException ex) {
                    logger.error("Error while deserializing application extras", ex);
                }
            }
            response.setApplicationContext(applicationContext);
        }
    }

    /**
     * Assign operation history to operation.
     * @param response Response to be enriched by operation history.
     * @param operation Database entity representing operation.
     */
    private void assignOperationHistory(GetOperationDetailResponse response, OperationEntity operation) {
        // add operation history
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
    }


    /**
     * Assign AFS actions to operation.
     * @param response Response to be enriched by AFS actions.
     * @param operation Database entity representing operation.
     */
    private void assignAfsActions(GetOperationDetailResponse response, OperationEntity operation) {
        // add AFS actions
        for (OperationAfsActionEntity afsAction: operation.getAfsActions()) {
            AfsActionDetail action = new AfsActionDetail();
            action.setName(afsAction.getAfsAction());
            action.setStepIndex(afsAction.getStepIndex());
            action.setRequestExtras(convertExtrasToMap(afsAction.getRequestAfsExtras()));
            action.setAfsResponseApplied(afsAction.isAfsResponseApplied());
            action.setAfsLabel(afsAction.getAfsLabel());
            action.setResponseExtras(convertExtrasToMap(afsAction.getResponseAfsExtras()));
            response.getAfsActions().add(action);
        }
    }

    /**
     * Convert extras String to map.
     * @param extras String with extras.
     * @return Extras map.
     */
    private Map<String, Object> convertExtrasToMap(String extras) {
        try {
            TypeReference<Map<String, Object>> typeRef
                    = new TypeReference<Map<String, Object>>() {};
            return objectMapper.readValue(extras, typeRef);
        } catch (IOException e) {
            logger.error("Error occurred while deserializing data", e);
            return null;
        }
    }
}
