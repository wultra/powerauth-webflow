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
package io.getlime.security.powerauth.app.nextstep.service;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wultra.core.audit.base.Audit;
import com.wultra.core.audit.base.model.AuditDetail;
import com.wultra.security.powerauth.client.model.enumeration.OperationStatus;
import com.wultra.security.powerauth.client.model.response.OperationDetailResponse;
import io.getlime.security.powerauth.app.nextstep.repository.AuthenticationRepository;
import io.getlime.security.powerauth.app.nextstep.repository.OperationHistoryRepository;
import io.getlime.security.powerauth.app.nextstep.repository.OperationRepository;
import io.getlime.security.powerauth.app.nextstep.repository.OrganizationRepository;
import io.getlime.security.powerauth.app.nextstep.repository.catalogue.RepositoryCatalogue;
import io.getlime.security.powerauth.app.nextstep.repository.model.entity.*;
import io.getlime.security.powerauth.app.nextstep.service.adapter.OperationCustomizationService;
import io.getlime.security.powerauth.app.nextstep.service.catalogue.ServiceCatalogue;
import io.getlime.security.powerauth.lib.nextstep.model.entity.ApplicationContext;
import io.getlime.security.powerauth.lib.nextstep.model.entity.AuthStep;
import io.getlime.security.powerauth.lib.nextstep.model.entity.EnableMobileTokenResult;
import io.getlime.security.powerauth.lib.nextstep.model.entity.OperationFormData;
import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.UserAccountStatus;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthMethod;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthResult;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthStepResult;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.OperationCancelReason;
import io.getlime.security.powerauth.lib.nextstep.model.exception.*;
import io.getlime.security.powerauth.lib.nextstep.model.request.*;
import io.getlime.security.powerauth.lib.nextstep.model.response.CreateOperationResponse;
import io.getlime.security.powerauth.lib.nextstep.model.response.UpdateOperationResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * This service handles conversion of operation request/response objects into operation entities.
 * Operation entities are persisted, so that they can be later retrieved from the database.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Service
public class OperationPersistenceService {

    private final Logger logger = LoggerFactory.getLogger(OperationPersistenceService.class);
    private static final String AUDIT_TYPE_OPERATION = "OPERATION";

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final OperationRepository operationRepository;
    private final OrganizationRepository organizationRepository;
    private final OperationHistoryRepository operationHistoryRepository;
    private final AuthenticationRepository authenticationRepository;
    private final ServiceCatalogue serviceCatalogue;
    private final Audit audit;

    {
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
    }

    /**
     * Service constructor.
     * @param repositoryCatalogue Repository catalogue.
     * @param serviceCatalogue Service catalogue.
     * @param audit Audit interface.
     */
    @Autowired
    public OperationPersistenceService(RepositoryCatalogue repositoryCatalogue, @Lazy ServiceCatalogue serviceCatalogue, Audit audit) {
        this.operationRepository = repositoryCatalogue.getOperationRepository();
        this.organizationRepository = repositoryCatalogue.getOrganizationRepository();
        this.operationHistoryRepository = repositoryCatalogue.getOperationHistoryRepository();
        this.authenticationRepository = repositoryCatalogue.getAuthenticationRepository();
        this.serviceCatalogue = serviceCatalogue;
        this.audit = audit;
    }

    /**
     * Convert a CreateOperationRequest and CreateOperationResponse into OperationEntity and OperationHistoryEntity.
     * Both entities are persisted to store both the operation and its history in the database.
     *
     * @param request  create request received from the client
     * @param response create response generated for the client
     * @throws OrganizationNotFoundException Thrown when organization is not found.
     */
    public void createOperation(CreateOperationRequest request, CreateOperationResponse response) throws OrganizationNotFoundException {
        final IdGeneratorService idGeneratorService = serviceCatalogue.getIdGeneratorService();
        OperationEntity operation = new OperationEntity();
        operation.setOperationName(request.getOperationName());
        operation.setOperationData(request.getOperationData());
        operation.setOperationId(response.getOperationId());
        operation.setUserId(request.getUserId());
        if (request.getOrganizationId() != null) {
            final Optional<OrganizationEntity> organizationOptional = organizationRepository.findById(request.getOrganizationId());
            if (!organizationOptional.isPresent()) {
                throw new OrganizationNotFoundException("Organization not found: " + request.getOrganizationId());
            }
            operation.setOrganization(organizationOptional.get());
        }
        operation.setExternalOperationName(request.getOperationNameExternal());
        operation.setExternalTransactionId(request.getExternalTransactionId());
        operation.setResult(response.getResult());
        if (request.getApplicationContext() != null) {
            // Assign operation context to entity in case it was sent in request
            assignApplicationContext(operation, request.getApplicationContext());
        }
        if (request.getFormData() != null) {
            try {
                // Store form data as serialized JSON string.
                operation.setOperationFormData(objectMapper.writeValueAsString(request.getFormData()));
            } catch (JsonProcessingException ex) {
                logger.error("Error while serializing operation form data", ex);
                audit.error("Error while serializing operation form data", ex);
            }
        }
        operation.setTimestampCreated(response.getTimestampCreated());
        operation.setTimestampExpires(response.getTimestampExpires());
        operation = operationRepository.save(operation);
        logger.debug("Operation was created, operation ID: {}, operation name: {}", operation.getOperationId(), operation.getOperationId());
        final OperationHistoryEntity operationHistory = new OperationHistoryEntity(operation.getOperationId(),
                idGeneratorService.generateOperationHistoryId(operation.getOperationId()));
        operationHistory.setRequestAuthMethod(AuthMethod.INIT);
        operationHistory.setRequestAuthStepResult(AuthStepResult.CONFIRMED);
        operationHistory.setResponseResult(response.getResult());
        operationHistory.setResponseResultDescription(response.getResultDescription());
        try {
            // Params and steps are saved as JSON for now - new entities would be required to store this data.
            // We can add these entities later in case they are needed.
            operationHistory.setRequestParams(objectMapper.writeValueAsString(request.getParams()));
            operationHistory.setResponseSteps(objectMapper.writeValueAsString(response.getSteps()));
        } catch (JsonProcessingException ex) {
            logger.error("Error while serializing operation history", ex);
            audit.error("Error while serializing operation history", ex);
        }
        operationHistory.setResponseTimestampCreated(response.getTimestampCreated());
        operationHistory.setResponseTimestampExpires(response.getTimestampExpires());
        operationHistoryRepository.save(operationHistory);
        audit.info("Operation was created", AuditDetail.builder()
                .type(AUDIT_TYPE_OPERATION)
                .param("operationId", operation.getOperationId())
                .param("operationName", operation.getOperationName())
                .param("operationData", operation.getOperationData())
                .param("externalOperationName", operation.getExternalOperationName())
                .param("externalTransactionId", operation.getExternalTransactionId())
                .param("userId", operation.getUserId())
                .param("organizationId", operation.getOperationId())
                .param("requestAuthMethod", operationHistory.getRequestAuthMethod())
                .param("requestAuthStepResult", operationHistory.getRequestAuthStepResult())
                .param("responseResult", operationHistory.getResponseResult())
                .param("responseSteps", operationHistory.getResponseSteps())
                .build());
        audit.debug("Operation was created (detail)", AuditDetail.builder()
                .type(AUDIT_TYPE_OPERATION)
                .param("operationId", operation.getOperationId())
                .param("operation", operation)
                .build());
    }

    /**
     * Update an operation.
     * @param request Update operation request.
     * @return Update operation response.
     * @throws OperationAlreadyFailedException Thrown when operation is already failed.
     * @throws OperationAlreadyFinishedException Thrown when operation is already finished.
     * @throws OperationAlreadyCanceledException Thrown when operation is already canceled.
     * @throws AuthMethodNotFoundException Thrown when authentication method is not found.
     * @throws OperationNotFoundException Thrown when operation is not found.
     * @throws OperationNotValidException Thrown when operation is not valid.
     * @throws InvalidConfigurationException Thrown when Next Step configuration is invalid.
     * @throws InvalidRequestException Thrown when request is invalid.
     * @throws OrganizationNotFoundException Thrown when organization is not found.
     */
    public UpdateOperationResponse updateOperation(UpdateOperationRequest request) throws OperationAlreadyFailedException, OperationAlreadyFinishedException, OperationAlreadyCanceledException, AuthMethodNotFoundException, OperationNotFoundException, OperationNotValidException, InvalidConfigurationException, InvalidRequestException, OrganizationNotFoundException {
        final StepResolutionService stepResolutionService = serviceCatalogue.getStepResolutionService();
        // Resolve response based on dynamic step definitions
        final UpdateOperationResponse response = stepResolutionService.resolveNextStepResponse(request);

        // Persist operation update
        updateOperation(request, response);
        return response;
    }

    /**
     * Convert an UpdateOperationRequest and UpdateOperationResponse into OperationEntity and OperationHistoryEntity.
     * Both entities are persisted to update the status of processed operation as well as update its history.
     *
     * @param request  create request received from the client
     * @param response create response generated for the client
     * @throws OperationNotFoundException Thrown when operation does not exist.
     * @throws OrganizationNotFoundException Thrown when organization is not found.
     */
    private void updateOperation(UpdateOperationRequest request, UpdateOperationResponse response) throws OperationNotFoundException, OrganizationNotFoundException {
        final IdGeneratorService idGeneratorService = serviceCatalogue.getIdGeneratorService();
        final OperationCustomizationService operationCustomizationService = serviceCatalogue.getOperationCustomizationService();

        final Optional<OperationEntity> operationOptional = operationRepository.findById(response.getOperationId());
        if (!operationOptional.isPresent()) {
            throw new OperationNotFoundException("Operation not found, operation ID: " + response.getOperationId());
        }
        OperationEntity operation = operationOptional.get();
        final AuthResult originalResult = operation.getResult();
        if (request.getUserId() != null) {
            operation.setUserId(request.getUserId());
        }
        if (request.getOrganizationId() != null) {
            final Optional<OrganizationEntity> organizationOptional = organizationRepository.findById(request.getOrganizationId());
            if (!organizationOptional.isPresent()) {
                throw new OrganizationNotFoundException("Organization not found: " + request.getOrganizationId());
            }
            operation.setOrganization(organizationOptional.get());
        }
        operation.setResult(response.getResult());
        if (request.getApplicationContext() != null) {
            // Do not remove application context in case it was not sent in request
            assignApplicationContext(operation, request.getApplicationContext());
        }
        // operation expiration time matches current response expiration time
        operation.setTimestampExpires(response.getTimestampExpires());

        final OperationHistoryEntity operationHistory = new OperationHistoryEntity(operation.getOperationId(),
                idGeneratorService.generateOperationHistoryId(operation.getOperationId()));
        operationHistory.setRequestAuthMethod(request.getAuthMethod());
        operationHistory.setRequestAuthStepResult(request.getAuthStepResult());
        if (request.getAuthenticationId() != null) {
            final Optional<AuthenticationEntity> authenticationOptional = authenticationRepository.findById(request.getAuthenticationId());
            authenticationOptional.ifPresent(operationHistory::setAuthentication);
        }
        operationHistory.setResponseResult(response.getResult());
        operationHistory.setResponseResultDescription(response.getResultDescription());
        operationHistory.setChosenAuthMethod(response.getChosenAuthMethod());
        operationHistory.setMobileTokenActive(response.isMobileTokenActive());
        operationHistory.setPowerAuthOperationId(response.getPowerAuthOperationId());
        try {
            // Params, steps and auth instruments are saved as JSON for now - new entities would be required to store this data.
            // We can add these entities later in case they are needed.
            operationHistory.setRequestAuthInstruments(objectMapper.writeValueAsString(request.getAuthInstruments()));
            operationHistory.setRequestParams(objectMapper.writeValueAsString(request.getParams()));
            operationHistory.setResponseSteps(objectMapper.writeValueAsString(response.getSteps()));
        } catch (JsonProcessingException e) {
            logger.error("Error occurred while serializing operation history", e);
            audit.error("Error occurred while serializing operation history", e);
        }
        operationHistory.setResponseTimestampCreated(response.getTimestampCreated());
        operationHistory.setResponseTimestampExpires(response.getTimestampExpires());
        operation.getOperationHistory().add(operationHistory);
        operation = operationRepository.save(operation);
        logger.debug("Operation was updated, operation ID: {}, operation name: {}", operation.getOperationId(), operation.getOperationId());
        audit.info("Operation was updated", AuditDetail.builder()
                .type(AUDIT_TYPE_OPERATION)
                .param("operationId", operation.getOperationId())
                .param("userId", operation.getUserId())
                .param("organizationId", operation.getOrganization() != null ? operation.getOrganization().getOrganizationId() : null)
                .param("requestAuthMethod", operationHistory.getRequestAuthMethod())
                .param("requestAuthStepResult", operationHistory.getRequestAuthStepResult())
                .param("requestAuthInstruments", operationHistory.getRequestAuthInstruments())
                .param("responseResult", operationHistory.getResponseResult())
                .param("responseSteps", operationHistory.getResponseSteps())
                .build());
        if (!originalResult.equals(operation.getResult())) {
            operationCustomizationService.notifyOperationChange(operation);
        }
    }

    /**
     * Update user ID and organization ID for an operation.
     * @param request Update operation user request.
     * @throws OperationNotFoundException Thrown when operation does not exist.
     * @throws OrganizationNotFoundException Thrown when organization is not found.
     */
    public void updateOperationUser(UpdateOperationUserRequest request) throws OperationNotFoundException, OrganizationNotFoundException {
        final String operationId = request.getOperationId();
        final String userId = request.getUserId();
        final String organizationId = request.getOrganizationId();
        final UserAccountStatus accountStatus = request.getAccountStatus();
        final OperationEntity operation = getOperation(operationId);
        operation.setUserId(userId);
        if (organizationId != null) {
            final Optional<OrganizationEntity> organizationOptional = organizationRepository.findById(organizationId);
            if (!organizationOptional.isPresent()) {
                throw new OrganizationNotFoundException("Organization not found: " + organizationId);
            }
            operation.setOrganization(organizationOptional.get());
        }
        if (accountStatus != null) {
            operation.setUserAccountStatus(accountStatus);
        }
        operationRepository.save(operation);
        logger.debug("Operation user was updated, operation ID: {}, operation name: {}", operation.getOperationId(), operation.getOperationId());
        audit.info("Operation user was updated", AuditDetail.builder()
                .type(AUDIT_TYPE_OPERATION)
                .param("operationId", operation.getOperationId())
                .param("userId", operation.getUserId())
                .param("organizationId", operation.getOrganization().getOrganizationId())
                .build());
    }

    /**
     * Updates form data for given operation.
     *
     * @param request Request to update form data.
     * @throws OperationNotFoundException Thrown when operation does not exist.
     */
    public void updateFormData(UpdateFormDataRequest request) throws OperationNotFoundException {
        final Optional<OperationEntity> operationOptional = operationRepository.findById(request.getOperationId());
        if (!operationOptional.isPresent()) {
            throw new OperationNotFoundException("Operation not found, operation ID: " + request.getOperationId());
        }
        final OperationEntity operation = operationOptional.get();
        try {
            final OperationFormData formData = objectMapper.readValue(operation.getOperationFormData(), OperationFormData.class);
            // update only formData.userInput which should contain all input from the user
            formData.getUserInput().putAll(request.getFormData().getUserInput());
            operation.setOperationFormData(objectMapper.writeValueAsString(formData));
        } catch (IOException e) {
            logger.error("Error occurred while serializing operation form data", e);
            audit.error("Error occurred while serializing operation form data", e);
        }
        operationRepository.save(operation);
        logger.debug("Operation form data was updated, operation ID: {}, operation name: {}", operation.getOperationId(), operation.getOperationId());
        audit.info("Operation form data was updated", AuditDetail.builder()
                .type(AUDIT_TYPE_OPERATION)
                .param("operationId", operation.getOperationId())
                .build());
        audit.debug("Operation form data was updated (detail)", AuditDetail.builder()
                .type(AUDIT_TYPE_OPERATION)
                .param("operationId", operation.getOperationId())
                .param("formData", operation.getOperationFormData())
                .build());
    }

    /**
     * Update chosen authentication method.
     *
     * @param request Request to update chosen authentication method.
     * @throws OperationNotFoundException Thrown when operation does not exist.
     * @throws InvalidRequestException Thrown when request is invalid.
     * @throws OperationNotValidException Thrown when operation is invalid.
     */
    public void updateChosenAuthMethod(UpdateChosenAuthMethodRequest request) throws OperationNotFoundException, InvalidRequestException, OperationNotValidException {
        final Optional<OperationEntity> operationOptional = operationRepository.findById(request.getOperationId());
        if (!operationOptional.isPresent()) {
            throw new OperationNotFoundException("Operation not found, operation ID: " + request.getOperationId());
        }
        final OperationEntity operation = operationOptional.get();
        updateChosenAuthMethod(operation, request.getChosenAuthMethod());
    }

    /**
     * Update chosen authentication method.
     *
     * @param operation Operation.
     * @param chosenAuthMethod Chosen authentication method.
     * @throws OperationNotValidException Thrown when operation is invalid.
     * @throws InvalidRequestException Thrown when request is invalid.
     */
    public void updateChosenAuthMethod(OperationEntity operation, AuthMethod chosenAuthMethod) throws OperationNotValidException, InvalidRequestException {
        final OperationHistoryEntity currentHistory = operation.getCurrentOperationHistoryEntity();
        if (currentHistory == null) {
            throw new OperationNotValidException("Operation is missing history");
        }
        boolean chosenAuthMethodValid = false;
        for (AuthStep step : getResponseAuthSteps(operation)) {
            if (step.getAuthMethod() == chosenAuthMethod) {
                chosenAuthMethodValid = true;
                break;
            }
        }
        if (!chosenAuthMethodValid) {
            throw new InvalidRequestException("Invalid chosen authentication method");
        }
        currentHistory.setChosenAuthMethod(chosenAuthMethod);
        operationHistoryRepository.save(currentHistory);
        audit.info("Operation chosen auth method was updated", AuditDetail.builder()
                .type(AUDIT_TYPE_OPERATION)
                .param("operationId", operation.getOperationId())
                .param("chosenAuthMethod", chosenAuthMethod)
                .build());
        logger.debug("Operation chosen authentication method was updated, operation ID: {}, operation name: {}", operation.getOperationId(), operation.getOperationId());
    }

    /**
     * Update mobile token status.
     *
     * @param request Request to update mobile token status.
     * @throws OperationNotFoundException Thrown when operation does not exist.
     * @throws OperationNotValidException Thrown when operation is not valid.
     * @throws InvalidConfigurationException Thrown when Next Step configuration is invalid.
     */
    public void updateMobileToken(UpdateMobileTokenRequest request) throws OperationNotFoundException, OperationNotValidException, InvalidConfigurationException {
        final MobileTokenConfigurationService mobileTokenConfigurationService = serviceCatalogue.getMobileTokenConfigurationService();
        final Optional<OperationEntity> operationOptional = operationRepository.findById(request.getOperationId());
        if (!operationOptional.isPresent()) {
            throw new OperationNotFoundException("Operation not found, operation ID: " + request.getOperationId());
        }
        final OperationEntity operation = operationOptional.get();
        final OperationHistoryEntity currentHistory = operation.getCurrentOperationHistoryEntity();
        if (currentHistory == null) {
            throw new OperationNotValidException("Operation is missing history");
        }
        if (request.isMobileTokenActive()) {
            final EnableMobileTokenResult result = mobileTokenConfigurationService.enableMobileToken(operation);
            currentHistory.setMobileTokenActive(result.isEnabled());
            currentHistory.setPowerAuthOperationId(result.getPowerAuthOperationId());
        } else {
            currentHistory.setMobileTokenActive(false);
            currentHistory.setPowerAuthOperationId(null);
        }
        operationHistoryRepository.save(currentHistory);
        audit.info("Operation mobile token status was updated", AuditDetail.builder()
                .type(AUDIT_TYPE_OPERATION)
                .param("operationId", operation.getOperationId())
                .param("mobileTokenActive", currentHistory.isMobileTokenActive())
                .param("powerAuthOperationId", currentHistory.getPowerAuthOperationId())
                .build());
        logger.debug("Operation mobile token was updated, operation ID: {}, operation name: {}", operation.getOperationId(), operation.getOperationId());
    }

    /**
     * Update application context.
     *
     * @param request Request to update application context.
     * @throws OperationNotFoundException Thrown when operation does not exist.
     */
    public void updateApplicationContext(UpdateApplicationContextRequest request) throws OperationNotFoundException {
        final Optional<OperationEntity> operationOptional = operationRepository.findById(request.getOperationId());
        if (!operationOptional.isPresent()) {
            throw new OperationNotFoundException("Operation not found, operation ID: " + request.getOperationId());
        }
        final OperationEntity operation = operationOptional.get();
        final ApplicationContext applicationContext = request.getApplicationContext();
        try {
            if (applicationContext == null) {
                operation.setApplicationId(null);
                operation.setApplicationName(null);
                operation.setApplicationDescription(null);
                operation.setApplicationOriginalScopes(null);
                operation.setApplicationExtras(objectMapper.writeValueAsString(Collections.emptyMap()));
            } else {
                operation.setApplicationId(applicationContext.getId());
                operation.setApplicationName(applicationContext.getName());
                operation.setApplicationDescription(applicationContext.getDescription());
                operation.setApplicationOriginalScopes(objectMapper.writeValueAsString(applicationContext.getOriginalScopes()));
                operation.setApplicationExtras(objectMapper.writeValueAsString(applicationContext.getExtras()));
            }
        } catch (IOException e) {
            logger.error("Error occurred while serializing application attributes for an operation", e);
            audit.error("Error occurred while serializing application attributes for an operation", e);
        }
        operationRepository.save(operation);
        audit.info("Operation application context was updated", AuditDetail.builder()
                .type(AUDIT_TYPE_OPERATION)
                .param("operationId", operation.getOperationId())
                .param("applicationContext", applicationContext)
                .build());
        logger.debug("Operation application was updated, operation ID: {}, operation name: {}", operation.getOperationId(), operation.getOperationId());
    }

    /**
     * Retrieve an OperationEntity for given operationId from database.
     *
     * @param operationId ID of an operation.
     * @return OperationEntity loaded from database.
     * @throws OperationNotFoundException Thrown when operation does not exist.
     */
    public OperationEntity getOperation(String operationId) throws OperationNotFoundException {
        try {
            return getOperation(operationId, false);
        } catch (OperationNotValidException ex) {
            // Not possible, operation is not validated
            return null;
        }
    }

    /**
     * Retrieve an OperationEntity for given operationId from database.
     *
     * @param operationId ID of an operation.
     * @param validateOperation Whether operation should be validated.
     * @return OperationEntity loaded from database.
     * @throws OperationNotFoundException Thrown when operation does not exist.
     * @throws OperationNotValidException Thrown when operation is invalid.
     */
    public OperationEntity getOperation(String operationId, boolean validateOperation) throws OperationNotFoundException, OperationNotValidException {
        final Optional<OperationEntity> operationOptional = operationRepository.findById(operationId);
        if (!operationOptional.isPresent()) {
            throw new OperationNotFoundException("Operation not found, operation ID: " + operationId);
        }
        final OperationEntity operation = operationOptional.get();
        if (validateOperation) {
            validateMobileTokenOperation(operation);
        }
        return operation;
    }

    /**
     * Return whether operation exists.
     *
     * @param operationId Operation ID.
     * @return Whether operation exists.
     */
    public boolean operationExists(String operationId) {
        final Optional<OperationEntity> operationOptional = operationRepository.findById(operationId);
        return operationOptional.isPresent();
    }

    /**
     * Retrieve list of pending operations for given user ID from database.
     *
     * @param userId User ID.
     * @param mobileTokenOnly Whether pending operation list should be filtered for only next step with mobile token support.
     * @return List of operations which match the query.
     */
    public List<OperationEntity> getPendingOperations(String userId, boolean mobileTokenOnly) {
        final List<OperationEntity> entities = operationRepository.findPendingOperationsForUser(userId);
        if (!mobileTokenOnly) {
            // Return all unfinished operations for user
            return entities;
        }
        final List<OperationEntity> filteredList = new ArrayList<>();
        for (OperationEntity operation : entities) {
            try {
                final boolean mobileTokenActive = validateMobileTokenOperation(operation);
                if (mobileTokenActive) {
                    filteredList.add(operation);
                }
            } catch (OperationNotValidException ex) {
                // Invalid operations are skipped
                logger.warn(ex.getMessage(), ex);
                audit.warn(ex.getMessage(), ex);
            }
        }
        return filteredList;
    }

    /**
     * Validate a mobile token operation status.
     * @param operation Operation entity.
     * @return Whether operation is a pending operation with an active PowerAuth token.
     * @throws OperationNotValidException Thrown when operation is invalid.
     */
    private boolean validateMobileTokenOperation(OperationEntity operation) throws OperationNotValidException {
        final PowerAuthOperationService powerAuthOperationService = serviceCatalogue.getPowerAuthOperationService();
        final OperationHistoryEntity currentHistoryEntity = operation.getCurrentOperationHistoryEntity();
        if (currentHistoryEntity != null && currentHistoryEntity.getResponseResult() == AuthResult.CONTINUE && currentHistoryEntity.isMobileTokenActive()) {
            if (currentHistoryEntity.getPowerAuthOperationId() == null) {
                // PowerAuth operation was not created, but mobile token is active
                return true;
            }
            // PowerAuth operation was created, reconcile states of both operations
            final OperationDetailResponse detail = powerAuthOperationService.getOperationDetail(operation);
            if (detail == null) {
                return false;
            }
            // PowerAuth operation expired, cancel Next Step operation
            if (detail.getStatus() == OperationStatus.EXPIRED) {
                handlePowerAuthOperationExpiration(operation);
                return false;
            }
            // PowerAuth operation expires before Next Step operation, update expiration time
            if (detail.getTimestampExpires() != null && detail.getTimestampExpires().before(operation.getTimestampExpires())) {
                operation.setTimestampExpires(detail.getTimestampExpires());
            }
            return true;
        }
        return false;
    }

    /**
     * Handle status change of PowerAuth operation when it expires in PowerAuth server.
     * @param operation Operation entity.
     * @throws OperationNotValidException Thrown when operation is invalid.
     */
    private void handlePowerAuthOperationExpiration(OperationEntity operation) throws OperationNotValidException {
        // Operation expired in PowerAuth server, cancel Next Step operation
        final UpdateOperationRequest request = new UpdateOperationRequest();
        request.setOperationId(operation.getOperationId());
        request.setUserId(operation.getUserId());
        request.setOperationId(operation.getOperationId());
        final OperationHistoryEntity currentHistory = operation.getCurrentOperationHistoryEntity();
        if (currentHistory == null) {
            throw new OperationNotValidException("Operation is missing history");
        }
        request.setAuthMethod(currentHistory.getChosenAuthMethod());
        request.setAuthStepResult(AuthStepResult.CANCELED);
        request.setAuthStepResultDescription(OperationCancelReason.TIMED_OUT_OPERATION.toString());
        try {
            updateOperation(request);
        } catch (Exception ex) {
            logger.warn(ex.getMessage(), ex);
        }
    }

    /**
     * Retrieve list of operations for given external transaction ID from database.
     *
     * @param externalTransactionId External transaction ID.
     * @return List of operations which match the query.
     */
    public List<OperationEntity> findByExternalTransactionId(String externalTransactionId) {
        return operationRepository.findAllByExternalTransactionId(externalTransactionId);
    }

    /**
     * Gets the list of @{link AuthStep} for an operation. Steps from the current response are returned.
     * In case no history is available, empty list is returned.
     *
     * @param operation Operation entity.
     * @return List of {@link AuthStep}.
     */
    public List<AuthStep> getResponseAuthSteps(OperationEntity operation) {
        final List<AuthStep> steps = new ArrayList<>();
        if (operation == null) {
            return steps;
        }
        final OperationHistoryEntity currentHistory = operation.getCurrentOperationHistoryEntity();
        if (currentHistory == null) {
            return steps;
        }
        // get steps from the current response
        final String responseSteps = currentHistory.getResponseSteps();
        if (responseSteps == null) {
            return steps;
        }
        try {
            steps.addAll(objectMapper.readValue(responseSteps, new TypeReference<List<AuthStep>>() {}));
            return steps;
        } catch (IOException e) {
            // in case of an error empty list is returned
            logger.error("Error occurred while deserializing response steps", e);
            audit.error("Error occurred while deserializing response steps", e);
        }
        return steps;
    }

    /**
     * Create an AFS action.
     * @param request Request to crete an AFS action.
     */
    public void createAfsAction(CreateAfsActionRequest request) {
        try {
            final OperationAfsActionEntity afsEntity = new OperationAfsActionEntity();
            final OperationEntity operation = getOperation(request.getOperationId());
            afsEntity.setOperation(operation);
            afsEntity.setAfsAction(request.getAfsAction());
            afsEntity.setStepIndex(request.getStepIndex());
            afsEntity.setRequestAfsExtras(request.getRequestAfsExtras());
            afsEntity.setAfsLabel(request.getAfsLabel());
            afsEntity.setAfsResponseApplied(request.isAfsResponseApplied());
            afsEntity.setResponseAfsExtras(request.getResponseAfsExtras());
            afsEntity.setTimestampCreated(request.getTimestampCreated());
            operation.getAfsActions().add(afsEntity);
            operationRepository.save(operation);
            logger.debug("Operation AFS action was created, operation ID: {}, operation name: {}", operation.getOperationId(), operation.getOperationId());
            audit.info("Operation AFS action was created", AuditDetail.builder()
                    .type(AUDIT_TYPE_OPERATION)
                    .param("operationId", operation.getOperationId())
                    .param("afsAction", afsEntity)
                    .build());
        } catch (OperationNotFoundException e) {
            logger.error("AFS action could not be saved because operation does not exist: {}", request.getOperationId());
            audit.error("AFS action could not be saved because operation does not exist: {}", request.getOperationId());
        }
    }

    /**
     * Assign application context to an operation entity.
     *
     * @param operationEntity    Operation entity.
     * @param applicationContext Application context.
     */
    private void assignApplicationContext(OperationEntity operationEntity, ApplicationContext applicationContext) {
        operationEntity.setApplicationId(applicationContext.getId());
        operationEntity.setApplicationName(applicationContext.getName());
        operationEntity.setApplicationDescription(applicationContext.getDescription());
        // Extras and original scopes are saved as JSON
        try {
            operationEntity.setApplicationExtras(objectMapper.writeValueAsString(applicationContext.getExtras()));
            operationEntity.setApplicationOriginalScopes(objectMapper.writeValueAsString(applicationContext.getOriginalScopes()));
        } catch (JsonProcessingException ex) {
            logger.error("Error while serializing application attributes.", ex);
            audit.error("Error while serializing application attributes.", ex);
        }
    }

}
