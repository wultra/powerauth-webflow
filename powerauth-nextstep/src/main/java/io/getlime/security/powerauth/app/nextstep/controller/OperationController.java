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

import com.wultra.core.rest.model.base.request.ObjectRequest;
import com.wultra.core.rest.model.base.response.ObjectResponse;
import com.wultra.core.rest.model.base.response.Response;
import io.getlime.security.powerauth.app.nextstep.converter.OperationConverter;
import io.getlime.security.powerauth.app.nextstep.repository.model.entity.OperationEntity;
import io.getlime.security.powerauth.app.nextstep.service.MobileTokenConfigurationService;
import io.getlime.security.powerauth.app.nextstep.service.OperationConfigurationService;
import io.getlime.security.powerauth.app.nextstep.service.OperationPersistenceService;
import io.getlime.security.powerauth.app.nextstep.service.StepResolutionService;
import io.getlime.security.powerauth.lib.nextstep.model.entity.AuthStep;
import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.UserAccountStatus;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthMethod;
import io.getlime.security.powerauth.lib.nextstep.model.exception.*;
import io.getlime.security.powerauth.lib.nextstep.model.request.*;
import io.getlime.security.powerauth.lib.nextstep.model.response.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * REST controller class related to Next Step operations.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
@RestController
@Validated
public class OperationController {

    private static final Logger logger = LoggerFactory.getLogger(OperationController.class);

    private final OperationPersistenceService operationPersistenceService;
    private final OperationConfigurationService operationConfigurationService;
    private final StepResolutionService stepResolutionService;
    private final MobileTokenConfigurationService mobileTokenConfigurationService;
    private final OperationConverter operationConverter;

    /**
     * REST controller constructor.
     * @param operationPersistenceService Operation persistence service.
     * @param operationConfigurationService Operation configuration service.
     * @param stepResolutionService Step resolution service.
     * @param mobileTokenConfigurationService Mobile token configuration service.
     * @param operationConverter Operation converter.
     */
    @Autowired
    public OperationController(OperationPersistenceService operationPersistenceService, OperationConfigurationService operationConfigurationService,
                               StepResolutionService stepResolutionService, MobileTokenConfigurationService mobileTokenConfigurationService, OperationConverter operationConverter) {
        this.operationPersistenceService = operationPersistenceService;
        this.operationConfigurationService = operationConfigurationService;
        this.stepResolutionService = stepResolutionService;
        this.mobileTokenConfigurationService = mobileTokenConfigurationService;
        this.operationConverter = operationConverter;
    }

    /**
     * Create a new operation with given name and data.
     *
     * @param request Create operation request.
     * @return Create operation response.
     * @throws OperationAlreadyExistsException Thrown when operation already exists.
     * @throws InvalidConfigurationException Thrown when Next Step configuration is invalid.
     * @throws OrganizationNotFoundException Thrown when organization is not found.
     */
    @Operation(summary = "Create an operation")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Operation was created"),
            @ApiResponse(responseCode = "400", description = "Invalid request, error codes: REQUEST_VALIDATION_FAILED, OPERATION_ALREADY_EXISTS, INVALID_CONFIGURATION, ORGANIZATION_NOT_FOUND"),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
    })
    @PostMapping("operation")
    public ObjectResponse<CreateOperationResponse> createOperation(@Valid @RequestBody ObjectRequest<CreateOperationRequest> request) throws OperationAlreadyExistsException, InvalidConfigurationException, OrganizationNotFoundException {
        logger.info("Received createOperation request, operation ID: {}, operation name: {}", request.getRequestObject().getOperationId(), request.getRequestObject().getOperationName());
        // resolve response based on dynamic step definitions
        final CreateOperationResponse response = stepResolutionService.resolveNextStepResponse(request.getRequestObject());

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
     * @throws InvalidRequestException Thrown when request is invalid.
     * @throws AuthMethodNotFoundException Thrown when authentication method is not found.
     * @throws OperationAlreadyFinishedException Thrown when operation is already finished.
     * @throws OperationAlreadyFailedException Thrown when operation is already failed.
     * @throws OperationAlreadyCanceledException Thrown when operation is already canceled.
     * @throws OperationNotValidException Thrown when operation is not valid.
     * @throws OperationNotFoundException Thrown when operation is not found.
     * @throws InvalidConfigurationException Thrown when Next Step configuration is invalid.
     * @throws OrganizationNotFoundException Thrown when organization is not found.
     */
    @Operation(summary = "Update an operation")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Operation was updated"),
            @ApiResponse(responseCode = "400", description = "Invalid request, error codes: REQUEST_VALIDATION_FAILED, INVALID_REQUEST, AUTH_METHOD_NOT_FOUND, OPERATION_ALREADY_FINISHED, OPERATION_ALREADY_FAILED, OPERATION_ALREADY_CANCELED, OPERATION_NOT_VALID, OPERATION_NOT_FOUND, INVALID_CONFIGURATION, ORGANIZATION_NOT_FOUND"),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
    })
    @PutMapping("operation")
    public ObjectResponse<UpdateOperationResponse> updateOperation(@Valid @RequestBody ObjectRequest<UpdateOperationRequest> request) throws InvalidRequestException, AuthMethodNotFoundException, OperationAlreadyFinishedException, OperationAlreadyFailedException, OperationAlreadyCanceledException, OperationNotValidException, OperationNotFoundException, InvalidConfigurationException, OrganizationNotFoundException {
        return updateOperationImpl(request);
    }

    /**
     * Update operation with given ID with a previous authentication step result (POST method alternative).
     *
     * @param request Update operation request.
     * @return Update operation response.
     * @throws InvalidRequestException Thrown when request is invalid.
     * @throws AuthMethodNotFoundException Thrown when authentication method is not found.
     * @throws OperationAlreadyFinishedException Thrown when operation is already finished.
     * @throws OperationAlreadyFailedException Thrown when operation is already failed.
     * @throws OperationAlreadyCanceledException Thrown when operation is already canceled.
     * @throws OperationNotValidException Thrown when operation is not valid.
     * @throws OperationNotFoundException Thrown when operation is not found.
     * @throws InvalidConfigurationException Thrown when Next Step configuration is invalid.
     * @throws OrganizationNotFoundException Thrown when organization is not found.
     */
    @Operation(summary = "Update an operation")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Operation was updated"),
            @ApiResponse(responseCode = "400", description = "Invalid request, error codes: REQUEST_VALIDATION_FAILED, INVALID_REQUEST, AUTH_METHOD_NOT_FOUND, OPERATION_ALREADY_FINISHED, OPERATION_ALREADY_FAILED, OPERATION_ALREADY_CANCELED, OPERATION_NOT_VALID, OPERATION_NOT_FOUND, INVALID_CONFIGURATION, ORGANIZATION_NOT_FOUND"),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
    })
    @PostMapping("operation/update")
    public ObjectResponse<UpdateOperationResponse> updateOperationPost(@Valid @RequestBody ObjectRequest<UpdateOperationRequest> request) throws InvalidRequestException, AuthMethodNotFoundException, OperationAlreadyFinishedException, OperationAlreadyFailedException, OperationNotValidException, OperationNotFoundException, InvalidConfigurationException, OperationAlreadyCanceledException, OrganizationNotFoundException {
        return updateOperationImpl(request);
    }

    private ObjectResponse<UpdateOperationResponse> updateOperationImpl(ObjectRequest<UpdateOperationRequest> request) throws OperationAlreadyFinishedException, AuthMethodNotFoundException, OperationAlreadyFailedException, InvalidConfigurationException, OperationNotValidException, OperationNotFoundException, InvalidRequestException, OperationAlreadyCanceledException, OrganizationNotFoundException {
        logger.info("Received updateOperation request, operation ID: {}", request.getRequestObject().getOperationId());

        final UpdateOperationResponse response = operationPersistenceService.updateOperation(request.getRequestObject());

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
     * @throws OperationNotFoundException Thrown when operation is not found.
     * @throws OrganizationNotFoundException Thrown when organization is not found.
     */
    @Operation(summary = "Update user for an operation")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Operation user was updated"),
            @ApiResponse(responseCode = "400", description = "Invalid request, error codes: REQUEST_VALIDATION_FAILED, OPERATION_NOT_FOUND, ORGANIZATION_NOT_FOUND"),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
    })
    @PutMapping("operation/user")
    public Response updateOperationUser(@Valid @RequestBody ObjectRequest<UpdateOperationUserRequest> request) throws OperationNotFoundException, OrganizationNotFoundException {
        return updateOperationUserImpl(request);
    }

    /**
     * Assign user ID and organization ID to and operation (POST alternative).
     *
     * @param request Update operation user request.
     * @return Response.
     * @throws OperationNotFoundException Thrown when operation is not found.
     * @throws OrganizationNotFoundException Thrown when organization is not found.
     */
    @Operation(summary = "Update user for an operation")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Operation user was updated"),
            @ApiResponse(responseCode = "400", description = "Invalid request, error codes: REQUEST_VALIDATION_FAILED, OPERATION_NOT_FOUND, ORGANIZATION_NOT_FOUND"),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
    })
    @PostMapping("operation/user/update")
    public Response updateOperationUserPost(@Valid @RequestBody ObjectRequest<UpdateOperationUserRequest> request) throws OperationNotFoundException, OrganizationNotFoundException {
        return updateOperationUserImpl(request);
    }

    private Response updateOperationUserImpl(ObjectRequest<UpdateOperationUserRequest> request) throws OperationNotFoundException, OrganizationNotFoundException {
        final String operationId = request.getRequestObject().getOperationId();
        final String userId = request.getRequestObject().getUserId();
        final String organizationId = request.getRequestObject().getOrganizationId();
        final UserAccountStatus accountStatus = request.getRequestObject().getAccountStatus();
        logger.info("Received updateOperationUser request, operation ID: {}, user ID: {}, organization ID: {}, account status: {}", operationId, userId, organizationId, accountStatus);

        // persist operation user update
        operationPersistenceService.updateOperationUser(request.getRequestObject());

        logger.info("The updateOperationUser request succeeded, operation ID: {}, user ID: {}, organization ID: {}, account status: {}", operationId, userId, organizationId, accountStatus);
        return new Response();
    }

    /**
     * Get detail of an operation with given ID.
     *
     * @param operationId Operation ID.
     * @return Get operation detail response.
     * @throws OperationNotFoundException Thrown when operation does not exist.
     * @throws OperationNotValidException Thrown when operation is invalid.
     */
    @Operation(summary = "Get operation detail")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Operation detail sent in response"),
            @ApiResponse(responseCode = "400", description = "Invalid request, error codes: REQUEST_VALIDATION_FAILED, OPERATION_NOT_FOUND, OPERATION_NOT_VALID"),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
    })
    @GetMapping("operation/detail")
    public ObjectResponse<GetOperationDetailResponse> operationDetail(@RequestParam @NotBlank @Size(min = 1, max = 256) String operationId) throws OperationNotFoundException, OperationNotValidException {
        // Log level is FINE to avoid flooding logs, this endpoint is used all the time.
        logger.debug("Received operationDetail request, operation ID: {}", operationId);

        final OperationEntity operation = operationPersistenceService.getOperation(operationId, true);
        final GetOperationDetailResponse response = operationConverter.fromEntity(operation);

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
     * Get detail of an operation with given ID using POST method.
     *
     * @param request Get operation detail request.
     * @return Get operation detail response.
     * @throws OperationNotFoundException Thrown when operation does not exist.
     * @throws OperationNotValidException Thrown when operation is invalid.
     */
    @Operation(summary = "Get operation detail")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Operation detail sent in response"),
            @ApiResponse(responseCode = "400", description = "Invalid request, error codes: REQUEST_VALIDATION_FAILED, OPERATION_NOT_FOUND, OPERATION_NOT_VALID"),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
    })
    @PostMapping("operation/detail")
    public ObjectResponse<GetOperationDetailResponse> operationDetailPost(@Valid @RequestBody ObjectRequest<GetOperationDetailRequest> request) throws OperationNotFoundException, OperationNotValidException {
        // Log level is FINE to avoid flooding logs, this endpoint is used all the time.
        logger.debug("Received operationDetail request, operation ID: {}", request.getRequestObject().getOperationId());

        final GetOperationDetailRequest requestObject = request.getRequestObject();

        final OperationEntity operation = operationPersistenceService.getOperation(requestObject.getOperationId(), true);
        final GetOperationDetailResponse response = operationConverter.fromEntity(operation);

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
     * @param operationName Operation name.
     * @return Get operation configuration response.
     * @throws OperationConfigNotFoundException Thrown when operation is not configured.
     */
    @Operation(summary = "Get operation configuration detail")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Operation configuration detail sent in response"),
            @ApiResponse(responseCode = "400", description = "Invalid request, error codes: REQUEST_VALIDATION_FAILED, OPERATION_CONFIG_NOT_FOUND"),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
    })
    @GetMapping("operation/config/detail")
    public ObjectResponse<GetOperationConfigDetailResponse> getOperationConfigDetail(@RequestParam @NotBlank @Size(min = 2, max = 256) String operationName) throws OperationConfigNotFoundException {
        // Log level is FINE to avoid flooding logs, this endpoint is used all the time.
        logger.debug("Received getOperationConfigDetail request, operation name: {}", operationName);

        final GetOperationConfigDetailResponse response = operationConfigurationService.getOperationConfig(operationName);

        logger.debug("The getOperationConfigDetail request succeeded, operation name: {}", operationName);
        return new ObjectResponse<>(response);
    }

    /**
     * Get configuration of an operation with given operation name using POST method.
     *
     * @param request Get operation configuration request.
     * @return Get operation configuration response.
     * @throws OperationConfigNotFoundException Thrown when operation is not configured.
     */
    @Operation(summary = "Get operation configuration detail")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Operation configuration detail sent in response"),
            @ApiResponse(responseCode = "400", description = "Invalid request, error codes: REQUEST_VALIDATION_FAILED, OPERATION_CONFIG_NOT_FOUND"),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
    })
    @PostMapping("operation/config/detail")
    public ObjectResponse<GetOperationConfigDetailResponse> getOperationConfigDetailPost(@Valid @RequestBody ObjectRequest<GetOperationConfigDetailRequest> request) throws OperationConfigNotFoundException {
        // Log level is FINE to avoid flooding logs, this endpoint is used all the time.
        logger.debug("Received getOperationConfigDetail request, operation name: {}", request.getRequestObject().getOperationName());

        final GetOperationConfigDetailRequest requestObject = request.getRequestObject();

        final GetOperationConfigDetailResponse response = operationConfigurationService.getOperationConfig(requestObject.getOperationName());

        logger.debug("The getOperationConfigDetail request succeeded, operation name: {}", request.getRequestObject().getOperationName());
        return new ObjectResponse<>(response);
    }

    /**
     * Get configurations of all operations.
     *
     * @return Get operation configurations response.
     */
    @Operation(summary = "Get operation configuration list")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Operation configuration list sent in response"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
    })
    @GetMapping("operation/config")
    public ObjectResponse<GetOperationConfigListResponse> getOperationConfigList() {
        // Log level is FINE to avoid flooding logs, this endpoint is used all the time.
        logger.debug("Received getOperationConfigList request");

        final GetOperationConfigListResponse response = operationConfigurationService.getOperationConfigList();

        logger.debug("The getOperationConfigList request succeeded, operation config list size: {}", response.getOperationConfigs().size());
        return new ObjectResponse<>(response);
    }

    /**
     * Get configurations of all operations using POST method.
     *
     * @param request Get configurations of all operations request.
     * @return Get operation configurations response.
     */
    @Operation(summary = "Get operation configuration list")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Operation configuration list sent in response"),
            @ApiResponse(responseCode = "400", description = "Invalid request, error codes: REQUEST_VALIDATION_FAILED"),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
    })
    @PostMapping("operation/config/list")
    public ObjectResponse<GetOperationConfigListResponse> getOperationConfigListPost(@Valid @RequestBody ObjectRequest<GetOperationConfigListRequest> request) {
        // Log level is FINE to avoid flooding logs, this endpoint is used all the time.
        logger.debug("Received getOperationConfigListPost request");

        final GetOperationConfigListResponse response = operationConfigurationService.getOperationConfigList();

        logger.debug("The getOperationConfigListPost request succeeded, operation config list size: {}", response.getOperationConfigs().size());
        return new ObjectResponse<>(response);
    }

    /**
     * Get the list of pending operations for user.
     *
     * @param userId User ID.
     * @param mobileTokenOnly Whether only operations with mobile token should be returned
     * @return List with operation details.
     */
    @Operation(summary = "Get pending operation list")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pending operation list sent in response"),
            @ApiResponse(responseCode = "400", description = "Invalid request, error codes: REQUEST_VALIDATION_FAILED"),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
    })
    @GetMapping("user/operation")
    public ObjectResponse<List<GetOperationDetailResponse>> getPendingOperations(@RequestParam @NotBlank @Size(min = 1, max = 256) String userId, @RequestParam boolean mobileTokenOnly) {
        // Log level is FINE to avoid flooding logs, this endpoint is used all the time.
        logger.debug("Received getPendingOperations request, user ID: {}", userId);

        final List<GetOperationDetailResponse> responseList = new ArrayList<>();

        final List<OperationEntity> operations = operationPersistenceService.getPendingOperations(userId, mobileTokenOnly);
        for (OperationEntity operation : operations) {
            final GetOperationDetailResponse response = operationConverter.fromEntity(operation);
            responseList.add(response);
        }

        logger.debug("The getPendingOperations request succeeded, operation list size: {}", responseList.size());
        return new ObjectResponse<>(responseList);
    }

    /**
     * Get the list of pending operations for user using POST method.
     *
     * @param request Get pending operations request.
     * @return List with operation details.
     */
    @Operation(summary = "Get pending operation list")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pending operation list sent in response"),
            @ApiResponse(responseCode = "400", description = "Invalid request, error codes: REQUEST_VALIDATION_FAILED"),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
    })
    @PostMapping("user/operation/list")
    public ObjectResponse<List<GetOperationDetailResponse>> getPendingOperationsPost(@Valid @RequestBody ObjectRequest<GetPendingOperationsRequest> request) {
        // Log level is FINE to avoid flooding logs, this endpoint is used all the time.
        logger.debug("Received getPendingOperationsPost request, user ID: {}", request.getRequestObject().getUserId());

        final GetPendingOperationsRequest requestObject = request.getRequestObject();

        final List<GetOperationDetailResponse> responseList = new ArrayList<>();

        final List<OperationEntity> operations = operationPersistenceService.getPendingOperations(requestObject.getUserId(), requestObject.isMobileTokenOnly());
        for (OperationEntity operation : operations) {
            final GetOperationDetailResponse response = operationConverter.fromEntity(operation);
            responseList.add(response);
        }

        logger.debug("The getPendingOperationsPost request succeeded, operation list size: {}", responseList.size());
        return new ObjectResponse<>(responseList);
    }

    /**
     * Lookup operations for given external transaction ID.
     *
     * @param request Lookup operations by external transaction ID request.
     * @return Response for operations lookup by external transaction ID.
     */
    @Operation(summary = "Lookup operations by external ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Operation list sent in response"),
            @ApiResponse(responseCode = "400", description = "Invalid request, error codes: REQUEST_VALIDATION_FAILED"),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
    })
    @PostMapping("operation/lookup/external")
    public ObjectResponse<LookupOperationsByExternalIdResponse> lookupOperationsByExternalId(@Valid @RequestBody ObjectRequest<LookupOperationsByExternalIdRequest> request) {
        // Log level is FINE to avoid flooding logs, this endpoint is used all the time.
        logger.debug("Received lookupOperationsByExternalId request, external transaction ID: {}", request.getRequestObject().getExternalTransactionId());

        final LookupOperationsByExternalIdRequest requestObject = request.getRequestObject();

        final LookupOperationsByExternalIdResponse response = new LookupOperationsByExternalIdResponse();
        final List<OperationEntity> operations = operationPersistenceService.findByExternalTransactionId(requestObject.getExternalTransactionId());
        for (OperationEntity operation : operations) {
            final GetOperationDetailResponse operationDetail = operationConverter.fromEntity(operation);
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
    @Operation(summary = "Update operation form data")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Operation form data was updated"),
            @ApiResponse(responseCode = "400", description = "Invalid request, error codes: REQUEST_VALIDATION_FAILED, OPERATION_NOT_FOUND"),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
    })
    @PutMapping("operation/formData")
    public Response updateOperationFormData(@Valid @RequestBody ObjectRequest<UpdateFormDataRequest> request) throws OperationNotFoundException {
        return updateOperationFormDataImpl(request);
    }

    /**
     * Update operation with updated form data (POST method alternative).
     *
     * @param request Update operation request.
     * @return Update operation response.
     * @throws OperationNotFoundException Thrown when operation is not found.
     */
    @Operation(summary = "Update operation form data")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Operation form data was updated"),
            @ApiResponse(responseCode = "400", description = "Invalid request, error codes: REQUEST_VALIDATION_FAILED, OPERATION_NOT_FOUND"),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
    })
    @PostMapping("operation/formData/update")
    public Response updateOperationFormDataPost(@Valid @RequestBody ObjectRequest<UpdateFormDataRequest> request) throws OperationNotFoundException {
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
     * @throws InvalidRequestException Thrown when request is invalid.
     * @throws OperationNotValidException Thrown when operation is invalid.
     */
    @Operation(summary = "Update chosen authentication method for an operation")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Chosen authentication method was updated"),
            @ApiResponse(responseCode = "400", description = "Invalid request, error codes: REQUEST_VALIDATION_FAILED, OPERATION_NOT_FOUND, INVALID_REQUEST, OPERATION_NOT_VALID"),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
    })
    @PutMapping("operation/chosenAuthMethod")
    public Response updateChosenAuthMethod(@Valid @RequestBody ObjectRequest<UpdateChosenAuthMethodRequest> request) throws OperationNotFoundException, InvalidRequestException, OperationNotValidException {
        return updateChosenAuthMethodImpl(request);
    }

    /**
     * Update operation with chosen authentication method (POST method alternative).
     * @param request Update operation request.
     * @return Update operation response.
     * @throws OperationNotFoundException Thrown when operation is not found.
     * @throws InvalidRequestException Thrown when request is invalid.
     * @throws OperationNotValidException Thrown when operation is invalid.
     */
    @Operation(summary = "Update chosen authentication method for an operation")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Chosen authentication method was updated"),
            @ApiResponse(responseCode = "400", description = "Invalid request, error codes: REQUEST_VALIDATION_FAILED, OPERATION_NOT_FOUND, INVALID_REQUEST, OPERATION_NOT_VALID"),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
    })
    @PostMapping("operation/chosenAuthMethod/update")
    public Response updateChosenAuthMethodPost(@Valid @RequestBody ObjectRequest<UpdateChosenAuthMethodRequest> request) throws OperationNotFoundException, InvalidRequestException, OperationNotValidException {
        return updateChosenAuthMethodImpl(request);
    }

    private Response updateChosenAuthMethodImpl(ObjectRequest<UpdateChosenAuthMethodRequest> request) throws OperationNotFoundException, InvalidRequestException, OperationNotValidException {
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
     * @throws OperationNotValidException Thrown when operation is not valid.
     * @throws InvalidConfigurationException Thrown when Next Step configuration is invalid.
     */
    @Operation(summary = "Update mobile token status for an operation")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Mobile token status was updated"),
            @ApiResponse(responseCode = "400", description = "Invalid request, error codes: REQUEST_VALIDATION_FAILED, OPERATION_NOT_FOUND, OPERATION_NOT_VALID, INVALID_CONFIGURATION"),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
    })
    @PutMapping("operation/mobileToken/status")
    public Response updateMobileToken(@Valid @RequestBody ObjectRequest<UpdateMobileTokenRequest> request) throws OperationNotFoundException, OperationNotValidException, InvalidConfigurationException {
        return updateMobileTokenImpl(request);
    }

    /**
     * Update operation with chosen authentication method (POST method alternative).
     * @param request Update operation request.
     * @return Update operation response.
     * @throws OperationNotFoundException Thrown when operation is not found.
     * @throws OperationNotValidException Thrown when operation is not valid.
     * @throws InvalidConfigurationException Thrown when Next Step configuration is invalid.
     */
    @Operation(summary = "Update mobile token status for an operation")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Mobile token status was updated"),
            @ApiResponse(responseCode = "400", description = "Invalid request, error codes: REQUEST_VALIDATION_FAILED, OPERATION_NOT_FOUND, OPERATION_NOT_VALID, INVALID_CONFIGURATION"),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
    })
    @PostMapping("operation/mobileToken/status/update")
    public @ResponseBody Response updateMobileTokenPost(@Valid @RequestBody ObjectRequest<UpdateMobileTokenRequest> request) throws OperationNotFoundException, OperationNotValidException, InvalidConfigurationException {
        return updateMobileTokenImpl(request);
    }

    private Response updateMobileTokenImpl(ObjectRequest<UpdateMobileTokenRequest> request) throws OperationNotFoundException, OperationNotValidException, InvalidConfigurationException {
        logger.info("Received updateMobileToken request, operation ID: {}, mobile token active: {}", request.getRequestObject().getOperationId(), request.getRequestObject().isMobileTokenActive());
        // persist mobile token update
        operationPersistenceService.updateMobileToken(request.getRequestObject());
        logger.debug("The updateMobileToken request succeeded");
        return new Response();
    }

    /**
     * Get mobile token configuration.
     * @param userId User ID.
     * @param operationName Operation name.
     * @param authMethod Authentication method.
     * @return Get mobile token configuration response.
     * @throws InvalidConfigurationException Thrown when Next Step configuration is invalid.
     */
    @Operation(summary = "Get mobile token configuration")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Mobile token configuration sent in response"),
            @ApiResponse(responseCode = "400", description = "Invalid request, error codes: REQUEST_VALIDATION_FAILED, INVALID_CONFIGURATION"),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
    })
    @GetMapping("operation/mobileToken/config/detail")
    public ObjectResponse<GetMobileTokenConfigResponse> getMobileTokenConfig(@RequestParam @NotBlank @Size(min = 1, max = 256) String userId, @RequestParam @NotBlank @Size(min = 2, max = 256) String operationName, @RequestParam @NotNull AuthMethod authMethod) throws InvalidConfigurationException {
        logger.info("Received getMobileTokenConfig request, user ID: {}, operation name: {}, authentication method: {}", userId, operationName, authMethod);
        final boolean isMobileTokenEnabled = mobileTokenConfigurationService.isMobileTokenActive(userId, operationName, authMethod);
        final GetMobileTokenConfigResponse response = new GetMobileTokenConfigResponse();
        response.setMobileTokenEnabled(isMobileTokenEnabled);
        logger.debug("The getMobileTokenConfig request succeeded");
        return new ObjectResponse<>(response);
    }

    /**
     * Get mobile token configuration using POST method.
     * @param request Get mobile token configuration request.
     * @return Get mobile token configuration response.
     * @throws InvalidConfigurationException Thrown when Next Step configuration is invalid.
     */
    @Operation(summary = "Get mobile token configuration")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Mobile token configuration sent in response"),
            @ApiResponse(responseCode = "400", description = "Invalid request, error codes: REQUEST_VALIDATION_FAILED, INVALID_CONFIGURATION"),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
    })
    @PostMapping("operation/mobileToken/config/detail")
    public ObjectResponse<GetMobileTokenConfigResponse> getMobileTokenConfigPost(@Valid @RequestBody ObjectRequest<GetMobileTokenConfigRequest> request) throws InvalidConfigurationException {
        final String userId = request.getRequestObject().getUserId();
        final String operationName = request.getRequestObject().getOperationName();
        final AuthMethod authMethod = request.getRequestObject().getAuthMethod();
        logger.info("Received getMobileTokenConfigPost request, user ID: {}, operation name: {}, authentication method: {}", userId, operationName, authMethod);
        final boolean isMobileTokenEnabled = mobileTokenConfigurationService.isMobileTokenActive(userId, operationName, authMethod);
        final GetMobileTokenConfigResponse response = new GetMobileTokenConfigResponse();
        response.setMobileTokenEnabled(isMobileTokenEnabled);
        logger.debug("The getMobileTokenConfigPost request succeeded");
        return new ObjectResponse<>(response);
    }

    /**
     * Update application context for an operation (PUT method).
     * @param request Update application context request.
     * @return Response.
     * @throws OperationNotFoundException Thrown when operation is not found.
     */
    @Operation(summary = "Update application context for an operation")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Application context was updated"),
            @ApiResponse(responseCode = "400", description = "Invalid request, error codes: REQUEST_VALIDATION_FAILED, OPERATION_NOT_FOUND"),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
    })
    @PutMapping("operation/application")
    public Response updateApplicationContext(@Valid @RequestBody ObjectRequest<UpdateApplicationContextRequest> request) throws OperationNotFoundException {
        return updateApplicationContextImpl(request);
    }

    /**
     * Update application context for an operation (POST method alternative).
     * @param request Update application context request.
     * @return Response.
     * @throws OperationNotFoundException Thrown when operation is not found.
     */
    @Operation(summary = "Update application context for an operation")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Application context was updated"),
            @ApiResponse(responseCode = "400", description = "Invalid request, error codes: REQUEST_VALIDATION_FAILED, OPERATION_NOT_FOUND"),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
    })
    @PostMapping("operation/application/update")
    public Response updateApplicationContextPost(@Valid @RequestBody ObjectRequest<UpdateApplicationContextRequest> request) throws OperationNotFoundException {
        return updateApplicationContextImpl(request);
    }

    /**
     * Create an AFS action and store it in Next Step.
     * @param request Create AFS action request.
     * @return Response.
     */
    @Operation(summary = "Create an AFS action")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "AFS action was created"),
            @ApiResponse(responseCode = "400", description = "Invalid request, error codes: REQUEST_VALIDATION_FAILED"),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
    })
    @PostMapping("operation/afs/action")
    public Response createAfsAction(@Valid @RequestBody ObjectRequest<CreateAfsActionRequest> request) {
        final CreateAfsActionRequest afsRequest = request.getRequestObject();
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
     * Create an operation configuration.
     * @param request Create operation configuration request.
     * @return Create operation configuration response.
     * @throws OperationConfigAlreadyExists Thrown when operation configuration already exists.
     */
    @Operation(summary = "Create an operation configuration")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Operation configuration was created"),
            @ApiResponse(responseCode = "400", description = "Invalid request, error codes: REQUEST_VALIDATION_FAILED, OPERATION_CONFIG_ALREADY_EXISTS"),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
    })
    @PostMapping("operation/config")
    public ObjectResponse<CreateOperationConfigResponse> createOperationConfig(@Valid @RequestBody ObjectRequest<CreateOperationConfigRequest> request) throws OperationConfigAlreadyExists {
        logger.info("Received createOperationConfig request, operation name: {}", request.getRequestObject().getOperationName());
        final CreateOperationConfigResponse response = operationConfigurationService.createOperationConfig(request.getRequestObject());
        logger.info("The createOperationConfig request succeeded, operation name: {}", request.getRequestObject().getOperationName());
        return new ObjectResponse<>(response);
    }

    /**
     * Delete an operation configuration.
     * @param request Delete operation configuration request.
     * @return Delete operation configuration response.
     * @throws OperationConfigNotFoundException Thrown when operation configuration is not found.
     * @throws DeleteNotAllowedException Thrown when delete action is not allowed.
     */
    @Operation(summary = "Delete an operation configuration")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Operation configuration was deleted"),
            @ApiResponse(responseCode = "400", description = "Invalid request, error codes: REQUEST_VALIDATION_FAILED, OPERATION_CONFIG_NOT_FOUND, DELETE_NOT_ALLOWED"),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
    })
    @PostMapping("operation/config/delete")
    public ObjectResponse<DeleteOperationConfigResponse> deleteOperationConfig(@Valid @RequestBody ObjectRequest<DeleteOperationConfigRequest> request) throws OperationConfigNotFoundException, DeleteNotAllowedException {
        logger.info("Received deleteOperationConfig request, operation name: {}", request.getRequestObject().getOperationName());
        final DeleteOperationConfigResponse response = operationConfigurationService.deleteOperationConfig(request.getRequestObject());
        logger.info("The deleteOperationConfig request succeeded, operation name: {}", request.getRequestObject().getOperationName());
        return new ObjectResponse<>(response);
    }

    /**
     * Create a configuration for authentication method by operation name.
     * @param request Create operation and authentication method configuration request.
     * @return Create operation and authentication method configuration response.
     * @throws OperationMethodConfigAlreadyExists Thrown when operation and authentication method configuration already exists.
     * @throws OperationConfigNotFoundException Thrown when operation configuration is not found.
     * @throws AuthMethodNotFoundException Thrown when authentication method is not found.
     */
    @Operation(summary = "Create an operation and authentication method configuration")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Operation and authentication method configuration was created"),
            @ApiResponse(responseCode = "400", description = "Invalid request, error codes: REQUEST_VALIDATION_FAILED, OPERATION_METHOD_CONFIG_ALREADY_EXISTS, OPERATION_CONFIG_NOT_FOUND, AUTH_METHOD_NOT_FOUND"),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
    })
    @PostMapping("operation/auth-method/config")
    public ObjectResponse<CreateOperationMethodConfigResponse> createOperationMethodConfig(@Valid @RequestBody ObjectRequest<CreateOperationMethodConfigRequest> request) throws OperationMethodConfigAlreadyExists, OperationConfigNotFoundException, AuthMethodNotFoundException {
        logger.info("Received createOperationMethodConfig request, operation name: {}, authentication method: {}", request.getRequestObject().getOperationName(), request.getRequestObject().getAuthMethod());
        final CreateOperationMethodConfigResponse response = operationConfigurationService.createOperationMethodConfig(request.getRequestObject());
        logger.info("The createOperationMethodConfig request succeeded, operation name: {}, authentication method: {}", request.getRequestObject().getOperationName(), request.getRequestObject().getAuthMethod());
        return new ObjectResponse<>(response);
    }

    /**
     * Get a configuration for operation and authentication method.
     * @param operationName Operation name.
     * @param authMethod Authentication method.
     * @return Get operation and authentication method config detail response.
     * @throws OperationMethodConfigNotFoundException Thrown when operation and authentication method configuration is not found.
     */
    @Operation(summary = "Get an operation and authentication method configuration detail")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Operation and authentication method configuration detail sent in response"),
            @ApiResponse(responseCode = "400", description = "Invalid request, error codes: REQUEST_VALIDATION_FAILED, OPERATION_METHOD_CONFIG_NOT_FOUND"),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
    })
    @GetMapping("operation/auth-method/config/detail")
    public ObjectResponse<GetOperationMethodConfigDetailResponse> getOperationMethodConfigDetail(@RequestParam @NotBlank @Size(min = 2, max = 256) String operationName, @RequestParam @NotNull AuthMethod authMethod) throws OperationMethodConfigNotFoundException {
        logger.info("Received getOperationMethodConfigDetail request, operation name: {}, authentication method: {}", operationName, authMethod);
        GetOperationMethodConfigDetailRequest request = new GetOperationMethodConfigDetailRequest();
        request.setOperationName(operationName);
        request.setAuthMethod(authMethod);
        final GetOperationMethodConfigDetailResponse response = operationConfigurationService.getOperationMethodConfigDetail(request);
        logger.info("The getOperationMethodConfigDetail request succeeded, operation name: {}, authentication method: {}", operationName, authMethod);
        return new ObjectResponse<>(response);
    }

    /**
     * Get a configuration for operation and authentication method using POST method.
     * @param request Get operation and authentication method config detail request.
     * @return Get operation and authentication method config detail response.
     * @throws OperationMethodConfigNotFoundException Thrown when operation and authentication method configuration is not found.
     */
    @Operation(summary = "Get an operation and authentication method configuration detail")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Operation and authentication method configuration detail sent in response"),
            @ApiResponse(responseCode = "400", description = "Invalid request, error codes: REQUEST_VALIDATION_FAILED, OPERATION_METHOD_CONFIG_NOT_FOUND"),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
    })
    @PostMapping("operation/auth-method/config/detail")
    public ObjectResponse<GetOperationMethodConfigDetailResponse> getOperationMethodConfigDetailPost(@Valid @RequestBody ObjectRequest<GetOperationMethodConfigDetailRequest> request) throws OperationMethodConfigNotFoundException {
        logger.info("Received getOperationMethodConfigDetailPost request, operation name: {}, authentication method: {}", request.getRequestObject().getOperationName(), request.getRequestObject().getAuthMethod());
        final GetOperationMethodConfigDetailResponse response = operationConfigurationService.getOperationMethodConfigDetail(request.getRequestObject());
        logger.info("The getOperationMethodConfigDetailPost request succeeded, operation name: {}, authentication method: {}", request.getRequestObject().getOperationName(), request.getRequestObject().getAuthMethod());
        return new ObjectResponse<>(response);
    }

    /**
     * Delete a configuration for operation and authentication method.
     * @param request Delete operation and authentication method config request.
     * @return Delete operation and authentication method config response.
     * @throws OperationMethodConfigNotFoundException Thrown when operation and authentication method configuration is not found.
     */
    @Operation(summary = "Delete an operation and authentication method configuration detail")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Operation and authentication method configuration detail was deleted"),
            @ApiResponse(responseCode = "400", description = "Invalid request, error codes: REQUEST_VALIDATION_FAILED, OPERATION_METHOD_CONFIG_NOT_FOUND"),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
    })
    @PostMapping("operation/auth-method/config/delete")
    public ObjectResponse<DeleteOperationMethodConfigResponse> deleteOperationMethodConfig(@Valid @RequestBody ObjectRequest<DeleteOperationMethodConfigRequest> request) throws OperationMethodConfigNotFoundException {
        logger.info("Received deleteOperationMethodConfig request, operation name: {}, authentication method: {}", request.getRequestObject().getOperationName(), request.getRequestObject().getAuthMethod());
        final DeleteOperationMethodConfigResponse response = operationConfigurationService.deleteOperationMethodConfig(request.getRequestObject());
        logger.info("The deleteOperationMethodConfig request succeeded, operation name: {}, authentication method: {}", request.getRequestObject().getOperationName(), request.getRequestObject().getAuthMethod());
        return new ObjectResponse<>(response);
    }

}