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
import io.getlime.security.powerauth.app.nextstep.service.CredentialDefinitionService;
import io.getlime.security.powerauth.lib.nextstep.model.exception.*;
import io.getlime.security.powerauth.lib.nextstep.model.request.CreateCredentialDefinitionRequest;
import io.getlime.security.powerauth.lib.nextstep.model.request.DeleteCredentialDefinitionRequest;
import io.getlime.security.powerauth.lib.nextstep.model.request.GetCredentialDefinitionListRequest;
import io.getlime.security.powerauth.lib.nextstep.model.request.UpdateCredentialDefinitionRequest;
import io.getlime.security.powerauth.lib.nextstep.model.response.CreateCredentialDefinitionResponse;
import io.getlime.security.powerauth.lib.nextstep.model.response.DeleteCredentialDefinitionResponse;
import io.getlime.security.powerauth.lib.nextstep.model.response.GetCredentialDefinitionListResponse;
import io.getlime.security.powerauth.lib.nextstep.model.response.UpdateCredentialDefinitionResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * REST controller for credential definitions.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@RestController
@RequestMapping("credential/definition")
@Validated
public class CredentialDefinitionController {

    private static final Logger logger = LoggerFactory.getLogger(CredentialDefinitionController.class);

    private final CredentialDefinitionService credentialDefinitionService;

    /**
     * REST controller constructor.
     * @param credentialDefinitionService Credential definition service.
     */
    @Autowired
    public CredentialDefinitionController(CredentialDefinitionService credentialDefinitionService) {
        this.credentialDefinitionService = credentialDefinitionService;
    }

    /**
     * Create a credential definition.
     * @param request Create credential definition request.
     * @return Create credential definition response.
     * @throws CredentialDefinitionAlreadyExistsException Thrown when credential definition already exists.
     * @throws ApplicationNotFoundException Thrown when application is not found.
     * @throws HashConfigNotFoundException Thrown when hashing configuration is not found.
     * @throws CredentialPolicyNotFoundException Thrown when credential policy is not found.
     * @throws OrganizationNotFoundException Thrown when organization is not found.
     */
    @Operation(summary = "Create a credential definition")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Credential definition was created"),
            @ApiResponse(responseCode = "400", description = "Invalid request, error codes: REQUEST_VALIDATION_FAILED, CREDENTIAL_DEFINITION_ALREADY_EXISTS, APPLICATION_NOT_FOUND, HASHING_CONFIG_NOT_FOUND, CREDENTIAL_POLICY_NOT_FOUND, ORGANIZATION_NOT_FOUND"),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
    })
    @RequestMapping(method = RequestMethod.POST)
    public ObjectResponse<CreateCredentialDefinitionResponse> createCredentialDefinition(@Valid @RequestBody ObjectRequest<CreateCredentialDefinitionRequest> request) throws CredentialDefinitionAlreadyExistsException, ApplicationNotFoundException, HashConfigNotFoundException, CredentialPolicyNotFoundException, OrganizationNotFoundException {
        logger.info("Received createCredentialDefinition request, credential definition name: {}", request.getRequestObject().getCredentialDefinitionName());
        final CreateCredentialDefinitionResponse response = credentialDefinitionService.createCredentialDefinition(request.getRequestObject());
        logger.info("The createCredentialDefinition request succeeded, credential definition name: {}", request.getRequestObject().getCredentialDefinitionName());
        return new ObjectResponse<>(response);
    }

    /**
     * Update a credential definition via PUT method.
     * @param request Update credential definition request.
     * @return Update credential definition response.
     * @throws CredentialDefinitionNotFoundException Thrown when credential definition is not found.
     * @throws ApplicationNotFoundException Thrown when application is not found.
     * @throws HashConfigNotFoundException Thrown when hashing configuration is not found.
     * @throws CredentialPolicyNotFoundException Thrown when credential policy is not found.
     * @throws OrganizationNotFoundException Thrown when organization is not found.
     */
    @Operation(summary = "Update a credential definition")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Credential definition was updated"),
            @ApiResponse(responseCode = "400", description = "Invalid request, error codes: REQUEST_VALIDATION_FAILED, CREDENTIAL_DEFINITION_NOT_FOUND, APPLICATION_NOT_FOUND, HASHING_CONFIG_NOT_FOUND, CREDENTIAL_POLICY_NOT_FOUND, ORGANIZATION_NOT_FOUND"),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
    })
    @RequestMapping(method = RequestMethod.PUT)
    public ObjectResponse<UpdateCredentialDefinitionResponse> updateCredentialDefinition(@Valid @RequestBody ObjectRequest<UpdateCredentialDefinitionRequest> request) throws CredentialDefinitionNotFoundException, ApplicationNotFoundException, HashConfigNotFoundException, CredentialPolicyNotFoundException, OrganizationNotFoundException {
        logger.info("Received updateCredentialDefinition request, credential definition name: {}", request.getRequestObject().getCredentialDefinitionName());
        final UpdateCredentialDefinitionResponse response = credentialDefinitionService.updateCredentialDefinition(request.getRequestObject());
        logger.info("The updateCredentialDefinition request succeeded, credential definition name: {}", request.getRequestObject().getCredentialDefinitionName());
        return new ObjectResponse<>(response);
    }

    /**
     * Update a credential definition via POST method.
     * @param request Update credential definition request.
     * @return Update credential definition response.
     * @throws CredentialDefinitionNotFoundException Thrown when credential definition is not found.
     * @throws ApplicationNotFoundException Thrown when application is not found.
     * @throws HashConfigNotFoundException Thrown when hashing configuration is not found.
     * @throws CredentialPolicyNotFoundException Thrown when credential policy is not found.
     * @throws OrganizationNotFoundException Thrown when organization is not found.
     */
    @Operation(summary = "Update a credential definition")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Credential definition was updated"),
            @ApiResponse(responseCode = "400", description = "Invalid request, error codes: REQUEST_VALIDATION_FAILED, CREDENTIAL_DEFINITION_NOT_FOUND, APPLICATION_NOT_FOUND, HASHING_CONFIG_NOT_FOUND, CREDENTIAL_POLICY_NOT_FOUND, ORGANIZATION_NOT_FOUND"),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
    })
    @RequestMapping(value = "update", method = RequestMethod.POST)
    public ObjectResponse<UpdateCredentialDefinitionResponse> updateCredentialDefinitionPost(@Valid @RequestBody ObjectRequest<UpdateCredentialDefinitionRequest> request) throws CredentialDefinitionNotFoundException, ApplicationNotFoundException, HashConfigNotFoundException, CredentialPolicyNotFoundException, OrganizationNotFoundException {
        logger.info("Received updateCredentialDefinitionPost request, credential definition name: {}", request.getRequestObject().getCredentialDefinitionName());
        final UpdateCredentialDefinitionResponse response = credentialDefinitionService.updateCredentialDefinition(request.getRequestObject());
        logger.info("The updateCredentialDefinitionPost request succeeded, credential definition name: {}", request.getRequestObject().getCredentialDefinitionName());
        return new ObjectResponse<>(response);
    }

    /**
     * Get credential definition list.
     * @param includeRemoved Whether removed credentials should be included.
     * @return Get credential definition list response.
     */
    @Operation(summary = "Get credential definition list")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Credential definition list sent in response"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
    })
    @RequestMapping(method = RequestMethod.GET)
    public ObjectResponse<GetCredentialDefinitionListResponse> getCredentialDefinitionList(@RequestParam boolean includeRemoved) {
        GetCredentialDefinitionListRequest request = new GetCredentialDefinitionListRequest();
        request.setIncludeRemoved(includeRemoved);
        logger.info("Received getCredentialDefinitionList request");
        final GetCredentialDefinitionListResponse response = credentialDefinitionService.getCredentialDefinitionList(request);
        logger.info("The getCredentialDefinitionList request succeeded");
        return new ObjectResponse<>(response);
    }

    /**
     * Get credential definition list using POST method.
     * @param request Get credential definition list request.
     * @return Get credential definition list response.
     */
    @Operation(summary = "Get credential definition list")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Credential definition list sent in response"),
            @ApiResponse(responseCode = "400", description = "Invalid request, error codes: REQUEST_VALIDATION_FAILED"),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
    })
    @RequestMapping(value = "list", method = RequestMethod.POST)
    public ObjectResponse<GetCredentialDefinitionListResponse> getCredentialDefinitionListPost(@Valid @RequestBody ObjectRequest<GetCredentialDefinitionListRequest> request) {
        logger.info("Received getCredentialDefinitionListPost request");
        final GetCredentialDefinitionListResponse response = credentialDefinitionService.getCredentialDefinitionList(request.getRequestObject());
        logger.info("The getCredentialDefinitionListPost request succeeded");
        return new ObjectResponse<>(response);
    }

    /**
     * Delete a credential definition.
     * @param request Delete credential definition request.
     * @return Delete credential definition response.
     * @throws CredentialDefinitionNotFoundException Thrown when credential definition is not found.
     */
    @Operation(summary = "Delete a credential definition")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Credential definition was deleted"),
            @ApiResponse(responseCode = "400", description = "Invalid request, error codes: REQUEST_VALIDATION_FAILED, CREDENTIAL_DEFINITION_NOT_FOUND"),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
    })
    @RequestMapping(value = "delete", method = RequestMethod.POST)
    public ObjectResponse<DeleteCredentialDefinitionResponse> deleteCredentialDefinition(@Valid @RequestBody ObjectRequest<DeleteCredentialDefinitionRequest> request) throws CredentialDefinitionNotFoundException {
        logger.info("Received deleteCredentialDefinition request, credential definition name: {}", request.getRequestObject().getCredentialDefinitionName());
        final DeleteCredentialDefinitionResponse response = credentialDefinitionService.deleteCredentialDefinition(request.getRequestObject());
        logger.info("The deleteCredentialDefinition request succeeded, credential definition name: {}", request.getRequestObject().getCredentialDefinitionName());
        return new ObjectResponse<>(response);
    }

}
