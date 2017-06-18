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
import io.getlime.security.powerauth.app.nextstep.repository.StepDefinitionRepository;
import io.getlime.security.powerauth.app.nextstep.repository.model.entity.OperationEntity;
import io.getlime.security.powerauth.app.nextstep.repository.model.entity.StepDefinitionEntity;
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
    private UserPrefsService userPrefsService;
    private Map<String, List<StepDefinitionEntity>> stepDefinitionsPerOperation;

    @Autowired
    public StepResolutionService(StepDefinitionRepository stepDefinitionRepository, OperationPersistenceService operationPersistenceService,
                                 IdGeneratorService idGeneratorService, NextStepServerConfiguration nextStepServerConfiguration,
                                 UserPrefsService userPrefsService) {
        this.operationPersistenceService = operationPersistenceService;
        this.idGeneratorService = idGeneratorService;
        this.nextStepServerConfiguration = nextStepServerConfiguration;
        this.userPrefsService = userPrefsService;
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
        response.setOperationId(idGeneratorService.generateOperationId());
        // AuthStepResult and AuthMethod are not available when creating the operation, null values are used to ignore them
        List<StepDefinitionEntity> stepDefinitions = filterSteps(request.getOperationName(), OperationRequestType.CREATE, null, null, null);
        response.getSteps().addAll(prepareAuthSteps(stepDefinitions));
        response.setTimestampCreated(new Date());
        response.setTimestampExpires(new DateTime().plusSeconds(nextStepServerConfiguration.getOperationExpirationTime()).toDate());
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
        UpdateOperationResponse response = new UpdateOperationResponse();
        response.setOperationId(request.getOperationId());
        response.setUserId(request.getUserId());
        response.setTimestampCreated(new Date());
        if (operation.isExpired()) {
            // Operation fails in case it is expired.
            // Response expiration time matches operation expiration to avoid extending expiration time of the operation.
            response.setTimestampExpires(operation.getTimestampExpires());
            response.setResult(AuthResult.FAILED);
            response.setResultDescription("authentication.timeout");
            return response;
        }
        response.setTimestampExpires(new DateTime().plusSeconds(nextStepServerConfiguration.getOperationExpirationTime()).toDate());
        List<StepDefinitionEntity> stepDefinitions = filterSteps(operation.getOperationName(), OperationRequestType.UPDATE, request.getAuthStepResult(), request.getAuthMethod(), request.getUserId());
        // TODO - verify priorities - issue #30
        response.getSteps().addAll(prepareAuthSteps(stepDefinitions));
        Set<AuthResult> allResults = new HashSet<>();
        for (StepDefinitionEntity stepDef : stepDefinitions) {
            allResults.add(stepDef.getResponseResult());
        }
        if (allResults.size() == 1) {
            // Correct response - only one AuthResult found. Return all matching steps.
            response.setResult(allResults.iterator().next());
            return response;
        } else if (allResults.size() > 1) {
            // This state should not occur - there are multiple step definitions with different values of AuthResult.
            // Fail the operation - this should not happen unless step definitions are misconfigured.
            response.getSteps().clear();
            response.setResult(AuthResult.FAILED);
            response.setResultDescription("error.unknown");
            return response;
        } else {
            // No step definition matches the current criteria. Suitable step definitions might have been filtered out
            // via user preferences. Fail the operation.
            // TODO - fallback mechanism - see issue #32
            response.getSteps().clear();
            response.setResult(AuthResult.FAILED);
            response.setResultDescription("error.noAuthMethod");
            return response;
        }
    }

    /**
     * Filters step definitions by given parameters and returns a list of step definitions which match the query.
     *
     * @param operationName  name of the operation
     * @param operationType  type of the operation - CREATE/UPDATE
     * @param authStepResult result of previous authentication step
     * @param authMethod     authentication method of previous authentication step
     * @return filtered list of steps
     */
    private List<StepDefinitionEntity> filterSteps(String operationName, OperationRequestType operationType, AuthStepResult authStepResult, AuthMethod authMethod, String userId) {
        List<StepDefinitionEntity> stepDefinitions = stepDefinitionsPerOperation.get(operationName);
        List<AuthMethod> authMethodsAvailableForUser = null;
        if (userId != null) {
            authMethodsAvailableForUser = userPrefsService.listAuthMethodsEnabledForUser(userId);
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

}
