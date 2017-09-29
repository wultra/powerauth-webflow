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
package io.getlime.security.powerauth.app.nextstep.service;

import io.getlime.security.powerauth.app.nextstep.configuration.NextStepServerConfiguration;
import io.getlime.security.powerauth.app.nextstep.repository.AuthMethodRepository;
import io.getlime.security.powerauth.app.nextstep.repository.StepDefinitionRepository;
import io.getlime.security.powerauth.app.nextstep.repository.model.entity.AuthMethodEntity;
import io.getlime.security.powerauth.app.nextstep.repository.model.entity.OperationEntity;
import io.getlime.security.powerauth.app.nextstep.repository.model.entity.OperationHistoryEntity;
import io.getlime.security.powerauth.app.nextstep.repository.model.entity.StepDefinitionEntity;
import io.getlime.security.powerauth.lib.nextstep.model.entity.AuthMethodDetail;
import io.getlime.security.powerauth.lib.nextstep.model.entity.AuthStep;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthMethod;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthResult;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthStepResult;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.OperationRequestType;
import io.getlime.security.powerauth.lib.nextstep.model.request.CreateOperationRequest;
import io.getlime.security.powerauth.lib.nextstep.model.request.UpdateOperationRequest;
import io.getlime.security.powerauth.lib.nextstep.model.response.CreateOperationResponse;
import io.getlime.security.powerauth.lib.nextstep.model.response.UpdateOperationResponse;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * This service performs dynamic resolution of the next steps. Step definitions are loaded during class initialization
 * and are used to generate responses for incoming requests. Step definitions are filtered by request parameters
 * and matching step definitions are returned as the list of next steps (including priorities in case more step
 * definitions match the request). Step definitions are also filtered by authentication methods available for the user,
 * authentication methods can be enabled or disabled dynamically in user preferences.
 *
 * @author Roman Strobl
 */
@Service
public class StepResolutionService {

    private IdGeneratorService idGeneratorService;
    private OperationPersistenceService operationPersistenceService;
    private NextStepServerConfiguration nextStepServerConfiguration;
    private AuthMethodService authMethodService;
    private AuthMethodRepository authMethodRepository;
    private Map<String, List<StepDefinitionEntity>> stepDefinitionsPerOperation;

    @Autowired
    public StepResolutionService(StepDefinitionRepository stepDefinitionRepository, OperationPersistenceService operationPersistenceService,
                                 IdGeneratorService idGeneratorService, NextStepServerConfiguration nextStepServerConfiguration,
                                 AuthMethodService authMethodService, AuthMethodRepository authMethodRepository) {
        this.operationPersistenceService = operationPersistenceService;
        this.idGeneratorService = idGeneratorService;
        this.nextStepServerConfiguration = nextStepServerConfiguration;
        this.authMethodService = authMethodService;
        this.authMethodRepository = authMethodRepository;
        stepDefinitionsPerOperation = new HashMap<>();
        List<String> operationNames = stepDefinitionRepository.findDistinctOperationNames();
        for (String operationName : operationNames) {
            stepDefinitionsPerOperation.put(operationName, stepDefinitionRepository.findStepDefinitionsForOperation(operationName));
        }
    }

    /**
     * Resolves the next steps for given CreateOperationRequest.
     *
     * @param request request to create a new operation
     * @return response with ordered list of next steps
     */
    public CreateOperationResponse resolveNextStepResponse(CreateOperationRequest request) {
        CreateOperationResponse response = new CreateOperationResponse();
        if (request.getOperationId() != null && !request.getOperationId().isEmpty()) {
            // operation ID received from the client, verify that it is available
            if (operationPersistenceService.getOperation(request.getOperationId()) != null) {
                throw new IllegalArgumentException("Operation could not be created, operation ID is already used: " + request.getOperationId());
            }
            response.setOperationId(request.getOperationId());
        } else {
            // set auto-generated operation ID
            response.setOperationId(idGeneratorService.generateOperationId());
        }
        response.setOperationName(request.getOperationName());
        // AuthStepResult and AuthMethod are not available when creating the operation, null values are used to ignore them
        List<StepDefinitionEntity> stepDefinitions = filterSteps(request.getOperationName(), OperationRequestType.CREATE, null, null, null);
        response.getSteps().addAll(prepareAuthSteps(stepDefinitions));
        response.setTimestampCreated(new Date());
        response.setTimestampExpires(new DateTime().plusSeconds(nextStepServerConfiguration.getOperationExpirationTime()).toDate());
        response.setFormData(request.getFormData());
        Set<AuthResult> allResults = new HashSet<>();
        for (StepDefinitionEntity stepDef : stepDefinitions) {
            allResults.add(stepDef.getResponseResult());
        }
        if (allResults.size() == 1) {
            response.setResult(allResults.iterator().next());
            return response;
        }
        throw new IllegalStateException("Next step could not be resolved for new operation.");
    }

    /**
     * Resolves the next steps for given UpdateOperationRequest.
     *
     * @param request request to update an existing operation
     * @return response with ordered list of next steps
     */
    public UpdateOperationResponse resolveNextStepResponse(UpdateOperationRequest request) {
        OperationEntity operation = operationPersistenceService.getOperation(request.getOperationId());
        checkLegitimityOfUpdate(operation, request);
        UpdateOperationResponse response = new UpdateOperationResponse();
        response.setOperationId(request.getOperationId());
        response.setOperationName(operation.getOperationName());
        response.setUserId(request.getUserId());
        response.setTimestampCreated(new Date());
        if (request.getAuthStepResult() == AuthStepResult.CANCELED) {
            // User canceled the operation. Save authStepResultDescription which contains the reason for cancellation.
            // The next step is still resolved according to the dynamic rules.
            response.setResultDescription("canceled." + request.getAuthStepResultDescription().toLowerCase());
        }
        if (operation.isExpired()) {
            // Operation fails in case it is expired.
            // Response expiration time matches operation expiration to avoid extending expiration time of the operation.
            response.setTimestampExpires(operation.getTimestampExpires());
            response.setResult(AuthResult.FAILED);
            response.setResultDescription("operation.timeout");
            return response;
        }
        response.setTimestampExpires(new DateTime().plusSeconds(nextStepServerConfiguration.getOperationExpirationTime()).toDate());
        AuthStepResult authStepResult = request.getAuthStepResult();
        if (isAuthMethodFailed(operation, request.getAuthMethod(), authStepResult)) {
            // check whether the authentication method has already failed completely, in case it has failed, update the authStepResult
            request.setAuthStepResult(AuthStepResult.AUTH_METHOD_FAILED);
        }

        List<StepDefinitionEntity> stepDefinitions = filterSteps(operation.getOperationName(), OperationRequestType.UPDATE, request.getAuthStepResult(), request.getAuthMethod(), request.getUserId());
        sortSteps(stepDefinitions);
        verifyDuplicatePrioritiesAbsent(stepDefinitions);
        Set<AuthResult> allResults = new HashSet<>();
        for (StepDefinitionEntity stepDef : stepDefinitions) {
            allResults.add(stepDef.getResponseResult());
        }
        if (allResults.size() == 1) {
            // Straightforward response - only one AuthResult found. Return all matching steps.
            response.getSteps().addAll(prepareAuthSteps(stepDefinitions));
            response.setResult(allResults.iterator().next());
            return response;
        } else if (allResults.size() > 1) {
            // We need to make sure a specific AuthResult is returned in case multiple authentication methods lead
            // to different AuthResults.
            // In case there is any DONE or CONTINUE next step, prefer it over FAILED. FAILED state can be caused by a failing
            // authentication method or by method canceled by the user, in this case try to switch to other
            // authentication method if it is available.
            Map<AuthResult, List<StepDefinitionEntity>> stepsByAuthResult = stepDefinitions
                    .stream()
                    .collect(Collectors.groupingBy(StepDefinitionEntity::getResponseResult));
            if (stepsByAuthResult.containsKey(AuthResult.DONE)) {
                List<StepDefinitionEntity> doneSteps = stepsByAuthResult.get(AuthResult.DONE);
                response.getSteps().addAll(prepareAuthSteps(doneSteps));
                response.setResult(AuthResult.DONE);
                return response;
            } else if (stepsByAuthResult.containsKey(AuthResult.CONTINUE)) {
                List<StepDefinitionEntity> continueSteps = stepsByAuthResult.get(AuthResult.CONTINUE);
                response.getSteps().addAll(prepareAuthSteps(continueSteps));
                response.setResult(AuthResult.CONTINUE);
                return response;
            } else if (stepsByAuthResult.containsKey(AuthResult.FAILED)) {
                List<StepDefinitionEntity> failedSteps = stepsByAuthResult.get(AuthResult.FAILED);
                response.getSteps().addAll(prepareAuthSteps(failedSteps));
                response.setResult(AuthResult.FAILED);
                return response;
            }
            // This code should not be reached - all cases should have been handled previously.
            response.getSteps().clear();
            response.setResult(AuthResult.FAILED);
            response.setResultDescription("error.unknown");
            return response;
        } else {
            // No step definition matches the current criteria. Suitable step definitions might have been filtered out
            // via user preferences. Fail the operation.
            response.getSteps().clear();
            response.setResult(AuthResult.FAILED);
            response.setResultDescription("error.noAuthMethod");
            return response;
        }
    }

    /**
     * Filters step definitions by given parameters and returns a list of step definitions which match the query.
     *
     * @param operationName name of the operation
     * @param operationType type of the operation - CREATE/UPDATE
     * @param authStepResult result of previous authentication step
     * @param authMethod authentication method of previous authentication step
     * @param userId user ID
     * @return filtered list of steps
     */
    private List<StepDefinitionEntity> filterSteps(String operationName, OperationRequestType operationType, AuthStepResult authStepResult, AuthMethod authMethod, String userId) {
        List<StepDefinitionEntity> stepDefinitions = stepDefinitionsPerOperation.get(operationName);
        List<AuthMethod> authMethodsAvailableForUser = new ArrayList<>();
        if (userId != null) {
            for (AuthMethodDetail authMethodDetail : authMethodService.listAuthMethodsEnabledForUser(userId)) {
                authMethodsAvailableForUser.add(authMethodDetail.getAuthMethod());
            }
        }
        List<StepDefinitionEntity> filteredStepDefinitions = new ArrayList<>();
        if (stepDefinitions == null) {
            throw new IllegalStateException("Step definitions are missing in Next Step server.");
        }
        for (StepDefinitionEntity stepDef : stepDefinitions) {
            if (operationType != null && !operationType.equals(stepDef.getOperationType())) {
                // filter by operation type
                continue;
            }
            if (authStepResult != null && !authStepResult.equals(stepDef.getRequestAuthStepResult())) {
                // filter by AuthStepResult
                continue;
            }
            if (authMethod != null && !authMethod.equals(stepDef.getRequestAuthMethod())) {
                // filter by request AuthMethod
                continue;
            }
            if (userId != null && stepDef.getResponseAuthMethod() != null && !authMethodsAvailableForUser.contains(stepDef.getResponseAuthMethod())) {
                // filter by response AuthMethod based on methods available for the user - the list can change
                // dynamically via user preferences
                continue;
            }
            filteredStepDefinitions.add(stepDef);
        }
        return filteredStepDefinitions;
    }

    /**
     * Sorts the step definitions based on their priorities.
     *
     * @param stepDefinitions step definitions
     */
    private void sortSteps(List<StepDefinitionEntity> stepDefinitions) {
        Collections.sort(stepDefinitions, Comparator.comparing(StepDefinitionEntity::getResponsePriority));
    }

    /**
     * Verifies that each priority is present only once in the list of step definitions.
     *
     * @param stepDefinitions step definitions
     */
    private void verifyDuplicatePrioritiesAbsent(List<StepDefinitionEntity> stepDefinitions) {
        Map<Long, List<StepDefinitionEntity>> stepsByPriority = stepDefinitions
                .stream()
                .collect(Collectors.groupingBy(StepDefinitionEntity::getResponsePriority));
        if (stepsByPriority.size() != stepDefinitions.size()) {
            throw new IllegalStateException("Multiple steps with the same priority detected while resolving next step.");
        }
    }

    /**
     * Converts List<StepDefinitionEntity> into a List<AuthStep>.
     *
     * @param stepDefinitions step definitions to convert
     * @return converted list of authentication steps
     */
    private List<AuthStep> prepareAuthSteps(List<StepDefinitionEntity> stepDefinitions) {
        List<AuthStep> authSteps = new ArrayList<>();
        for (StepDefinitionEntity stepDef : stepDefinitions) {
            if (stepDef.getResponseAuthMethod() != null) {
                AuthStep authStep = new AuthStep();
                authStep.setAuthMethod(stepDef.getResponseAuthMethod());
                authSteps.add(authStep);
            }
        }
        return authSteps;
    }

    /**
     * Check whether given authentication method has previously failed or it has exceeded the maximum number of attempts.
     *
     * @param operation  operation whose history is being checked
     * @param authMethod authentication method
     * @return whether authentication method failed
     */
    private boolean isAuthMethodFailed(OperationEntity operation, AuthMethod authMethod, AuthStepResult currentAuthStepResult) {
        if (currentAuthStepResult == AuthStepResult.AUTH_METHOD_FAILED) {
            return true;
        }
        // in case authentication method previously failed, it is already failed
        for (OperationHistoryEntity history : operation.getOperationHistory()) {
            if (history.getRequestAuthMethod() == authMethod && history.getRequestAuthStepResult() == AuthStepResult.AUTH_METHOD_FAILED) {
                return true;
            }
        }
        // check whether authMethod supports check of authorization failure count
        AuthMethodEntity authMethodEntity = authMethodRepository.findByAuthMethod(authMethod);
        if (authMethodEntity == null) {
            throw new IllegalStateException("AuthMethod is missing in database: " + authMethod);
        }
        if (authMethodEntity.getCheckAuthorizationFailures()) {
            // count failures
            int failureCount = 0;
            if (currentAuthStepResult == AuthStepResult.AUTH_FAILED) {
                // add current failure
                failureCount++;
            }
            for (OperationHistoryEntity history : operation.getOperationHistory()) {
                // add all failures from history for this method
                if (history.getRequestAuthMethod() == authMethod && history.getRequestAuthStepResult() == AuthStepResult.AUTH_FAILED) {
                    failureCount++;
                }
            }
            if (failureCount >= authMethodEntity.getMaxAuthorizationFailures()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check whether the update of operation is legitimate and meaningful.
     *
     * @param operationEntity Operation entity.
     * @param request         Update request.
     */
    private void checkLegitimityOfUpdate(OperationEntity operationEntity, UpdateOperationRequest request) {
        if (request == null || request.getOperationId() == null) {
            throw new IllegalArgumentException("Operation update failed, because request is invalid.");
        }
        if (operationEntity == null) {
            throw new IllegalArgumentException("Operation update failed, because operation does not exist (operationId: " + request.getOperationId() + ").");
        }
        if (request.getAuthMethod() == null) {
            throw new IllegalArgumentException("Operation update failed, because authentication method is missing (operationId: " + request.getOperationId() + ").");
        }
        if (request.getAuthMethod() == AuthMethod.INIT) {
            throw new IllegalArgumentException("Operation update failed, because INIT method cannot be updated (operationId: " + request.getOperationId() + ").");
        }
        if (request.getAuthStepResult() == null) {
            throw new IllegalArgumentException("Operation update failed, because result of authentication step is missing (operationId: " + request.getOperationId() + ").");
        }
        List<OperationHistoryEntity> operationHistory = operationEntity.getOperationHistory();
        if (operationHistory.isEmpty()) {
            throw new IllegalStateException("Operation update failed, because operation is missing its history (operationId: " + request.getOperationId() + ").");
        }
        OperationHistoryEntity initOperationItem = operationHistory.get(0);
        if (initOperationItem.getRequestAuthMethod() != null || initOperationItem.getRequestAuthStepResult() != null) {
            throw new IllegalStateException("Operation update failed, because INIT step for this operation is invalid (operationId: " + request.getOperationId() + ").");
        }
        for (OperationHistoryEntity historyItem : operationHistory) {
            if (historyItem.getResponseResult() == AuthResult.DONE) {
                throw new IllegalStateException("Operation update failed, because operation is already in DONE state (operationId: " + request.getOperationId() + ").");
            }
            if (historyItem.getResponseResult() == AuthResult.FAILED) {
                throw new IllegalStateException("Operation update failed, because operation is already in FAILED state (operationId: " + request.getOperationId() + ").");
            }
        }
    }

}
