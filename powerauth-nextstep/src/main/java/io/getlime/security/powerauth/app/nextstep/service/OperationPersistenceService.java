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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.getlime.security.powerauth.app.nextstep.repository.AuthenticationRepository;
import io.getlime.security.powerauth.app.nextstep.repository.OperationHistoryRepository;
import io.getlime.security.powerauth.app.nextstep.repository.OperationRepository;
import io.getlime.security.powerauth.app.nextstep.repository.model.entity.AuthenticationEntity;
import io.getlime.security.powerauth.app.nextstep.repository.model.entity.OperationAfsActionEntity;
import io.getlime.security.powerauth.app.nextstep.repository.model.entity.OperationEntity;
import io.getlime.security.powerauth.app.nextstep.repository.model.entity.OperationHistoryEntity;
import io.getlime.security.powerauth.lib.nextstep.model.entity.ApplicationContext;
import io.getlime.security.powerauth.lib.nextstep.model.entity.AuthStep;
import io.getlime.security.powerauth.lib.nextstep.model.entity.OperationFormData;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthMethod;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthResult;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthStepResult;
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

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final IdGeneratorService idGeneratorService;
    private final OperationRepository operationRepository;
    private final OperationHistoryRepository operationHistoryRepository;
    private final MobileTokenConfigurationService mobileTokenConfigurationService;
    private final StepResolutionService stepResolutionService;
    private final AuthenticationRepository authenticationRepository;

    /**
     * Service constructor.
     * @param idGeneratorService              ID generator service.
     * @param operationRepository             Operation repository.
     * @param operationHistoryRepository      Operation history repository.
     * @param mobileTokenConfigurationService Mobile token configuration service.
     * @param stepResolutionService           Step resolution service.
     * @param authenticationRepository        Authentication repository.
     */
    @Autowired
    public OperationPersistenceService(IdGeneratorService idGeneratorService, OperationRepository operationRepository,
                                       OperationHistoryRepository operationHistoryRepository,
                                       MobileTokenConfigurationService mobileTokenConfigurationService,
                                       @Lazy StepResolutionService stepResolutionService, AuthenticationRepository authenticationRepository) {
        this.idGeneratorService = idGeneratorService;
        this.operationRepository = operationRepository;
        this.operationHistoryRepository = operationHistoryRepository;
        this.mobileTokenConfigurationService = mobileTokenConfigurationService;
        this.stepResolutionService = stepResolutionService;
        this.authenticationRepository = authenticationRepository;
    }

    /**
     * Convert a CreateOperationRequest and CreateOperationResponse into OperationEntity and OperationHistoryEntity.
     * Both entities are persisted to store both the operation and its history in the database.
     *
     * @param request  create request received from the client
     * @param response create response generated for the client
     */
    public void createOperation(CreateOperationRequest request, CreateOperationResponse response) {
        OperationEntity operation = new OperationEntity();
        operation.setOperationName(request.getOperationName());
        operation.setOperationData(request.getOperationData());
        operation.setOperationId(response.getOperationId());
        operation.setUserId(request.getUserId());
        operation.setOrganizationId(request.getOrganizationId());
        operation.setOrganizationId(request.getOrganizationId());
        operation.setExternalOperationName(request.getOperationNameExternal());
        operation.setExternalTransactionId(request.getExternalTransactionId());
        operation.setResult(response.getResult());
        if (request.getApplicationContext() != null) {
            // Assign operation context to entity in case it was sent in request
            assignApplicationContext(operation, request.getApplicationContext());
        }
        try {
            // Store form data as serialized JSON string.
            operation.setOperationFormData(objectMapper.writeValueAsString(request.getFormData()));
        } catch (JsonProcessingException ex) {
            logger.error("Error while serializing operation form data", ex);
        }
        operation.setTimestampCreated(response.getTimestampCreated());
        operation.setTimestampExpires(response.getTimestampExpires());
        operationRepository.save(operation);

        OperationHistoryEntity operationHistory = new OperationHistoryEntity(operation.getOperationId(),
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
        }
        operationHistory.setResponseTimestampCreated(response.getTimestampCreated());
        operationHistory.setResponseTimestampExpires(response.getTimestampExpires());
        operationHistoryRepository.save(operationHistory);
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
     */
    public UpdateOperationResponse updateOperation(UpdateOperationRequest request) throws OperationAlreadyFailedException, OperationAlreadyFinishedException, OperationAlreadyCanceledException, AuthMethodNotFoundException, OperationNotFoundException, OperationNotValidException, InvalidConfigurationException, InvalidRequestException {
        // Resolve response based on dynamic step definitions
        UpdateOperationResponse response = stepResolutionService.resolveNextStepResponse(request);

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
     */
    private void updateOperation(UpdateOperationRequest request, UpdateOperationResponse response) throws OperationNotFoundException {
        Optional<OperationEntity> operationOptional = operationRepository.findById(response.getOperationId());
        if (!operationOptional.isPresent()) {
            throw new OperationNotFoundException("Operation not found, operation ID: " + response.getOperationId());
        }
        OperationEntity operation = operationOptional.get();
        operation.setUserId(request.getUserId());
        operation.setOrganizationId(request.getOrganizationId());
        operation.setResult(response.getResult());
        if (request.getApplicationContext() != null) {
            // Do not remove application context in case it was not sent in request
            assignApplicationContext(operation, request.getApplicationContext());
        }
        // operation expiration time matches current response expiration time
        operation.setTimestampExpires(response.getTimestampExpires());
        operationRepository.save(operation);

        OperationHistoryEntity operationHistory = new OperationHistoryEntity(operation.getOperationId(),
                idGeneratorService.generateOperationHistoryId(operation.getOperationId()));
        operationHistory.setRequestAuthMethod(request.getAuthMethod());
        operationHistory.setRequestAuthStepResult(request.getAuthStepResult());
        if (request.getAuthenticationId() != null) {
            Optional<AuthenticationEntity> authenticationOptional = authenticationRepository.findById(request.getAuthenticationId());
            authenticationOptional.ifPresent(operationHistory::setAuthentication);
        }
        operationHistory.setResponseResult(response.getResult());
        operationHistory.setResponseResultDescription(response.getResultDescription());
        try {
            // Params, steps and auth instruments are saved as JSON for now - new entities would be required to store this data.
            // We can add these entities later in case they are needed.
            operationHistory.setRequestAuthInstruments(objectMapper.writeValueAsString(request.getAuthInstruments()));
            operationHistory.setRequestParams(objectMapper.writeValueAsString(request.getParams()));
            operationHistory.setResponseSteps(objectMapper.writeValueAsString(response.getSteps()));
        } catch (JsonProcessingException e) {
            logger.error("Error occurred while serializing operation history", e);
        }
        operationHistory.setResponseTimestampCreated(response.getTimestampCreated());
        operationHistory.setResponseTimestampExpires(response.getTimestampExpires());
        operationHistoryRepository.save(operationHistory);
    }

    /**
     * Update user ID and organization ID for an operation.
     * @param request Update operation user request.
     * @throws OperationNotFoundException Thrown when operation does not exist.
     */
    public void updateOperationUser(UpdateOperationUserRequest request) throws OperationNotFoundException {
        String operationId = request.getOperationId();
        String userId = request.getUserId();
        String organizationId = request.getOrganizationId();
        OperationEntity operation = getOperation(operationId);
        operation.setUserId(userId);
        operation.setOrganizationId(organizationId);
        operation.setUserAccountStatus(request.getAccountStatus());
        operationRepository.save(operation);
    }

    /**
     * Updates form data for given operation.
     *
     * @param request Request to update form data.
     * @throws OperationNotFoundException Thrown when operation does not exist.
     */
    public void updateFormData(UpdateFormDataRequest request) throws OperationNotFoundException {
        Optional<OperationEntity> operationOptional = operationRepository.findById(request.getOperationId());
        if (!operationOptional.isPresent()) {
            throw new OperationNotFoundException("Operation not found, operation ID: " + request.getOperationId());
        }
        OperationEntity operation = operationOptional.get();
        try {
            OperationFormData formData = objectMapper.readValue(operation.getOperationFormData(), OperationFormData.class);
            // update only formData.userInput which should contain all input from the user
            formData.getUserInput().putAll(request.getFormData().getUserInput());
            operation.setOperationFormData(objectMapper.writeValueAsString(formData));
        } catch (IOException e) {
            logger.error("Error occurred while serializing operation form data", e);
        }
        operationRepository.save(operation);
    }

    /**
     * Update chosen authentication method.
     *
     * @param request Request to update chosen authentication method.
     * @throws OperationNotFoundException Thrown when operation does not exist.
     * @throws InvalidConfigurationException Thrown when Next Step configuration is invalid.
     * @throws InvalidRequestException Thrown when request is invalid.
     */
    public void updateChosenAuthMethod(UpdateChosenAuthMethodRequest request) throws OperationNotFoundException, InvalidConfigurationException, InvalidRequestException {
        Optional<OperationEntity> operationOptional = operationRepository.findById(request.getOperationId());
        if (!operationOptional.isPresent()) {
            throw new OperationNotFoundException("Operation not found, operation ID: " + request.getOperationId());
        }
        OperationEntity operation = operationOptional.get();
        OperationHistoryEntity currentHistory = operation.getCurrentOperationHistoryEntity();
        if (currentHistory == null) {
            throw new InvalidConfigurationException("Operation is missing history");
        }
        boolean chosenAuthMethodValid = false;
        for (AuthStep step : getResponseAuthSteps(operation)) {
            if (step.getAuthMethod() == request.getChosenAuthMethod()) {
                chosenAuthMethodValid = true;
                break;
            }
        }
        if (!chosenAuthMethodValid) {
            throw new InvalidRequestException("Invalid chosen authentication method");
        }
        currentHistory.setChosenAuthMethod(request.getChosenAuthMethod());
        operationHistoryRepository.save(currentHistory);
    }

    /**
     * Update mobile token status.
     *
     * @param request Request to update mobile token status.
     * @throws OperationNotFoundException Thrown when operation does not exist.
     */
    public void updateMobileToken(UpdateMobileTokenRequest request) throws OperationNotFoundException, OperationNotValidException {
        Optional<OperationEntity> operationOptional = operationRepository.findById(request.getOperationId());
        if (!operationOptional.isPresent()) {
            throw new OperationNotFoundException("Operation not found, operation ID: " + request.getOperationId());
        }
        OperationEntity operation = operationOptional.get();
        OperationHistoryEntity currentHistory = operation.getCurrentOperationHistoryEntity();
        if (currentHistory == null) {
            throw new OperationNotValidException("Operation is missing history");
        }
        currentHistory.setMobileTokenActive(request.isMobileTokenActive());
        operationHistoryRepository.save(currentHistory);
    }

    /**
     * Update application context.
     *
     * @param request Request to update application context.
     * @throws OperationNotFoundException Thrown when operation does not exist.
     */
    public void updateApplicationContext(UpdateApplicationContextRequest request) throws OperationNotFoundException {
        Optional<OperationEntity> operationOptional = operationRepository.findById(request.getOperationId());
        if (!operationOptional.isPresent()) {
            throw new OperationNotFoundException("Operation not found, operation ID: " + request.getOperationId());
        }
        OperationEntity operation = operationOptional.get();
        ApplicationContext applicationContext = request.getApplicationContext();
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
        }
        operationRepository.save(operation);
    }

    /**
     * Retrieve an OperationEntity for given operationId from database.
     *
     * @param operationId id of an operation
     * @return OperationEntity loaded from database
     * @throws OperationNotFoundException Thrown when operation does not exist.
     */
    public OperationEntity getOperation(String operationId) throws OperationNotFoundException {
        Optional<OperationEntity> operationOptional = operationRepository.findById(operationId);
        if (!operationOptional.isPresent()) {
            throw new OperationNotFoundException("Operation not found, operation ID: " + operationId);
        }
        return operationOptional.get();
    }

    /**
     * Return whether operation exists.
     *
     * @param operationId Operation ID.
     * @return Whether operation exists.
     */
    public boolean operationExists(String operationId) {
        Optional<OperationEntity> operationOptional = operationRepository.findById(operationId);
        return operationOptional.isPresent();
    }

    /**
     * Retrieve list of pending operations for given user ID from database.
     *
     * @param userId User ID.
     * @param mobileTokenOnly Whether pending operation list should be filtered for only next step with mobile token support.
     * @return List of operations which match the query.
     * @throws InvalidConfigurationException Thrown when Next Step configuration is invalid.
     */
    public List<OperationEntity> getPendingOperations(String userId, boolean mobileTokenOnly) throws InvalidConfigurationException {
        List<OperationEntity> entities = operationRepository.findPendingOperationsForUser(userId);
        if (!mobileTokenOnly) {
            // Return all unfinished operations for user
            return entities;
        }
        List<OperationEntity> filteredList = new ArrayList<>();
        for (OperationEntity operation : entities) {
            // Add operations whose last step is CONFIRMED with CONTINUE result and chosen authentication method supports mobile token
            OperationHistoryEntity currentHistoryEntity = operation.getCurrentOperationHistoryEntity();
            if (currentHistoryEntity != null && currentHistoryEntity.getRequestAuthStepResult() == AuthStepResult.CONFIRMED
                    && currentHistoryEntity.getResponseResult() == AuthResult.CONTINUE && currentHistoryEntity.isMobileTokenActive()) {
                AuthMethod chosenAuthMethod = currentHistoryEntity.getChosenAuthMethod();
                if (mobileTokenConfigurationService.isMobileTokenEnabled(userId, operation.getOperationName(), chosenAuthMethod)) {
                    filteredList.add(operation);
                }
            }
        }
        return filteredList;
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
        List<AuthStep> steps = new ArrayList<>();
        if (operation == null) {
            return steps;
        }
        OperationHistoryEntity currentHistory = operation.getCurrentOperationHistoryEntity();
        if (currentHistory == null) {
            return steps;
        }
        // get steps from the current response
        String responseSteps = currentHistory.getResponseSteps();
        if (responseSteps == null) {
            return steps;
        }
        try {
            steps.addAll(objectMapper.readValue(responseSteps, new TypeReference<List<AuthStep>>() {}));
            return steps;
        } catch (IOException e) {
            // in case of an error empty list is returned
            logger.error("Error occurred while deserializing response steps", e);
        }
        return steps;
    }

    /**
     * Create an AFS action.
     * @param request Request to crete an AFS action.
     */
    public void createAfsAction(CreateAfsActionRequest request) {
        try {
            OperationAfsActionEntity afsEntity = new OperationAfsActionEntity();
            OperationEntity operation = getOperation(request.getOperationId());
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
        } catch (OperationNotFoundException e) {
            logger.error("AFS action could not be saved because operation does not exist: {}", request.getOperationId());
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
        }
    }

}
