/*
 * PowerAuth Web Flow and related software components
 * Copyright (C) 2021 Wultra s.r.o.
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

import com.wultra.core.audit.base.Audit;
import io.getlime.security.powerauth.app.nextstep.repository.OperationRepository;
import io.getlime.security.powerauth.app.nextstep.repository.catalogue.RepositoryCatalogue;
import io.getlime.security.powerauth.app.nextstep.repository.model.entity.OperationEntity;
import io.getlime.security.powerauth.app.nextstep.repository.model.entity.StepDefinitionEntity;
import io.getlime.security.powerauth.app.nextstep.service.catalogue.ServiceCatalogue;
import io.getlime.security.powerauth.lib.nextstep.model.entity.AuthStep;
import io.getlime.security.powerauth.lib.nextstep.model.entity.EnableMobileTokenResult;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthMethod;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthResult;
import io.getlime.security.powerauth.lib.nextstep.model.exception.InvalidConfigurationException;
import io.getlime.security.powerauth.lib.nextstep.model.exception.OperationNotFoundException;
import io.getlime.security.powerauth.lib.nextstep.model.request.UpdateOperationRequest;
import io.getlime.security.powerauth.lib.nextstep.model.response.UpdateOperationResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;

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
    private final ServiceCatalogue serviceCatalogue;
    private final Audit audit;

    /**
     * Authentication change service constructor.
     * @param repositoryCatalogue Repository catalogue.
     * @param serviceCatalogue Service catalogue.
     * @param audit Audit interface.
     */
    @Autowired
    public AuthMethodChangeService(RepositoryCatalogue repositoryCatalogue, @Lazy ServiceCatalogue serviceCatalogue, Audit audit) {
        this.operationRepository = repositoryCatalogue.getOperationRepository();
        this.serviceCatalogue = serviceCatalogue;
        this.audit = audit;
    }

    /**
     * Downgrade an authentication method for an operation.
     * @param request Update operation request.
     * @param response Update operation response.
     * @param stepDefinitions Next step definitions.
     * @return Update operation response.
     */
    public UpdateOperationResponse downgradeAuthMethod(UpdateOperationRequest request, UpdateOperationResponse response, List<StepDefinitionEntity> stepDefinitions) {
        logger.info("Authentication downgrade started for operation ID: {}, authentication method: {}", request.getOperationId(), request.getTargetAuthMethod());
        final AuthMethod targetAuthMethod = request.getTargetAuthMethod();
        if (targetAuthMethod == null) {
            // Invalid request - authentication method downgrade expects a target authentication method
            response.setResult(AuthResult.FAILED);
            response.setResultDescription("error.invalidRequest");
            logger.warn("Authentication downgrade failed for operation ID: {}, authentication method: {}", request.getOperationId(), request.getTargetAuthMethod());
            audit.warn("Authentication downgrade failed for operation ID: {}, authentication method: {}", request.getOperationId(), request.getTargetAuthMethod());
            return response;
        }
        for (StepDefinitionEntity stepDef : stepDefinitions) {
            if (stepDef.getResponseAuthMethod() == targetAuthMethod) {
                final AuthStep authStep = new AuthStep();
                authStep.setAuthMethod(targetAuthMethod);
                response.getSteps().add(authStep);
                response.setResult(AuthResult.CONTINUE);
                logger.info("Authentication downgrade succeeded for operation ID: {}, authentication method: {}", request.getOperationId(), request.getTargetAuthMethod());
                audit.info("Authentication downgrade succeeded for operation ID: {}, authentication method: {}", request.getOperationId(), request.getTargetAuthMethod());
                return response;
            }
        }
        // Target authentication method set for downgrade is not available
        response.setResult(AuthResult.FAILED);
        response.setResultDescription("error.noAuthMethod");
        logger.warn("Authentication downgrade failed for operation ID: {}, authentication method: {}", request.getOperationId(), request.getTargetAuthMethod());
        audit.warn("Authentication downgrade failed for operation ID: {}, authentication method: {}", request.getOperationId(), request.getTargetAuthMethod());
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
        final OperationPersistenceService operationPersistenceService = serviceCatalogue.getOperationPersistenceService();
        logger.info("Set chosen authentication method started for operation ID: {}, authentication method: {}", request.getOperationId(), request.getTargetAuthMethod());
        final String operationId = request.getOperationId();
        final AuthMethod targetAuthMethod = request.getTargetAuthMethod();
        if (targetAuthMethod == null) {
            // Invalid request - authentication method choice expects a target authentication method
            response.setResult(AuthResult.FAILED);
            response.setResultDescription("error.invalidRequest");
            logger.info("Set chosen authentication method failed for operation ID: {}, authentication method: {}", request.getOperationId(), request.getTargetAuthMethod());
            return response;
        }
        final OperationEntity operation = operationRepository.findById(operationId).orElseThrow(() ->
                new OperationNotFoundException("Operation not found, operation ID: " + operationId));
        if (operation.getResult() != AuthResult.CONTINUE) {
            // Invalid request - authentication method choice expects a CONTINUE operation result
            response.setResult(AuthResult.FAILED);
            response.setResultDescription("error.invalidRequest");
            logger.info("Set chosen authentication method failed for operation ID: {}, authentication method: {}", request.getOperationId(), request.getTargetAuthMethod());
            return response;
        }
        response.setChosenAuthMethod(targetAuthMethod);
        response.getSteps().addAll(operationPersistenceService.getResponseAuthSteps(operation));
        response.setResult(AuthResult.CONTINUE);
        if (targetAuthMethod == AuthMethod.POWERAUTH_TOKEN) {
            // Specific logic for enabling POWERAUTH_TOKEN authentication method
            logger.info("Set chosen authentication method succeeded for operation ID: {}, authentication method: {}", request.getOperationId(), request.getTargetAuthMethod());
            return enableMobileToken(response, operation);
        }
        logger.info("Set chosen authentication method succeeded for operation ID: {}, authentication method: {}", request.getOperationId(), request.getTargetAuthMethod());
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
        final MobileTokenConfigurationService mobileTokenConfigurationService = serviceCatalogue.getMobileTokenConfigurationService();
        logger.info("Enable mobile token started for operation ID: {}", operation.getOperationId());
        final String userId = operation.getUserId();
        if (userId == null) {
            // User ID must be set before mobile token is enabled
            response.setResult(AuthResult.FAILED);
            response.setResultDescription("error.invalidRequest");
        }
        final EnableMobileTokenResult result = mobileTokenConfigurationService.enableMobileToken(operation);
        response.setMobileTokenActive(result.isEnabled());
        if (result.isEnabled()) {
            response.setPowerAuthOperationId(result.getPowerAuthOperationId());
            logger.info("Enable mobile token succeeded for operation ID: {}", operation.getOperationId());
        } else {
            // Mobile token is not available, return failed result
            response.getSteps().clear();
            response.setResult(AuthResult.FAILED);
            response.setResultDescription("operation.methodNotAvailable");
            logger.info("Enable mobile token failed for operation ID: {}", operation.getOperationId());
        }
        return response;
    }

}
