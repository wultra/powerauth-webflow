/*
 * Copyright 2021 Wultra s.r.o.
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

import io.getlime.security.powerauth.app.nextstep.repository.OperationRepository;
import io.getlime.security.powerauth.app.nextstep.repository.model.entity.OperationEntity;
import io.getlime.security.powerauth.app.nextstep.repository.model.entity.StepDefinitionEntity;
import io.getlime.security.powerauth.lib.nextstep.model.entity.AuthStep;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthMethod;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthResult;
import io.getlime.security.powerauth.lib.nextstep.model.exception.InvalidConfigurationException;
import io.getlime.security.powerauth.lib.nextstep.model.exception.OperationNotFoundException;
import io.getlime.security.powerauth.lib.nextstep.model.request.UpdateOperationRequest;
import io.getlime.security.powerauth.lib.nextstep.model.response.UpdateOperationResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * This service handles authentication method changes within an operation with specific logic
 * for various use cases.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Service
public class AuthMethodChangeService {

    private static final Logger logger = LoggerFactory.getLogger(AuthMethodChangeService.class);

    private final OperationRepository operationRepository;
    private final OperationPersistenceService operationPersistenceService;
    private final MobileTokenConfigurationService mobileTokenConfigurationService;

    /**
     * Authentication change service constructor.
     * @param operationRepository Operation repository.
     * @param operationPersistenceService Operation persistence service.
     * @param mobileTokenConfigurationService Mobile token configuration service.
     */
    @Autowired
    public AuthMethodChangeService(OperationRepository operationRepository, OperationPersistenceService operationPersistenceService, MobileTokenConfigurationService mobileTokenConfigurationService) {
        this.operationRepository = operationRepository;
        this.operationPersistenceService = operationPersistenceService;
        this.mobileTokenConfigurationService = mobileTokenConfigurationService;
    }

    /**
     * Downgrade an authentication method for an operation.
     * @param request Update operation request.
     * @param response Update operation response.
     * @param stepDefinitions Next step definitions.
     * @return Update operation response.
     */
    public UpdateOperationResponse downgradeAuthMethod(UpdateOperationRequest request, UpdateOperationResponse response, List<StepDefinitionEntity> stepDefinitions) {
        AuthMethod targetAuthMethod = request.getTargetAuthMethod();
        if (targetAuthMethod == null) {
            // Invalid request - authentication method downgrade expects a target authentication method
            response.setResult(AuthResult.FAILED);
            response.setResultDescription("error.invalidRequest");
            return response;
        }
        for (StepDefinitionEntity stepDef : stepDefinitions) {
            if (stepDef.getResponseAuthMethod() == targetAuthMethod) {
                AuthStep authStep = new AuthStep();
                authStep.setAuthMethod(targetAuthMethod);
                response.getSteps().add(authStep);
                response.setResult(AuthResult.CONTINUE);
                return response;
            }
        }
        // Target authentication method set for downgrade is not available
        response.setResult(AuthResult.FAILED);
        response.setResultDescription("error.noAuthMethod");
        return response;
    }

    /**
     * Set chosen authentication method for an operation.
     * @param request Update operation request.
     * @param response Update operation response.
     * @return Update operation response.
     * @throws OperationNotFoundException Thrown when operation does not exist.
     * @throws InvalidConfigurationException Thrown when Next Step configuration is invalid.
     */
    public UpdateOperationResponse setChosenAuthMethod(UpdateOperationRequest request, UpdateOperationResponse response) throws InvalidConfigurationException, OperationNotFoundException {
        String operationId = request.getOperationId();
        AuthMethod targetAuthMethod = request.getTargetAuthMethod();
        if (targetAuthMethod == null) {
            // Invalid request - authentication method choice expects a target authentication method
            response.setResult(AuthResult.FAILED);
            response.setResultDescription("error.invalidRequest");
            return response;
        }
        Optional<OperationEntity> operationOptional = operationRepository.findById(operationId);
        if (!operationOptional.isPresent()) {
            throw new OperationNotFoundException("Operation not found, operation ID: " + operationId);
        }
        OperationEntity operation = operationOptional.get();
        if (operation.getResult() != AuthResult.CONTINUE) {
            // Invalid request - authentication method choice expects a CONTINUE operation result
            response.setResult(AuthResult.FAILED);
            response.setResultDescription("error.invalidRequest");
            return response;
        }
        response.setChosenAuthMethod(targetAuthMethod);
        response.getSteps().addAll(operationPersistenceService.getResponseAuthSteps(operation));
        response.setResult(AuthResult.CONTINUE);
        if (targetAuthMethod == AuthMethod.POWERAUTH_TOKEN) {
            // Specific logic for enabling POWERAUTH_TOKEN authentication method
            return enableMobileToken(response, operation);
        }
        // Other methods do not have any extra steps
        return response;
    }

    /**
     * Enable the mobile token.
     *
     * @param response Update operation response.
     * @param operation Operation entity.
     * @return Update operation response.
     * @throws InvalidConfigurationException Thrown when Next Step configuration is invalid.
     */
    private UpdateOperationResponse enableMobileToken(UpdateOperationResponse response, OperationEntity operation) throws InvalidConfigurationException {
        String userId = operation.getUserId();
        if (userId == null) {
            // User ID must be set before mobile token is enabled
            response.setResult(AuthResult.FAILED);
            response.setResultDescription("error.invalidRequest");
        }
        boolean enabled = mobileTokenConfigurationService.enableMobileToken(operation);
        response.setMobileTokenActive(enabled);
        if (!enabled) {
            // Mobile token is not available, return failed result
            response.getSteps().clear();
            response.setResult(AuthResult.FAILED);
            response.setResultDescription("operation.methodNotAvailable");
        }
        return response;
    }

}
