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
import io.getlime.core.rest.model.base.response.Response;
import io.getlime.security.powerauth.app.nextstep.converter.OperationConverter;
import io.getlime.security.powerauth.app.nextstep.repository.model.entity.OperationEntity;
import io.getlime.security.powerauth.app.nextstep.service.MobileTokenConfigurationService;
import io.getlime.security.powerauth.app.nextstep.service.OperationConfigurationService;
import io.getlime.security.powerauth.app.nextstep.service.OperationPersistenceService;
import io.getlime.security.powerauth.app.nextstep.service.StepResolutionService;
import io.getlime.security.powerauth.lib.nextstep.model.entity.AuthStep;
import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.UserAccountStatus;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthMethod;
import io.getlime.security.powerauth.lib.nextstep.model.exception.NextStepServiceException;
import io.getlime.security.powerauth.lib.nextstep.model.exception.OperationAlreadyExistsException;
import io.getlime.security.powerauth.lib.nextstep.model.exception.OperationNotConfiguredException;
import io.getlime.security.powerauth.lib.nextstep.model.exception.OperationNotFoundException;
import io.getlime.security.powerauth.lib.nextstep.model.request.*;
import io.getlime.security.powerauth.lib.nextstep.model.response.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * REST controller class related to Next Step operations.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
@RestController
public class OperationController {

    private static final Logger logger = LoggerFactory.getLogger(OperationController.class);

    private final OperationPersistenceService operationPersistenceService;
    private final OperationConfigurationService operationConfigurationService;
    private final StepResolutionService stepResolutionService;
    private final MobileTokenConfigurationService mobileTokenConfigurationService;
    private final OperationConverter operationConverter = new OperationConverter();

    /**
     * Controller constructor.
     * @param operationPersistenceService Operation persistence service.
     * @param operationConfigurationService Operation configuration service.
     * @param stepResolutionService Step resolution service.
     * @param mobileTokenConfigurationService Mobile token configuration service.
     */
    @Autowired
    public OperationController(OperationPersistenceService operationPersistenceService, OperationConfigurationService operationConfigurationService,
                               StepResolutionService stepResolutionService, MobileTokenConfigurationService mobileTokenConfigurationService) {
        this.operationPersistenceService = operationPersistenceService;
        this.operationConfigurationService = operationConfigurationService;
        this.stepResolutionService = stepResolutionService;
        this.mobileTokenConfigurationService = mobileTokenConfigurationService;
    }

    /**
     * Create a new operation with given name and data.
     *
     * @param request Create operation request.
     * @return Create operation response.
     * @throws OperationAlreadyExistsException Thrown when operation already exists.
     */
    @RequestMapping(value = "operation", method = RequestMethod.POST)
    public ObjectResponse<CreateOperationResponse> createOperation(@RequestBody ObjectRequest<CreateOperationRequest> request) throws OperationAlreadyExistsException {
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
    @RequestMapping(value = "operation", method = RequestMethod.PUT)
    public ObjectResponse<UpdateOperationResponse> updateOperation(@RequestBody ObjectRequest<UpdateOperationRequest> request) throws NextStepServiceException {
        return updateOperationImpl(request);
    }

    /**
     * Update operation with given ID with a previous authentication step result (POST method alternative).
     *
     * @param request Update operation request.
     * @return Update operation response.
     * @throws NextStepServiceException Thrown when next step resolution fails.
     */
    @RequestMapping(value = "operation/update", method = RequestMethod.POST)
    public ObjectResponse<UpdateOperationResponse> updateOperationPost(@RequestBody ObjectRequest<UpdateOperationRequest> request) throws NextStepServiceException {
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
    @RequestMapping(value = "operation/user", method = RequestMethod.PUT)
    public Response updateOperationUser(@RequestBody ObjectRequest<UpdateOperationUserRequest> request) throws NextStepServiceException {
        return updateOperationUserImpl(request);
    }

    /**
     * Assign user ID and organization ID to and operation (POST alternative).
     *
     * @param request Update operation user request.
     * @return Response.
     * @throws NextStepServiceException Thrown when update fails in Next Step.
     */
    @RequestMapping(value = "operation/user/update", method = RequestMethod.POST)
    public Response updateOperationUserPost(@RequestBody ObjectRequest<UpdateOperationUserRequest> request) throws NextStepServiceException {
        return updateOperationUserImpl(request);
    }

    private Response updateOperationUserImpl(ObjectRequest<UpdateOperationUserRequest> request) throws NextStepServiceException {
        String operationId = request.getRequestObject().getOperationId();
        String userId = request.getRequestObject().getUserId();
        String organizationId = request.getRequestObject().getOrganizationId();
        UserAccountStatus accountStatus = request.getRequestObject().getAccountStatus();
        logger.info("Received updateOperationUser request, operation ID: {}, user ID: {}, organization ID: {}, account status: {}", operationId, userId, organizationId, accountStatus);

        // persist operation user update
        operationPersistenceService.updateOperationUser(request.getRequestObject());

        logger.info("The updateOperationUser request succeeded, operation ID: {}, user ID: {}, organization ID: {}, account status: {}", operationId, userId, organizationId, accountStatus);
        return new Response();
    }

    /**
     * Get detail of an operation with given ID.
     *
     * @param request Get operation detail request.
     * @return Get operation detail response.
     * @throws OperationNotFoundException Thrown when operation does not exist.
     */
    @RequestMapping(value = "operation/detail", method = RequestMethod.POST)
    public ObjectResponse<GetOperationDetailResponse> operationDetail(@RequestBody ObjectRequest<GetOperationDetailRequest> request) throws OperationNotFoundException {
        // Log level is FINE to avoid flooding logs, this endpoint is used all the time.
        logger.debug("Received operationDetail request, operation ID: {}", request.getRequestObject().getOperationId());

        GetOperationDetailRequest requestObject = request.getRequestObject();

        OperationEntity operation = operationPersistenceService.getOperation(requestObject.getOperationId());
        GetOperationDetailResponse response = operationConverter.fromEntity(operation);

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
    @RequestMapping(value = "operation/config/detail", method = RequestMethod.POST)
    public ObjectResponse<GetOperationConfigDetailResponse> getOperationConfigDetail(@RequestBody ObjectRequest<GetOperationConfigDetailRequest> request) throws OperationNotConfiguredException {
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
    @RequestMapping(value = "operation/config/list", method = RequestMethod.POST)
    public ObjectResponse<GetOperationConfigListResponse> getOperationConfigList(@RequestBody ObjectRequest<GetOperationConfigListRequest> request) {
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
    @RequestMapping(value = "user/operation/list", method = RequestMethod.POST)
    public ObjectResponse<List<GetOperationDetailResponse>> getPendingOperations(@RequestBody ObjectRequest<GetPendingOperationsRequest> request) {
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
            GetOperationDetailResponse response = operationConverter.fromEntity(operation);
            responseList.add(response);
        }

        logger.debug("The getPendingOperations request succeeded, operation list size: {}", responseList.size());
        return new ObjectResponse<>(responseList);
    }

    /**
     * Lookup operations for given external transaction ID.
     *
     * @param request Lookup operations by external transaction ID request.
     * @return Response for operations lookup by external transaction ID.
     */
    @RequestMapping(value = "operation/lookup/external", method = RequestMethod.POST)
    public ObjectResponse<LookupOperationsByExternalIdResponse> lookupOperationsByExternalId(@RequestBody ObjectRequest<LookupOperationsByExternalIdRequest> request) {
        // Log level is FINE to avoid flooding logs, this endpoint is used all the time.
        logger.debug("Received lookupOperationsByExternalId request, external transaction ID: {}", request.getRequestObject().getExternalTransactionId());

        LookupOperationsByExternalIdRequest requestObject = request.getRequestObject();

        LookupOperationsByExternalIdResponse response = new LookupOperationsByExternalIdResponse();
        List<OperationEntity> operations = operationPersistenceService.findByExternalTransactionId(requestObject.getExternalTransactionId());
        for (OperationEntity operation : operations) {
            GetOperationDetailResponse operationDetail = operationConverter.fromEntity(operation);
            response.getOperations().add(operationDetail);
        }

        logger.debug("The lookupOperationsByExternalId request succeeded, operation list size: {}", response.getOperations().size());
        return new ObjectResponse<>(response);
    }

    /**
     * Update operation with updated form data (PUT method).
     *
     * @param request Update operation request.
     * @return Update operation response.
     * @throws OperationNotFoundException Thrown when operation is not found.
     */
    @RequestMapping(value = "operation/formData", method = RequestMethod.PUT)
    public Response updateOperationFormData(@RequestBody ObjectRequest<UpdateFormDataRequest> request) throws OperationNotFoundException {
        return updateOperationFormDataImpl(request);
    }

    /**
     * Update operation with updated form data (POST method alternative).
     *
     * @param request Update operation request.
     * @return Update operation response.
     * @throws OperationNotFoundException Thrown when operation is not found.
     */
    @RequestMapping(value = "operation/formData/update", method = RequestMethod.POST)
    public Response updateOperationFormDataPost(@RequestBody ObjectRequest<UpdateFormDataRequest> request) throws OperationNotFoundException {
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
    @RequestMapping(value = "operation/chosenAuthMethod", method = RequestMethod.PUT)
    public Response updateChosenAuthMethod(@RequestBody ObjectRequest<UpdateChosenAuthMethodRequest> request) throws OperationNotFoundException {
        return updateChosenAuthMethodImpl(request);
    }

    /**
     * Update operation with chosen authentication method (POST method alternative).
     * @param request Update operation request.
     * @return Update operation response.
     * @throws OperationNotFoundException Thrown when operation is not found.
     */
    @RequestMapping(value = "operation/chosenAuthMethod/update", method = RequestMethod.POST)
    public Response updateChosenAuthMethodPost(@RequestBody ObjectRequest<UpdateChosenAuthMethodRequest> request) throws OperationNotFoundException {
        return updateChosenAuthMethodImpl(request);
    }

    private Response updateChosenAuthMethodImpl(ObjectRequest<UpdateChosenAuthMethodRequest> request) throws OperationNotFoundException {
        logger.info("Received updateChosenAuthMethod request, operation ID: {}, chosen authentication method: {}", request.getRequestObject().getOperationId(), request.getRequestObject().getChosenAuthMethod().toString());
        // persist chosen auth method update
        operationPersistenceService.updateChosenAuthMethod(request.getRequestObject());
        logger.debug("The updateChosenAuthMethod request succeeded");
        return new Response();
    }

    /**
     * Update mobile token status for an operation (PUT method).
     * @param request Update operation request.
     * @return Update operation response.
     * @throws OperationNotFoundException Thrown when operation is not found.
     */
    @RequestMapping(value = "operation/mobileToken/status", method = RequestMethod.PUT)
    public Response updateMobileToken(@RequestBody ObjectRequest<UpdateMobileTokenRequest> request) throws OperationNotFoundException {
        return updateMobileTokenImpl(request);
    }

    /**
     * Update operation with chosen authentication method (POST method alternative).
     * @param request Update operation request.
     * @return Update operation response.
     * @throws OperationNotFoundException Thrown when operation is not found.
     */
    @RequestMapping(value = "operation/mobileToken/status/update", method = RequestMethod.POST)
    public @ResponseBody Response updateMobileTokenPost(@RequestBody ObjectRequest<UpdateMobileTokenRequest> request) throws OperationNotFoundException {
        return updateMobileTokenImpl(request);
    }

    private Response updateMobileTokenImpl(ObjectRequest<UpdateMobileTokenRequest> request) throws OperationNotFoundException {
        logger.info("Received updateMobileToken request, operation ID: {}, mobile token active: {}", request.getRequestObject().getOperationId(), request.getRequestObject().isMobileTokenActive());
        // persist mobile token update
        operationPersistenceService.updateMobileToken(request.getRequestObject());
        logger.debug("The updateMobileToken request succeeded");
        return new Response();
    }

    /**
     * Get mobile token configuration.
     * @param request Get mobile token configuration request.
     * @return Get mobile token configuration response.
     * @throws OperationNotFoundException Thrown when operation is not found.
     */
    @RequestMapping(value = "operation/mobileToken/config/detail", method = RequestMethod.POST)
    public ObjectResponse<GetMobileTokenConfigResponse> getMobileTokenConfig(@RequestBody ObjectRequest<GetMobileTokenConfigRequest> request) throws OperationNotFoundException {
        String userId = request.getRequestObject().getUserId();
        String operationName = request.getRequestObject().getOperationName();
        AuthMethod authMethod = request.getRequestObject().getAuthMethod();
        logger.info("Received getMobileTokenConfig request, user ID: {}, operation name: {}, authentication method: {}", userId, operationName, authMethod);
        boolean isMobileTokenEnabled = mobileTokenConfigurationService.isMobileTokenEnabled(userId, operationName, authMethod);
        GetMobileTokenConfigResponse response = new GetMobileTokenConfigResponse();
        response.setMobileTokenEnabled(isMobileTokenEnabled);
        logger.debug("The getMobileTokenConfig request succeeded");
        return new ObjectResponse<>(response);
    }

    /**
     * Update application context for an operation (PUT method).
     * @param request Update application context request.
     * @return Response.
     * @throws OperationNotFoundException Thrown when operation is not found.
     */
    @RequestMapping(value = "operation/application", method = RequestMethod.PUT)
    public Response updateApplicationContext(@RequestBody ObjectRequest<UpdateApplicationContextRequest> request) throws OperationNotFoundException {
        return updateApplicationContextImpl(request);
    }

    /**
     * Update application context for an operation (POST method alternative).
     * @param request Update application context request.
     * @return Response.
     * @throws OperationNotFoundException Thrown when operation is not found.
     */
    @RequestMapping(value = "operation/application/update", method = RequestMethod.POST)
    public Response updateApplicationContextPost(@RequestBody ObjectRequest<UpdateApplicationContextRequest> request) throws OperationNotFoundException {
        return updateApplicationContextImpl(request);
    }

    @RequestMapping(value = "operation/afs/action/create", method = RequestMethod.POST)
    public Response createAfsAction(@RequestBody ObjectRequest<CreateAfsActionRequest> request) throws OperationNotFoundException {
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

    @RequestMapping(value = "operation/config", method = RequestMethod.POST)
    public ObjectResponse<CreateOperationConfigResponse> createOperationConfig(@RequestBody ObjectRequest<CreateOperationConfigRequest> request) {
        return new ObjectResponse<>(new CreateOperationConfigResponse());
    }

    @RequestMapping(value = "operation/config/delete", method = RequestMethod.POST)
    public ObjectResponse<DeleteOperationConfigResponse> deleteOperationConfig(@RequestBody ObjectRequest<DeleteOperationConfigRequest> request) {
        return new ObjectResponse<>(new DeleteOperationConfigResponse());
    }

}