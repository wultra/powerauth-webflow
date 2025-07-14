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

import com.wultra.core.rest.model.base.request.ObjectRequest;
import com.wultra.core.rest.model.base.response.ObjectResponse;
import io.getlime.security.powerauth.app.nextstep.service.CredentialPolicyService;
import io.getlime.security.powerauth.lib.nextstep.model.exception.CredentialPolicyAlreadyExistsException;
import io.getlime.security.powerauth.lib.nextstep.model.exception.CredentialPolicyNotFoundException;
import io.getlime.security.powerauth.lib.nextstep.model.exception.InvalidConfigurationException;
import io.getlime.security.powerauth.lib.nextstep.model.exception.InvalidRequestException;
import io.getlime.security.powerauth.lib.nextstep.model.request.CreateCredentialPolicyRequest;
import io.getlime.security.powerauth.lib.nextstep.model.request.DeleteCredentialPolicyRequest;
import io.getlime.security.powerauth.lib.nextstep.model.request.GetCredentialPolicyListRequest;
import io.getlime.security.powerauth.lib.nextstep.model.request.UpdateCredentialPolicyRequest;
import io.getlime.security.powerauth.lib.nextstep.model.response.CreateCredentialPolicyResponse;
import io.getlime.security.powerauth.lib.nextstep.model.response.DeleteCredentialPolicyResponse;
import io.getlime.security.powerauth.lib.nextstep.model.response.GetCredentialPolicyListResponse;
import io.getlime.security.powerauth.lib.nextstep.model.response.UpdateCredentialPolicyResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for credential policy definitions.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@RestController
@RequestMapping("credential/policy")
@Validated
public class CredentialPolicyController {

    private static final Logger logger = LoggerFactory.getLogger(CredentialPolicyController.class);

    private final CredentialPolicyService credentialPolicyService;

    /**
     * REST controller constructor.
     * @param credentialPolicyService Credential policy service.
     */
    @Autowired
    public CredentialPolicyController(CredentialPolicyService credentialPolicyService) {
        this.credentialPolicyService = credentialPolicyService;
    }

    /**
     * Create a credential policy.
     * @param request Create credential policy request.
     * @return Create credential policy response.
     * @throws CredentialPolicyAlreadyExistsException Thrown when credential policy already exists.
     * @throws InvalidRequestException Thrown when request is invalid.
     */
    @Operation(summary = "Create a credential policy")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Credential policy was created"),
            @ApiResponse(responseCode = "400", description = "Invalid request, error codes: REQUEST_VALIDATION_FAILED, INVALID_REQUEST, CREDENTIAL_POLICY_ALREADY_EXISTS"),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
    })
    @PostMapping
    public ObjectResponse<CreateCredentialPolicyResponse> createCredentialPolicy(@Valid @RequestBody ObjectRequest<CreateCredentialPolicyRequest> request) throws CredentialPolicyAlreadyExistsException, InvalidRequestException {
        logger.info("Received createCredentialPolicy request, credential policy name: {}", request.getRequestObject().getCredentialPolicyName());
        final CreateCredentialPolicyResponse response = credentialPolicyService.createCredentialPolicy(request.getRequestObject());
        logger.info("The createCredentialPolicy request succeeded, credential policy name: {}", request.getRequestObject().getCredentialPolicyName());
        return new ObjectResponse<>(response);
    }

    /**
     * Update a credential policy via PUT method.
     * @param request Update credential policy request.
     * @return Update credential policy response.
     * @throws CredentialPolicyNotFoundException Thrown when credential policy is not found.
     * @throws InvalidRequestException Thrown when request is invalid.
     */
    @Operation(summary = "Update a credential policy")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Credential policy was updated"),
            @ApiResponse(responseCode = "400", description = "Invalid request, error codes: REQUEST_VALIDATION_FAILED, CREDENTIAL_POLICY_NOT_FOUND, INVALID_REQUEST"),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
    })
    @PutMapping
    public ObjectResponse<UpdateCredentialPolicyResponse> updateCredentialPolicy(@Valid @RequestBody ObjectRequest<UpdateCredentialPolicyRequest> request) throws CredentialPolicyNotFoundException, InvalidRequestException {
        logger.info("Received updateCredentialPolicy request, credential policy name: {}", request.getRequestObject().getCredentialPolicyName());
        final UpdateCredentialPolicyResponse response = credentialPolicyService.updateCredentialPolicy(request.getRequestObject());
        logger.info("The updateCredentialPolicy request succeeded, credential policy name: {}", request.getRequestObject().getCredentialPolicyName());
        return new ObjectResponse<>(response);
    }

    /**
     * Update a credential policy via POST method.
     * @param request Update credential policy request.
     * @return Update credential policy response.
     * @throws CredentialPolicyNotFoundException Thrown when credential policy is not found.
     * @throws InvalidRequestException Thrown when request is invalid.
     */
    @Operation(summary = "Update a credential policy")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Credential policy was updated"),
            @ApiResponse(responseCode = "400", description = "Invalid request, error codes: REQUEST_VALIDATION_FAILED, CREDENTIAL_POLICY_NOT_FOUND, INVALID_REQUEST"),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
    })
    @PostMapping("update")
    public ObjectResponse<UpdateCredentialPolicyResponse> updateCredentialPolicyPost(@Valid @RequestBody ObjectRequest<UpdateCredentialPolicyRequest> request) throws CredentialPolicyNotFoundException, InvalidRequestException {
        logger.info("Received updateCredentialPolicyPost request, credential policy name: {}", request.getRequestObject().getCredentialPolicyName());
        final UpdateCredentialPolicyResponse response = credentialPolicyService.updateCredentialPolicy(request.getRequestObject());
        logger.info("The updateCredentialPolicyPost request succeeded, credential policy name: {}", request.getRequestObject().getCredentialPolicyName());
        return new ObjectResponse<>(response);
    }

    /**
     * Get credential policy list.
     * @param includeRemoved Whether removed credential policies should be included.
     * @return Get credential policy list response.
     * @throws InvalidConfigurationException Thrown when Next Step configuration is invalid.
     */
    @Operation(summary = "Get credential policy list")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Credential policy list sent in response"),
            @ApiResponse(responseCode = "400", description = "Invalid request, error codes: INVALID_CONFIGURATION"),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
    })
    @GetMapping
    public ObjectResponse<GetCredentialPolicyListResponse> getCredentialPolicyList(@RequestParam boolean includeRemoved) throws InvalidConfigurationException {
        logger.info("Received getCredentialPolicyList request");
        GetCredentialPolicyListRequest request = new GetCredentialPolicyListRequest();
        request.setIncludeRemoved(includeRemoved);
        final GetCredentialPolicyListResponse response = credentialPolicyService.getCredentialPolicyList(request);
        logger.info("The getCredentialPolicyList request succeeded");
        return new ObjectResponse<>(response);
    }

    /**
     * Get credential policy list using POST method.
     * @param request Get credential policy list request.
     * @return Get credential policy list response.
     * @throws InvalidConfigurationException Thrown when Next Step configuration is invalid.
     */
    @Operation(summary = "Get credential policy list")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Credential policy list sent in response"),
            @ApiResponse(responseCode = "400", description = "Invalid request, error codes: REQUEST_VALIDATION_FAILED, INVALID_CONFIGURATION"),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
    })
    @PostMapping("list")
    public ObjectResponse<GetCredentialPolicyListResponse> getCredentialPolicyListPost(@Valid @RequestBody ObjectRequest<GetCredentialPolicyListRequest> request) throws InvalidConfigurationException {
        logger.info("Received getCredentialPolicyListPost request");
        final GetCredentialPolicyListResponse response = credentialPolicyService.getCredentialPolicyList(request.getRequestObject());
        logger.info("The getCredentialPolicyListPost request succeeded");
        return new ObjectResponse<>(response);
    }

    /**
     * Delete a credential policy.
     * @param request Delete credential policy request.
     * @return Delete credential policy response.
     * @throws CredentialPolicyNotFoundException Thrown when credential policy is not found.
     */
    @Operation(summary = "Delete a credential policy")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Credential policy was deleted"),
            @ApiResponse(responseCode = "400", description = "Invalid request, error codes: REQUEST_VALIDATION_FAILED, CREDENTIAL_POLICY_NOT_FOUND"),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
    })
    @PostMapping("delete")
    public ObjectResponse<DeleteCredentialPolicyResponse> deleteCredentialPolicy(@Valid @RequestBody ObjectRequest<DeleteCredentialPolicyRequest> request) throws CredentialPolicyNotFoundException {
        logger.info("Received deleteCredentialPolicy request, credential policy name: {}", request.getRequestObject().getCredentialPolicyName());
        final DeleteCredentialPolicyResponse response = credentialPolicyService.deleteCredentialPolicy(request.getRequestObject());
        logger.info("The deleteCredentialPolicy request succeeded, credential policy name: {}", request.getRequestObject().getCredentialPolicyName());
        return new ObjectResponse<>(response);
    }

}
