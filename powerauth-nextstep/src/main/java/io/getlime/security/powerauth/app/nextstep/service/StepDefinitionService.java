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

import com.wultra.core.audit.base.Audit;
import com.wultra.core.audit.base.model.AuditDetail;
import io.getlime.security.powerauth.app.nextstep.controller.OrganizationController;
import io.getlime.security.powerauth.app.nextstep.repository.StepDefinitionRepository;
import io.getlime.security.powerauth.app.nextstep.repository.catalogue.RepositoryCatalogue;
import io.getlime.security.powerauth.app.nextstep.repository.model.entity.StepDefinitionEntity;
import io.getlime.security.powerauth.app.nextstep.service.catalogue.ServiceCatalogue;
import io.getlime.security.powerauth.lib.nextstep.model.exception.StepDefinitionAlreadyExistsException;
import io.getlime.security.powerauth.lib.nextstep.model.exception.StepDefinitionNotFoundException;
import io.getlime.security.powerauth.lib.nextstep.model.request.CreateStepDefinitionRequest;
import io.getlime.security.powerauth.lib.nextstep.model.request.DeleteStepDefinitionRequest;
import io.getlime.security.powerauth.lib.nextstep.model.response.CreateStepDefinitionResponse;
import io.getlime.security.powerauth.lib.nextstep.model.response.DeleteStepDefinitionResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Service which handles persistence of step definitions.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Service
public class StepDefinitionService {

    private static final Logger logger = LoggerFactory.getLogger(OrganizationController.class);
    private static final String AUDIT_TYPE_CONFIGURATION = "CONFIGURATION";

    private final StepDefinitionRepository stepDefinitionRepository;
    private final ServiceCatalogue serviceCatalogue;
    private final Audit audit;

    /**
     * Step definition service constructor.
     * @param repositoryCatalogue Repository catalogue.
     * @param serviceCatalogue Service catalogue.
     * @param audit Audit interface.
     */
    @Autowired
    public StepDefinitionService(RepositoryCatalogue repositoryCatalogue, @Lazy ServiceCatalogue serviceCatalogue, Audit audit) {
        this.stepDefinitionRepository = repositoryCatalogue.getStepDefinitionRepository();
        this.serviceCatalogue = serviceCatalogue;
        this.audit = audit;
    }

    /**
     * Create a step definition.
     * @param request Create step definition request.
     * @return Create step definition response.
     * @throws StepDefinitionAlreadyExistsException Thrown when step definition already exists.
     */
    @Transactional
    public CreateStepDefinitionResponse createStepDefinition(CreateStepDefinitionRequest request) throws StepDefinitionAlreadyExistsException {
        final StepResolutionService stepResolutionService = serviceCatalogue.getStepResolutionService();
        final Optional<StepDefinitionEntity> stepDefinitionOptional = stepDefinitionRepository.findById(request.getStepDefinitionId());
        if (stepDefinitionOptional.isPresent()) {
            throw new StepDefinitionAlreadyExistsException("Step definition already exits, ID: " + request.getStepDefinitionId());
        }
        StepDefinitionEntity stepDefinition = new StepDefinitionEntity();
        stepDefinition.setStepDefinitionId(request.getStepDefinitionId());
        stepDefinition.setOperationName(request.getOperationName());
        stepDefinition.setOperationType(request.getOperationRequestType());
        stepDefinition.setRequestAuthStepResult(request.getRequestAuthStepResult());
        stepDefinition.setRequestAuthMethod(request.getRequestAuthMethod());
        stepDefinition.setResponsePriority(request.getResponsePriority());
        stepDefinition.setResponseAuthMethod(request.getResponseAuthMethod());
        stepDefinition.setResponseResult(request.getResponseResult());
        stepDefinition = stepDefinitionRepository.save(stepDefinition);
        logger.debug("Step definition was created, step definition ID: {}", stepDefinition.getStepDefinitionId());
        audit.info("Step definition was created", AuditDetail.builder()
                .type(AUDIT_TYPE_CONFIGURATION)
                .param("stepDefinition", stepDefinition)
                .build());
        stepResolutionService.reloadStepDefinitions();
        final CreateStepDefinitionResponse response = new CreateStepDefinitionResponse();
        response.setStepDefinitionId(request.getStepDefinitionId());
        response.setOperationName(request.getOperationName());
        response.setOperationRequestType(request.getOperationRequestType());
        response.setRequestAuthStepResult(request.getRequestAuthStepResult());
        response.setRequestAuthMethod(request.getRequestAuthMethod());
        response.setResponsePriority(request.getResponsePriority());
        response.setResponseAuthMethod(request.getResponseAuthMethod());
        response.setResponseResult(request.getResponseResult());
        return response;
    }

    /**
     * Delete a step definition.
     * @param request Delete step definition request.
     * @return Delete step definition response.
     * @throws StepDefinitionNotFoundException Thrown when step definition is not found.
     */
    @Transactional
    public DeleteStepDefinitionResponse deleteStepDefinition(DeleteStepDefinitionRequest request) throws StepDefinitionNotFoundException {
        final StepResolutionService stepResolutionService = serviceCatalogue.getStepResolutionService();
        final Optional<StepDefinitionEntity> stepDefinitionOptional = stepDefinitionRepository.findById(request.getStepDefinitionId());
        if (!stepDefinitionOptional.isPresent()) {
            throw new StepDefinitionNotFoundException("Step definition not found, ID: " + request.getStepDefinitionId());
        }
        final StepDefinitionEntity stepDefinition = stepDefinitionOptional.get();
        stepDefinitionRepository.delete(stepDefinition);
        logger.debug("Step definition was deleted, step definition ID: {}", stepDefinition.getStepDefinitionId());
        audit.info("Step definition was deleted", AuditDetail.builder()
                .type(AUDIT_TYPE_CONFIGURATION)
                .param("stepDefinitionId", stepDefinition.getStepDefinitionId())
                .build());
        stepResolutionService.reloadStepDefinitions();
        final DeleteStepDefinitionResponse response = new DeleteStepDefinitionResponse();
        response.setStepDefinitionId(stepDefinition.getStepDefinitionId());
        return response;
    }
}
