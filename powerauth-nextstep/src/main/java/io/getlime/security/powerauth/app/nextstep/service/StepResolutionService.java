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

import io.getlime.security.powerauth.app.nextstep.configuration.NextStepServerConfiguration;
import io.getlime.security.powerauth.app.nextstep.converter.OperationConverter;
import io.getlime.security.powerauth.app.nextstep.repository.AuthMethodRepository;
import io.getlime.security.powerauth.app.nextstep.repository.StepDefinitionRepository;
import io.getlime.security.powerauth.app.nextstep.repository.model.entity.AuthMethodEntity;
import io.getlime.security.powerauth.app.nextstep.repository.model.entity.OperationEntity;
import io.getlime.security.powerauth.app.nextstep.repository.model.entity.OperationHistoryEntity;
import io.getlime.security.powerauth.app.nextstep.repository.model.entity.StepDefinitionEntity;
import io.getlime.security.powerauth.lib.nextstep.model.entity.AuthStep;
import io.getlime.security.powerauth.lib.nextstep.model.entity.UserAuthMethodDetail;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthMethod;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthResult;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthStepResult;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.OperationRequestType;
import io.getlime.security.powerauth.lib.nextstep.model.exception.*;
import io.getlime.security.powerauth.lib.nextstep.model.request.CreateOperationRequest;
import io.getlime.security.powerauth.lib.nextstep.model.request.UpdateOperationRequest;
import io.getlime.security.powerauth.lib.nextstep.model.response.CreateOperationResponse;
import io.getlime.security.powerauth.lib.nextstep.model.response.UpdateOperationResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * This service performs dynamic resolution of the next steps. Step definitions are loaded during class initialization
 * and are used to generate responses for incoming requests. Step definitions are filtered by request parameters
 * and matching step definitions are returned as the list of next steps (including priorities in case more step
 * definitions match the request). Step definitions are also filtered by authentication methods available for the user,
 * authentication methods can be enabled or disabled dynamically in user preferences.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Service
public class StepResolutionService {

    private final IdGeneratorService idGeneratorService;
    private final OperationPersistenceService operationPersistenceService;
    private final NextStepServerConfiguration nextStepServerConfiguration;
    private final AuthMethodService authMethodService;
    private final AuthMethodRepository authMethodRepository;
    private final MobileTokenConfigurationService mobileTokenConfigurationService;
    private final Map<String, List<StepDefinitionEntity>> stepDefinitionsPerOperation;

    private final OperationConverter operationConverter = new OperationConverter();

    /**
     * Service constructor.
     * @param stepDefinitionRepository Step definition repository.
     * @param operationPersistenceService Operation persistence service.
     * @param idGeneratorService ID generator service.
     * @param nextStepServerConfiguration Next step server configuration.
     * @param authMethodService Authentication method service.
     * @param authMethodRepository Authentication method repository.
     * @param mobileTokenConfigurationService Mobile token configuration service.
     */
    @Autowired
    public StepResolutionService(StepDefinitionRepository stepDefinitionRepository, OperationPersistenceService operationPersistenceService,
                                 IdGeneratorService idGeneratorService, NextStepServerConfiguration nextStepServerConfiguration,
                                 AuthMethodService authMethodService, AuthMethodRepository authMethodRepository, MobileTokenConfigurationService mobileTokenConfigurationService) {
        this.operationPersistenceService = operationPersistenceService;
        this.idGeneratorService = idGeneratorService;
        this.nextStepServerConfiguration = nextStepServerConfiguration;
        this.authMethodService = authMethodService;
        this.authMethodRepository = authMethodRepository;
        this.mobileTokenConfigurationService = mobileTokenConfigurationService;
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
     * @throws OperationAlreadyExistsException Thrown when operation already exists.
     * @throws InvalidConfigurationException Thrown when Next Step configuration is invalid.
     */
    public CreateOperationResponse resolveNextStepResponse(CreateOperationRequest request) throws OperationAlreadyExistsException, InvalidConfigurationException {
        CreateOperationResponse response = new CreateOperationResponse();
        if (request.getOperationId() != null && !request.getOperationId().isEmpty()) {
            // operation ID received from the client, verify that it is available
            if (operationPersistenceService.operationExists(request.getOperationId())) {
                throw new OperationAlreadyExistsException("Operation could not be created, operation ID is already used: " + request.getOperationId());
            }
            response.setOperationId(request.getOperationId());
        } else {
            // set auto-generated operation ID
            response.setOperationId(idGeneratorService.generateOperationId());
        }
        response.setOperationName(request.getOperationName());
        response.setOrganizationId(request.getOrganizationId());
        response.setExternalTransactionId(request.getExternalTransactionId());
        // AuthStepResult and AuthMethod are not available when creating the operation, null values are used to ignore them
        List<StepDefinitionEntity> stepDefinitions = filterStepDefinitions(request.getOperationName(), OperationRequestType.CREATE, null, null, null);
        response.getSteps().addAll(filterAuthSteps(stepDefinitions, null, request.getOperationName()));
        response.setTimestampCreated(new Date());
        ZonedDateTime timestampExpires = ZonedDateTime.now().plusSeconds(nextStepServerConfiguration.getOperationExpirationTime());
        response.setTimestampExpires(Date.from(timestampExpires.toInstant()));
        response.setFormData(request.getFormData());
        Set<AuthResult> allResults = new HashSet<>();
        for (StepDefinitionEntity stepDef : stepDefinitions) {
            allResults.add(stepDef.getResponseResult());
        }
        if (allResults.size() == 1) {
            response.setResult(allResults.iterator().next());
            return response;
        }
        throw new InvalidConfigurationException("Next step could not be resolved for new operation.");
    }

    /**
     * Resolves the next steps for given UpdateOperationRequest.
     *
     * @param request Request to update an existing operation.
     * @return Response with ordered list of next steps.
     * @throws OperationNotFoundException Thrown when operation is not found.
     * @throws OperationAlreadyFinishedException Thrown when operation is already finished.
     * @throws OperationAlreadyCanceledException Thrown when operation is already canceled.
     * @throws OperationAlreadyFailedException Thrown when operation is already failed.
     * @throws AuthMethodNotFoundException Thrown when authentication method is not found.
     * @throws InvalidRequestException Thrown when request is not valid.
     * @throws OperationNotValidException Thrown when operation is not valid.
     * @throws InvalidConfigurationException Thrown when Next Step configuration is not valid.
     */
    public UpdateOperationResponse resolveNextStepResponse(UpdateOperationRequest request) throws OperationNotFoundException, OperationAlreadyFailedException, OperationAlreadyFinishedException, OperationAlreadyCanceledException, AuthMethodNotFoundException, InvalidRequestException, OperationNotValidException, InvalidConfigurationException {
        OperationEntity operation = operationPersistenceService.getOperation(request.getOperationId());
        checkLegitimityOfUpdate(operation, request);
        UpdateOperationResponse response = new UpdateOperationResponse();
        response.setOperationId(request.getOperationId());
        response.setOperationName(operation.getOperationName());
        response.setUserId(request.getUserId());
        response.setOrganizationId(request.getOrganizationId());
        response.setExternalTransactionId(operation.getExternalTransactionId());
        // attach operation data and form data to the response
        response.setOperationData(operation.getOperationData());
        response.setFormData(operationConverter.fromEntity(operation).getFormData());
        response.setTimestampCreated(new Date());
        if (request.getAuthStepResult() == AuthStepResult.CANCELED) {
            // User canceled the operation. Save authStepResultDescription which contains the reason for cancellation.
            // The next step is still resolved according to the dynamic rules.
            response.setResultDescription("canceled." + request.getAuthStepResultDescription().toLowerCase());
        }
        if (operation.isExpired()) {
            // Operation fails in case it is expired.
            // Fail authentication method.
            request.setAuthStepResult(AuthStepResult.AUTH_METHOD_FAILED);
            // Response expiration time matches operation expiration to avoid extending expiration time of the operation.
            response.setTimestampExpires(operation.getTimestampExpires());
            response.setResult(AuthResult.FAILED);
            response.setResultDescription("operation.timeout");
            return response;
        }
        ZonedDateTime timestampExpires = ZonedDateTime.now().plusSeconds(nextStepServerConfiguration.getOperationExpirationTime());
        response.setTimestampExpires(Date.from(timestampExpires.toInstant()));
        AuthStepResult authStepResult = request.getAuthStepResult();
        if (isAuthMethodFailed(operation, request.getAuthMethod(), authStepResult)) {
            // check whether the authentication method has already failed completely, in case it has failed, update the authStepResult
            request.setAuthStepResult(AuthStepResult.AUTH_METHOD_FAILED);
        }

        List<StepDefinitionEntity> stepDefinitions = filterStepDefinitions(operation.getOperationName(), OperationRequestType.UPDATE, request.getAuthStepResult(), request.getAuthMethod(), request.getUserId());
        sortSteps(stepDefinitions);
        verifyDuplicatePrioritiesAbsent(stepDefinitions);
        Set<AuthResult> allResults = new HashSet<>();
        for (StepDefinitionEntity stepDef : stepDefinitions) {
            allResults.add(stepDef.getResponseResult());
        }
        if (allResults.size() == 1) {
            // Straightforward response - only one AuthResult found. Return all matching steps.
            response.getSteps().addAll(filterAuthSteps(stepDefinitions, response.getUserId(), response.getOperationName()));
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
                response.getSteps().addAll(filterAuthSteps(doneSteps, response.getUserId(), response.getOperationName()));
                response.setResult(AuthResult.DONE);
                return response;
            } else if (stepsByAuthResult.containsKey(AuthResult.CONTINUE)) {
                List<StepDefinitionEntity> continueSteps = stepsByAuthResult.get(AuthResult.CONTINUE);
                response.getSteps().addAll(filterAuthSteps(continueSteps, response.getUserId(), response.getOperationName()));
                response.setResult(AuthResult.CONTINUE);
                return response;
            } else if (stepsByAuthResult.containsKey(AuthResult.FAILED)) {
                List<StepDefinitionEntity> failedSteps = stepsByAuthResult.get(AuthResult.FAILED);
                response.getSteps().addAll(filterAuthSteps(failedSteps, response.getUserId(), response.getOperationName()));
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
     * @throws InvalidConfigurationException Thrown when Next Step configuration is invalid.
     */
    private List<StepDefinitionEntity> filterStepDefinitions(String operationName, OperationRequestType operationType, AuthStepResult authStepResult, AuthMethod authMethod, String userId) throws InvalidConfigurationException {
        List<StepDefinitionEntity> stepDefinitions = stepDefinitionsPerOperation.get(operationName);
        List<AuthMethod> authMethodsAvailableForUser = new ArrayList<>();
        if (userId != null) {
            for (UserAuthMethodDetail userAuthMethodDetail : authMethodService.listAuthMethodsEnabledForUser(userId)) {
                authMethodsAvailableForUser.add(userAuthMethodDetail.getAuthMethod());
            }
        }
        List<StepDefinitionEntity> filteredStepDefinitions = new ArrayList<>();
        if (stepDefinitions == null) {
            throw new InvalidConfigurationException("Step definitions are missing in Next Step server.");
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
     * @throws InvalidConfigurationException Thrown when Next Step configuration is invalid.
     */
    private void verifyDuplicatePrioritiesAbsent(List<StepDefinitionEntity> stepDefinitions) throws InvalidConfigurationException {
        Map<Long, List<StepDefinitionEntity>> stepsByPriority = stepDefinitions
                .stream()
                .collect(Collectors.groupingBy(StepDefinitionEntity::getResponsePriority));
        if (stepsByPriority.size() != stepDefinitions.size()) {
            throw new InvalidConfigurationException("Multiple steps with the same priority detected while resolving next step.");
        }
    }

    /**
     * Converts List<StepDefinitionEntity> into a List<AuthStep> and filters out POWERAUTH_TOKEN method in case it is not available.
     *
     * @param stepDefinitions Step definitions to convert and filter.
     * @return Final list of authentication steps.
     * @throws InvalidConfigurationException Thrown when Next Step configuration is invalid.
     */
    private List<AuthStep> filterAuthSteps(List<StepDefinitionEntity> stepDefinitions, String userId, String operationName) throws InvalidConfigurationException {
        List<AuthStep> authSteps = new ArrayList<>();
        for (StepDefinitionEntity stepDef : stepDefinitions) {
            if (stepDef.getResponseAuthMethod() != null) {
                // filter out POWERAUTH_TOKEN method in case it is not enabled for given operation and authentication method
                if (stepDef.getResponseAuthMethod() == AuthMethod.POWERAUTH_TOKEN
                        && !mobileTokenConfigurationService.isMobileTokenEnabled(userId, operationName, AuthMethod.POWERAUTH_TOKEN)) {
                    continue;
                }
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
     * @throws AuthMethodNotFoundException Thrown when authentication method is not found.
     */
    private boolean isAuthMethodFailed(OperationEntity operation, AuthMethod authMethod, AuthStepResult currentAuthStepResult) throws AuthMethodNotFoundException {
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
        Optional<AuthMethodEntity> authMethodEntityOptional = authMethodRepository.findByAuthMethod(authMethod);
        if (!authMethodEntityOptional.isPresent()) {
            throw new AuthMethodNotFoundException("Authentication method not found: " + authMethod);
        }
        AuthMethodEntity authMethodEntity = authMethodEntityOptional.get();
        if (authMethodEntity.getCheckAuthFails()) {
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
            return failureCount >= authMethodEntity.getMaxAuthFails();
        }
        return false;
    }

    /**
     * Get number of remaining authentication attempts for current authentication method.
     * @param operation Operation.
     * @return Number of remaining authentication attempts. Null value returned for no limit.
     */
    public Long getNumberOfRemainingAttempts(OperationEntity operation) {
        OperationHistoryEntity currentOperationHistory = operation.getCurrentOperationHistoryEntity();
        if (currentOperationHistory == null) {
            return null;
        }
        AuthMethod authMethod = currentOperationHistory.getRequestAuthMethod();
        // in case authentication method previously failed, it is already failed
        for (OperationHistoryEntity history : operation.getOperationHistory()) {
            if (history.getRequestAuthMethod() == authMethod && history.getRequestAuthStepResult() == AuthStepResult.AUTH_METHOD_FAILED) {
                return null;
            }
        }
        // check whether authMethod supports check of authorization failure count
        Optional<AuthMethodEntity> authMethodEntityOptional = authMethodRepository.findByAuthMethod(authMethod);
        if (!authMethodEntityOptional.isPresent()) {
            return null;
        }
        AuthMethodEntity authMethodEntity = authMethodEntityOptional.get();
        if (authMethodEntity.getCheckAuthFails()) {
            // count failures
            long failureCount = 0L;
            for (OperationHistoryEntity history : operation.getOperationHistory()) {
                // add all failures from history for this method
                if (history.getRequestAuthMethod() == authMethod && history.getRequestAuthStepResult() == AuthStepResult.AUTH_FAILED) {
                    failureCount++;
                }
            }
            if (failureCount >= authMethodEntity.getMaxAuthFails()) {
                return 0L;
            }
            return authMethodEntity.getMaxAuthFails() - failureCount;
        }
        return null;
    }

    /**
     * Check whether the update of operation is legitimate and meaningful.
     *
     * @param operationEntity Operation entity.
     * @param request Update request.
     * @throws OperationAlreadyFinishedException Thrown when operation is already finished.
     * @throws OperationAlreadyCanceledException Thrown when operation is already canceled.
     * @throws OperationAlreadyFailedException Thrown when operation is already failed.
     * @throws OperationNotValidException Thrown when operation is not valid.
     * @throws OperationNotFoundException Thrown when operation is not found.
     * @throws InvalidRequestException Thrown when request is invalid.
     */
    private void checkLegitimityOfUpdate(OperationEntity operationEntity, UpdateOperationRequest request) throws OperationAlreadyFinishedException, OperationAlreadyCanceledException, OperationAlreadyFailedException, OperationNotValidException, InvalidRequestException, OperationNotFoundException {
        if (request == null || request.getOperationId() == null) {
            throw new InvalidRequestException("Operation update failed, because request is invalid.");
        }
        if (operationEntity == null) {
            throw new OperationNotFoundException("Operation update failed, because operation does not exist (operationId: " + request.getOperationId() + ").");
        }
        if (request.getAuthMethod() == null) {
            throw new InvalidRequestException("Operation update failed, because authentication method is missing (operationId: " + request.getOperationId() + ").");
        }
        // INIT method can cancel other operations due to concurrency check
        if (request.getAuthStepResult() != AuthStepResult.CANCELED && request.getAuthMethod() == AuthMethod.INIT) {
            throw new InvalidRequestException("Operation update failed, because INIT method cannot be updated (operationId: " + request.getOperationId() + ").");
        }
        if (request.getAuthStepResult() == null) {
            throw new InvalidRequestException("Operation update failed, because result of authentication step is missing (operationId: " + request.getOperationId() + ").");
        }
        List<OperationHistoryEntity> operationHistory = operationEntity.getOperationHistory();
        if (operationHistory.isEmpty()) {
            throw new OperationNotValidException("Operation update failed, because operation is missing its history (operationId: " + request.getOperationId() + ").");
        }
        OperationHistoryEntity initOperationItem = operationHistory.get(0);
        if (initOperationItem.getRequestAuthMethod() != AuthMethod.INIT || initOperationItem.getRequestAuthStepResult() != AuthStepResult.CONFIRMED) {
            throw new OperationNotValidException("Operation update failed, because INIT step for this operation is invalid (operationId: " + request.getOperationId() + ").");
        }
        OperationHistoryEntity currentOperationHistory = operationEntity.getCurrentOperationHistoryEntity();
        // operation can be canceled anytime (e.g. by closed Web Socket) - do not check for step continuation
        if (currentOperationHistory != null && currentOperationHistory.getResponseResult() == AuthResult.CONTINUE
                && request.getAuthStepResult() != AuthStepResult.CANCELED ) {
            boolean stepAuthMethodValid = false;
            // check whether request AuthMethod is available in response AuthSteps - this verifies operation continuity
            if (request.getAuthMethod() == AuthMethod.SHOW_OPERATION_DETAIL) {
                // special handling for SHOW_OPERATION_DETAIL - either SMS_KEY or POWERAUTH_TOKEN are present in next steps
                for (AuthStep step : operationPersistenceService.getResponseAuthSteps(operationEntity)) {
                    if (step.getAuthMethod() == AuthMethod.SMS_KEY || step.getAuthMethod() == AuthMethod.POWERAUTH_TOKEN) {
                        stepAuthMethodValid = true;
                        break;
                    }
                }
            } else {
                // verification of operation continuity for all other authentication methods
                for (AuthStep step : operationPersistenceService.getResponseAuthSteps(operationEntity)) {
                    if (step.getAuthMethod() == request.getAuthMethod()) {
                        stepAuthMethodValid = true;
                        break;
                    }
                }
            }
            if (!stepAuthMethodValid) {
                throw new InvalidRequestException("Operation update failed, because authentication method is invalid (operationId: " + request.getOperationId() + ").");
            }
        }
        for (OperationHistoryEntity historyItem : operationHistory) {
            if (historyItem.getResponseResult() == AuthResult.DONE) {
                throw new OperationAlreadyFinishedException("Operation update failed, because operation is already in DONE state (operationId: " + request.getOperationId() + ").");
            }
            if (historyItem.getResponseResult() == AuthResult.FAILED) {
                // Do not allow to update a canceled operation (e.g. authorize an operation which is canceled),
                // unless the request is a cancellation request - double cancellation of operations is allowed.
                if (historyItem.getRequestAuthStepResult() == AuthStepResult.CANCELED && request.getAuthStepResult() != AuthStepResult.CANCELED) {
                    throw new OperationAlreadyCanceledException("Operation update failed, because operation is canceled (operationId: " + request.getOperationId() + ").");
                } else if (request.getAuthStepResult() != AuthStepResult.CANCELED) {
                    // #102 - allow double cancellation requests, cancel requests may come from multiple channels, so this is a supported scenario
                    throw new OperationAlreadyFailedException("Operation update failed, because operation is already in FAILED state (operationId: " + request.getOperationId() + ").");
                }
            }
        }
    }

}
