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

package io.getlime.security.powerauth.app.nextstep.controller;

import io.getlime.core.rest.model.base.request.ObjectRequest;
import io.getlime.core.rest.model.base.response.ObjectResponse;
import io.getlime.security.powerauth.app.nextstep.service.StepDefinitionService;
import io.getlime.security.powerauth.lib.nextstep.model.exception.StepDefinitionAlreadyExistsException;
import io.getlime.security.powerauth.lib.nextstep.model.exception.StepDefinitionNotFoundException;
import io.getlime.security.powerauth.lib.nextstep.model.request.CreateStepDefinitionRequest;
import io.getlime.security.powerauth.lib.nextstep.model.request.DeleteStepDefinitionRequest;
import io.getlime.security.powerauth.lib.nextstep.model.response.CreateStepDefinitionResponse;
import io.getlime.security.powerauth.lib.nextstep.model.response.DeleteStepDefinitionResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * REST controller class related to step definitions.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@RestController
@RequestMapping(value = "step/definition")
@Validated
public class StepDefinitionController {

    private static final Logger logger = LoggerFactory.getLogger(StepDefinitionController.class);

    private final StepDefinitionService stepDefinitionService;

    /**
     * REST controller constructor.
     * @param stepDefinitionService Step definition service.
     */
    @Autowired
    public StepDefinitionController(StepDefinitionService stepDefinitionService) {
        this.stepDefinitionService = stepDefinitionService;
    }

    /**
     * Create a step definition.
     * @param request Create step definition request.
     * @return Create step definition response.
     * @throws StepDefinitionAlreadyExistsException Thrown when step definition already exists.
     */
    @Operation(summary = "Create a step definition")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Step definition was created"),
            @ApiResponse(responseCode = "400", description = "Invalid request, error codes: REQUEST_VALIDATION_FAILED, STEP_DEFINITION_ALREADY_EXISTS"),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
    })
    @RequestMapping(method = RequestMethod.POST)
    public ObjectResponse<CreateStepDefinitionResponse> createStepDefinition(@Valid @RequestBody ObjectRequest<CreateStepDefinitionRequest> request) throws StepDefinitionAlreadyExistsException {
        logger.info("Received createStepDefinition request, step definition ID: {}", request.getRequestObject().getStepDefinitionId());
        final CreateStepDefinitionResponse response = stepDefinitionService.createStepDefinition(request.getRequestObject());
        logger.info("The createStepDefinition request succeeded, step definition ID: {}", request.getRequestObject().getStepDefinitionId());
        return new ObjectResponse<>(response);
    }

    /**
     * Delete a step definition.
     * @param request Delete step definition request.
     * @return Delete step definition response.
     * @throws StepDefinitionNotFoundException Thrown when step definition is not found.
     */
    @Operation(summary = "Delete a step definition")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Step definition was deleted"),
            @ApiResponse(responseCode = "400", description = "Invalid request, error codes: REQUEST_VALIDATION_FAILED, STEP_DEFINITION_NOT_FOUND"),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
    })
    @RequestMapping(value = "delete", method = RequestMethod.POST)
    public ObjectResponse<DeleteStepDefinitionResponse> deleteStepDefinition(@Valid @RequestBody ObjectRequest<DeleteStepDefinitionRequest> request) throws StepDefinitionNotFoundException {
        logger.info("Received deleteStepDefinition request, step definition ID: {}", request.getRequestObject().getStepDefinitionId());
        final DeleteStepDefinitionResponse response = stepDefinitionService.deleteStepDefinition(request.getRequestObject());
        logger.info("The deleteStepDefinition request succeeded, step definition ID: {}", request.getRequestObject().getStepDefinitionId());
        return new ObjectResponse<>(response);
    }

}
