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
package io.getlime.security.powerauth.app.nextstep.service;

import io.getlime.security.powerauth.app.nextstep.configuration.NextStepServerConfiguration;
import io.getlime.security.powerauth.app.nextstep.converter.OperationConverter;
import io.getlime.security.powerauth.app.nextstep.repository.AuthMethodRepository;
import io.getlime.security.powerauth.app.nextstep.repository.OperationConfigRepository;
import io.getlime.security.powerauth.app.nextstep.repository.OperationMethodConfigRepository;
import io.getlime.security.powerauth.app.nextstep.repository.StepDefinitionRepository;
import io.getlime.security.powerauth.app.nextstep.repository.catalogue.RepositoryCatalogue;
import io.getlime.security.powerauth.app.nextstep.repository.model.entity.*;
import io.getlime.security.powerauth.app.nextstep.service.catalogue.ServiceCatalogue;
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
import org.springframework.context.annotation.Lazy;
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

    private final StepDefinitionRepository stepDefinitionRepository;
    private final AuthMethodRepository authMethodRepository;
    private final OperationConfigRepository operationConfigRepository;
    private final OperationMethodConfigRepository operationMethodConfigRepository;
    private final ServiceCatalogue serviceCatalogue;
    private final NextStepServerConfiguration nextStepServerConfiguration;
    private final OperationConverter operationConverter;

    private final Map<String, List<StepDefinitionEntity>> stepDefinitionsPerOperation = new HashMap<>();

    private final Object stepDefinitionLock = new Object();

    /**
     * Service constructor.
     * @param repositoryCatalogue Repository catalogue.
     * @param serviceCatalogue Service catalogue.
     * @param nextStepServerConfiguration Next Step server configuration.
     * @param operationConverter Operation converter.
     */
    @Autowired
    public StepResolutionService(RepositoryCatalogue repositoryCatalogue, @Lazy ServiceCatalogue serviceCatalogue, NextStepServerConfiguration nextStepServerConfiguration, OperationConverter operationConverter) {
        this.stepDefinitionRepository = repositoryCatalogue.getStepDefinitionRepository();
        this.authMethodRepository = repositoryCatalogue.getAuthMethodRepository();
        this.operationConfigRepository = repositoryCatalogue.getOperationConfigRepository();
        this.operationMethodConfigRepository = repositoryCatalogue.getOperationMethodConfigRepository();
        this.serviceCatalogue = serviceCatalogue;
        this.nextStepServerConfiguration = nextStepServerConfiguration;
        this.operationConverter = operationConverter;
        reloadStepDefinitions();
    }

    /**
     * Reload step definitions from database.
     */
    public void reloadStepDefinitions() {
        synchronized (stepDefinitionLock) {
            stepDefinitionsPerOperation.clear();
            final List<String> operationNames = stepDefinitionRepository.findDistinctOperationNames();
            for (String operationName : operationNames) {
                stepDefinitionsPerOperation.put(operationName, stepDefinitionRepository.findStepDefinitionsForOperation(operationName));
            }
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
        final OperationPersistenceService operationPersistenceService = serviceCatalogue.getOperationPersistenceService();
        final IdGeneratorService idGeneratorService = serviceCatalogue.getIdGeneratorService();

        final CreateOperationResponse response = new CreateOperationResponse();
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
        response.setOperationData(request.getOperationData());
        response.setOperationNameExternal(request.getOperationNameExternal());
        response.setExternalTransactionId(request.getExternalTransactionId());
        // AuthStepResult and AuthMethod are not available when creating the operation, null values are used to ignore them
        final List<StepDefinitionEntity> stepDefinitions = filterStepDefinitions(request.getOperationName(), OperationRequestType.CREATE, null, null, null);
        response.getSteps().addAll(convertAuthSteps(stepDefinitions));
        response.setTimestampCreated(new Date());
        final int expirationTime = getExpirationTime(request.getOperationName());
        final ZonedDateTime timestampExpires = ZonedDateTime.now().plusSeconds(expirationTime);
        response.setTimestampExpires(Date.from(timestampExpires.toInstant()));
        response.setFormData(request.getFormData());
        final Set<AuthResult> allResults = new HashSet<>();
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
        final OperationPersistenceService operationPersistenceService = serviceCatalogue.getOperationPersistenceService();
        final AuthMethodChangeService authMethodChangeService = serviceCatalogue.getAuthMethodChangeService();

        final OperationEntity operation = operationPersistenceService.getOperation(request.getOperationId());
        checkLegitimacyOfUpdate(operation, request);
        final UpdateOperationResponse response = new UpdateOperationResponse();
        response.setOperationId(request.getOperationId());
        response.setOperationName(operation.getOperationName());
        response.setUserId(request.getUserId());
        response.setOrganizationId(request.getOrganizationId());
        response.setOperationNameExternal(operation.getExternalOperationName());
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
        final int expirationTime = getExpirationTime(operation.getOperationName());
        final ZonedDateTime timestampExpires = ZonedDateTime.now().plusSeconds(expirationTime);
        response.setTimestampExpires(Date.from(timestampExpires.toInstant()));
        final AuthStepResult authStepResult = request.getAuthStepResult();
        if (isAuthMethodFailed(operation, request.getAuthMethod(), authStepResult)) {
            // check whether the authentication method has already failed completely, in case it has failed, update the authStepResult
            request.setAuthStepResult(AuthStepResult.AUTH_METHOD_FAILED);
        }

        final List<StepDefinitionEntity> stepDefinitions = filterStepDefinitions(operation.getOperationName(), OperationRequestType.UPDATE, request.getAuthStepResult(), request.getAuthMethod(), request.getUserId());
        sortSteps(stepDefinitions);
        verifyDuplicatePrioritiesAbsent(stepDefinitions);

        // Authentication method downgrade result is handled separately due to specific logic
        if (request.getAuthStepResult() == AuthStepResult.AUTH_METHOD_DOWNGRADE) {
            return authMethodChangeService.downgradeAuthMethod(request, response, stepDefinitions);
        }
        // Authentication method chosen result is handled separately due to specific logic
        if (request.getAuthStepResult() == AuthStepResult.AUTH_METHOD_CHOSEN) {
            return authMethodChangeService.setChosenAuthMethod(request, response);
        }

        final Set<AuthResult> allResults = new HashSet<>();
        for (StepDefinitionEntity stepDef : stepDefinitions) {
            allResults.add(stepDef.getResponseResult());
        }
        if (allResults.size() == 1) {
            // Straightforward response - only one AuthResult found. Return all matching steps.
            response.getSteps().addAll(convertAuthSteps(stepDefinitions));
            response.setResult(allResults.iterator().next());
            return response;
        } else if (allResults.size() > 1) {
            // We need to make sure a specific AuthResult is returned in case multiple authentication methods lead
            // to different AuthResults.
            // In case there is any DONE or CONTINUE next step, prefer it over FAILED. FAILED state can be caused by a failing
            // authentication method or by method canceled by the user, in this case try to switch to other
            // authentication method if it is available.
            final Map<AuthResult, List<StepDefinitionEntity>> stepsByAuthResult = stepDefinitions
                    .stream()
                    .collect(Collectors.groupingBy(StepDefinitionEntity::getResponseResult));
            if (stepsByAuthResult.containsKey(AuthResult.DONE)) {
                List<StepDefinitionEntity> doneSteps = stepsByAuthResult.get(AuthResult.DONE);
                response.getSteps().addAll(convertAuthSteps(doneSteps));
                response.setResult(AuthResult.DONE);
                return response;
            } else if (stepsByAuthResult.containsKey(AuthResult.CONTINUE)) {
                List<StepDefinitionEntity> continueSteps = stepsByAuthResult.get(AuthResult.CONTINUE);
                response.getSteps().addAll(convertAuthSteps(continueSteps));
                response.setResult(AuthResult.CONTINUE);
                return response;
            } else if (stepsByAuthResult.containsKey(AuthResult.FAILED)) {
                List<StepDefinitionEntity> failedSteps = stepsByAuthResult.get(AuthResult.FAILED);
                response.getSteps().addAll(convertAuthSteps(failedSteps));
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
    private synchronized List<StepDefinitionEntity> filterStepDefinitions(String operationName, OperationRequestType operationType, AuthStepResult authStepResult, AuthMethod authMethod, String userId) throws InvalidConfigurationException {
        final AuthMethodService authMethodService = serviceCatalogue.getAuthMethodService();
        final MobileTokenConfigurationService mobileTokenConfigurationService = serviceCatalogue.getMobileTokenConfigurationService();
        final List<StepDefinitionEntity> stepDefinitions;
        synchronized (stepDefinitionLock) {
            // Step definitions may be modified and reloaded using REST API, lock is required
            stepDefinitions = stepDefinitionsPerOperation.get(operationName);
        }
        final List<AuthMethod> authMethodsAvailableForUser = new ArrayList<>();
        if (userId != null) {
            for (UserAuthMethodDetail userAuthMethodDetail : authMethodService.listAuthMethodsEnabledForUser(userId)) {
                authMethodsAvailableForUser.add(userAuthMethodDetail.getAuthMethod());
            }
        }
        final List<StepDefinitionEntity> filteredStepDefinitions = new ArrayList<>();
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
            // filter out POWERAUTH_TOKEN method in case it is not enabled for given operation and authentication method and active
            if (userId != null && stepDef.getResponseAuthMethod() == AuthMethod.POWERAUTH_TOKEN
                    && !mobileTokenConfigurationService.isMobileTokenActive(userId, operationName, AuthMethod.POWERAUTH_TOKEN)) {
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
        stepDefinitions.sort(Comparator.comparing(StepDefinitionEntity::getResponsePriority));
    }

    /**
     * Verifies that each priority is present only once in the list of step definitions.
     *
     * @param stepDefinitions step definitions
     * @throws InvalidConfigurationException Thrown when Next Step configuration is invalid.
     */
    private void verifyDuplicatePrioritiesAbsent(List<StepDefinitionEntity> stepDefinitions) throws InvalidConfigurationException {
        final Map<Long, List<StepDefinitionEntity>> stepsByPriority = stepDefinitions
                .stream()
                .collect(Collectors.groupingBy(StepDefinitionEntity::getResponsePriority));
        if (stepsByPriority.size() != stepDefinitions.size()) {
            throw new InvalidConfigurationException("Multiple steps with the same priority detected while resolving next step.");
        }
    }

    /**
     * Converts List<StepDefinitionEntity> into a List<AuthStep>.
     *
     * @param stepDefinitions Step definitions to convert.
     * @return Converted list of authentication steps.
     */
    private List<AuthStep> convertAuthSteps(List<StepDefinitionEntity> stepDefinitions) {
        final List<AuthStep> authSteps = new ArrayList<>();
        for (StepDefinitionEntity stepDef : stepDefinitions) {
            if (stepDef.getResponseAuthMethod() != null) {
                final AuthStep authStep = new AuthStep();
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
        final AuthMethodEntity authMethodEntity = authMethodRepository.findByAuthMethod(authMethod).orElseThrow(() ->
                new AuthMethodNotFoundException("Authentication method not found: " + authMethod));
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
            final int maxAuthFails = getMaxAuthFails(operation, authMethodEntity);
            return failureCount >= maxAuthFails;
        }
        return false;
    }

    /**
     * Get number of remaining authentication attempts for current authentication method.
     * @param operation Operation.
     * @return Number of remaining authentication attempts. Null value returned for no limit.
     */
    public Integer getNumberOfRemainingAttempts(OperationEntity operation) {
        final OperationHistoryEntity currentOperationHistory = operation.getCurrentOperationHistoryEntity();
        if (currentOperationHistory == null) {
            return null;
        }
        final AuthMethod authMethod = currentOperationHistory.getRequestAuthMethod();
        // check whether authMethod supports check of authorization failure count
        final Optional<AuthMethodEntity> authMethodEntityOptional = authMethodRepository.findByAuthMethod(authMethod);
        if (authMethodEntityOptional.isEmpty()) {
            return null;
        }
        // in case authentication method previously failed, it is already failed
        for (OperationHistoryEntity history : operation.getOperationHistory()) {
            if (history.getRequestAuthMethod() == authMethod && history.getRequestAuthStepResult() == AuthStepResult.AUTH_METHOD_FAILED) {
                return 0;
            }
        }
        final AuthMethodEntity authMethodEntity = authMethodEntityOptional.get();
        if (authMethodEntity.getCheckAuthFails()) {
            // count failures
            int failureCount = 0;
            for (OperationHistoryEntity history : operation.getOperationHistory()) {
                // add all failures from history for this method
                if (history.getRequestAuthMethod() == authMethod && history.getRequestAuthStepResult() == AuthStepResult.AUTH_FAILED) {
                    failureCount++;
                }
            }
            final int maxAuthFails = getMaxAuthFails(operation, authMethodEntity);
            if (failureCount >= maxAuthFails) {
                return 0;
            }
            return maxAuthFails - failureCount;
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
    private void checkLegitimacyOfUpdate(OperationEntity operationEntity, UpdateOperationRequest request) throws OperationAlreadyFinishedException, OperationAlreadyCanceledException, OperationAlreadyFailedException, OperationNotValidException, InvalidRequestException, OperationNotFoundException {
        final OperationPersistenceService operationPersistenceService = serviceCatalogue.getOperationPersistenceService();
        if (request == null || request.getOperationId() == null) {
            throw new InvalidRequestException("Operation update failed, because request is invalid.");
        }
        if (operationEntity == null) {
            throw new OperationNotFoundException("Operation update failed, because operation does not exist, operation ID: " + request.getOperationId() + ".");
        }
        if (request.getAuthMethod() == null) {
            throw new InvalidRequestException("Operation update failed, because authentication method is missing, operation ID: " + request.getOperationId() + ".");
        }
        // INIT method can cancel other operations due to concurrency check and can set chosen authentication method
        if (request.getAuthMethod() == AuthMethod.INIT &&
                (request.getAuthStepResult() != AuthStepResult.CANCELED
                        && request.getAuthStepResult() != AuthStepResult.AUTH_METHOD_CHOSEN)) {
            throw new InvalidRequestException("Operation update failed, because INIT method cannot be updated, operation ID: " + request.getOperationId() + ".");
        }
        if (request.getAuthStepResult() == null) {
            throw new InvalidRequestException("Operation update failed, because result of authentication step is missing, operation ID: " + request.getOperationId() + ".");
        }
        final List<OperationHistoryEntity> operationHistory = operationEntity.getOperationHistory();
        if (operationHistory.isEmpty()) {
            throw new OperationNotValidException("Operation update failed, because operation is missing its history, operation ID: " + request.getOperationId() + ".");
        }
        final OperationHistoryEntity initOperationItem = operationHistory.get(0);
        if (initOperationItem.getRequestAuthMethod() != AuthMethod.INIT || initOperationItem.getRequestAuthStepResult() != AuthStepResult.CONFIRMED) {
            throw new OperationNotValidException("Operation update failed, because INIT step for this operation is invalid, operation ID: " + request.getOperationId() + ".");
        }
        final OperationHistoryEntity currentOperationHistory = operationEntity.getCurrentOperationHistoryEntity();
        // operation can be canceled anytime (e.g. by closed Web Socket) - do not check for step continuation
        if (currentOperationHistory != null && currentOperationHistory.getResponseResult() == AuthResult.CONTINUE
                && request.getAuthStepResult() != AuthStepResult.CANCELED) {
            boolean stepAuthMethodValid = false;
            final List<AuthStep> authSteps = operationPersistenceService.getResponseAuthSteps(operationEntity);
            // check whether request AuthMethod is available in response AuthSteps - this verifies operation continuity
            if (request.getAuthMethod() == AuthMethod.SHOW_OPERATION_DETAIL) {
                // special handling for SHOW_OPERATION_DETAIL - either SMS_KEY or POWERAUTH_TOKEN are present in next steps
                for (AuthStep step : authSteps) {
                    if (step.getAuthMethod() == AuthMethod.SMS_KEY || step.getAuthMethod() == AuthMethod.POWERAUTH_TOKEN) {
                        stepAuthMethodValid = true;
                        break;
                    }
                }
            } if (request.getAuthStepResult() == AuthStepResult.AUTH_METHOD_CHOSEN) {
                // verification of operation continuity for chosen authentication method
                for (AuthStep step : authSteps) {
                    if (currentOperationHistory.getRequestAuthMethod() == request.getAuthMethod()
                            && step.getAuthMethod() == request.getTargetAuthMethod()) {
                        stepAuthMethodValid = true;
                        break;
                    }
                }
            } else {
                // verification of operation continuity for all other authentication methods
                for (AuthStep step : authSteps) {
                    if (step.getAuthMethod() == request.getAuthMethod()) {
                        stepAuthMethodValid = true;
                        break;
                    }
                }
            }
            if (!stepAuthMethodValid) {
                throw new InvalidRequestException("Operation update failed, because authentication method is invalid, operation ID: " + request.getOperationId() + ".");
            }
        }
        for (OperationHistoryEntity historyItem : operationHistory) {
            if (historyItem.getResponseResult() == AuthResult.DONE) {
                throw new OperationAlreadyFinishedException("Operation update failed, because operation is already in DONE state, operation ID: " + request.getOperationId() + ".");
            }
            if (historyItem.getResponseResult() == AuthResult.FAILED) {
                // Do not allow to update a canceled operation (e.g. authorize an operation which is canceled),
                // unless the request is a cancellation request - double cancellation of operations is allowed.
                if (historyItem.getRequestAuthStepResult() == AuthStepResult.CANCELED && request.getAuthStepResult() != AuthStepResult.CANCELED) {
                    throw new OperationAlreadyCanceledException("Operation update failed, because operation is canceled, operation ID: " + request.getOperationId() + ".");
                } else if (request.getAuthStepResult() != AuthStepResult.CANCELED) {
                    // #102 - allow double cancellation requests, cancel requests may come from multiple channels, so this is a supported scenario
                    throw new OperationAlreadyFailedException("Operation update failed, because operation is already in FAILED state, operation ID: " + request.getOperationId() + ".");
                }
            }
        }
    }

    /**
     * Get expiration time for the operation in seconds.
     * @param operationName Operation name.
     * @return Expiration time in seconds.
     */
    private int getExpirationTime(String operationName) {
        Integer expirationTime = null;
        final Optional<OperationConfigEntity> configOptional = operationConfigRepository.findById(operationName);
        if (configOptional.isPresent()) {
            OperationConfigEntity config = configOptional.get();
            expirationTime = config.getExpirationTime();
        }
        if (expirationTime == null) {
            expirationTime = nextStepServerConfiguration.getOperationExpirationTime();
        }
        return expirationTime;
    }

    /**
     * Get maximum number of authentication failures for given operation and authentication method.
     * @param operation Operation entity.
     * @param authMethod Authentication method.
     * @return Maximum number of authentication failures.
     */
    private int getMaxAuthFails(OperationEntity operation, AuthMethodEntity authMethod) {
        final OperationMethodConfigEntity.OperationAuthMethodKey primaryKey = new OperationMethodConfigEntity.OperationAuthMethodKey(operation.getOperationName(), authMethod.getAuthMethod());
        final Optional<OperationMethodConfigEntity> configOptional = operationMethodConfigRepository.findById(primaryKey);
        if (configOptional.isPresent()) {
            final OperationMethodConfigEntity config = configOptional.get();
            return config.getMaxAuthFails();
        }
        return authMethod.getMaxAuthFails();
    }

}
