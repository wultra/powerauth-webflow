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
import io.getlime.security.powerauth.app.nextstep.service.OtpDefinitionService;
import io.getlime.security.powerauth.lib.nextstep.model.exception.ApplicationNotFoundException;
import io.getlime.security.powerauth.lib.nextstep.model.exception.OtpDefinitionAlreadyExistsException;
import io.getlime.security.powerauth.lib.nextstep.model.exception.OtpDefinitionNotFoundException;
import io.getlime.security.powerauth.lib.nextstep.model.exception.OtpPolicyNotFoundException;
import io.getlime.security.powerauth.lib.nextstep.model.request.CreateOtpDefinitionRequest;
import io.getlime.security.powerauth.lib.nextstep.model.request.DeleteOtpDefinitionRequest;
import io.getlime.security.powerauth.lib.nextstep.model.request.GetOtpDefinitionListRequest;
import io.getlime.security.powerauth.lib.nextstep.model.request.UpdateOtpDefinitionRequest;
import io.getlime.security.powerauth.lib.nextstep.model.response.CreateOtpDefinitionResponse;
import io.getlime.security.powerauth.lib.nextstep.model.response.DeleteOtpDefinitionResponse;
import io.getlime.security.powerauth.lib.nextstep.model.response.GetOtpDefinitionListResponse;
import io.getlime.security.powerauth.lib.nextstep.model.response.UpdateOtpDefinitionResponse;
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
 * REST controller for OTP definitions.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@RestController
@RequestMapping("otp/definition")
@Validated
public class OtpDefinitionController {

    private static final Logger logger = LoggerFactory.getLogger(OtpDefinitionController.class);

    private final OtpDefinitionService otpDefinitionService;

    /**
     * REST controller constructor.
     * @param otpDefinitionService OTP definition service.
     */
    @Autowired
    public OtpDefinitionController(OtpDefinitionService otpDefinitionService) {
        this.otpDefinitionService = otpDefinitionService;
    }

    /**
     * Create an OTP definition.
     * @param request Create OTP definition request.
     * @return Create OTP definition response.
     * @throws OtpDefinitionAlreadyExistsException Thrown when OTP definition already exists.
     * @throws ApplicationNotFoundException Thrown when application is not found.
     * @throws OtpPolicyNotFoundException Thrown when OTP policy is not found.
     */
    @Operation(summary = "Create an OTP definition")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OTP definition was created"),
            @ApiResponse(responseCode = "400", description = "Invalid request, error codes: REQUEST_VALIDATION_FAILED, OTP_DEFINITION_ALREADY_EXISTS, APPLICATION_NOT_FOUND, OTP_POLICY_NOT_FOUND"),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
    })
    @RequestMapping(method = RequestMethod.POST)
    public ObjectResponse<CreateOtpDefinitionResponse> createOtpDefinition(@Valid @RequestBody ObjectRequest<CreateOtpDefinitionRequest> request) throws OtpDefinitionAlreadyExistsException, ApplicationNotFoundException, OtpPolicyNotFoundException {
        logger.info("Received createOtpDefinition request, OTP definition name: {}", request.getRequestObject().getOtpDefinitionName());
        final CreateOtpDefinitionResponse response = otpDefinitionService.createOtpDefinition(request.getRequestObject());
        logger.info("The createOtpDefinition request succeeded, OTP definition name: {}", request.getRequestObject().getOtpDefinitionName());
        return new ObjectResponse<>(response);
    }

    /**
     * Update an OTP definition via PUT method.
     * @param request Update OTP definition request.
     * @return Update OTP definition response.
     * @throws OtpDefinitionNotFoundException Thrown when OTP definition is not found.
     * @throws ApplicationNotFoundException Thrown when application is not found.
     * @throws OtpPolicyNotFoundException Thrown when OTP policy is not found.
     */
    @Operation(summary = "Update an OTP definition")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OTP definition was updated"),
            @ApiResponse(responseCode = "400", description = "Invalid request, error codes: REQUEST_VALIDATION_FAILED, OTP_DEFINITION_NOT_FOUND, APPLICATION_NOT_FOUND, OTP_POLICY_NOT_FOUND"),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
    })
    @RequestMapping(method = RequestMethod.PUT)
    public ObjectResponse<UpdateOtpDefinitionResponse> updateOtpDefinition(@Valid @RequestBody ObjectRequest<UpdateOtpDefinitionRequest> request) throws OtpDefinitionNotFoundException, ApplicationNotFoundException, OtpPolicyNotFoundException {
        logger.info("Received updateOtpDefinition request, OTP definition name: {}", request.getRequestObject().getOtpDefinitionName());
        final UpdateOtpDefinitionResponse response = otpDefinitionService.updateOtpDefinition(request.getRequestObject());
        logger.info("The updateOtpDefinition request succeeded, OTP definition name: {}", request.getRequestObject().getOtpDefinitionName());
        return new ObjectResponse<>(response);
    }

    /**
     * Update an OTP definition via POST method.
     * @param request Update OTP definition request.
     * @return Update OTP definition response.
     * @throws OtpDefinitionNotFoundException Thrown when OTP definition is not found.
     * @throws ApplicationNotFoundException Thrown when application is not found.
     * @throws OtpPolicyNotFoundException Thrown when OTP policy is not found.
     */
    @Operation(summary = "Update an OTP definition")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OTP definition was updated"),
            @ApiResponse(responseCode = "400", description = "Invalid request, error codes: REQUEST_VALIDATION_FAILED, OTP_DEFINITION_NOT_FOUND, APPLICATION_NOT_FOUND, OTP_POLICY_NOT_FOUND"),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
    })
    @RequestMapping(value = "update", method = RequestMethod.POST)
    public ObjectResponse<UpdateOtpDefinitionResponse> updateOtpDefinitionPost(@Valid @RequestBody ObjectRequest<UpdateOtpDefinitionRequest> request) throws OtpDefinitionNotFoundException, ApplicationNotFoundException, OtpPolicyNotFoundException {
        logger.info("Received updateOtpDefinitionPost request, OTP definition name: {}", request.getRequestObject().getOtpDefinitionName());
        final UpdateOtpDefinitionResponse response = otpDefinitionService.updateOtpDefinition(request.getRequestObject());
        logger.info("The updateOtpDefinitionPost request succeeded, OTP definition name: {}", request.getRequestObject().getOtpDefinitionName());
        return new ObjectResponse<>(response);
    }

    /**
     * Get OTP definition list.
     * @param includeRemoved Whether removed OTP definitions should be included.
     * @return Get OTP definition list response.
     */
    @Operation(summary = "Get OTP list")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OTP list sent in response"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
    })
    @RequestMapping(method = RequestMethod.GET)
    public ObjectResponse<GetOtpDefinitionListResponse> getOtpDefinitionList(@RequestParam boolean includeRemoved) {
        logger.info("Received getOtpDefinitionList request");
        GetOtpDefinitionListRequest request = new GetOtpDefinitionListRequest();
        request.setIncludeRemoved(includeRemoved);
        final GetOtpDefinitionListResponse response = otpDefinitionService.getOtpDefinitionList(request);
        logger.info("The getOtpDefinitionList request succeeded");
        return new ObjectResponse<>(response);
    }

    /**
     * Get OTP definition list using POST method.
     * @param request Get OTP definition list request.
     * @return Get OTP definition list response.
     */
    @Operation(summary = "Get OTP definition list")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OTP definition was updated"),
            @ApiResponse(responseCode = "400", description = "Invalid request, error codes: REQUEST_VALIDATION_FAILED"),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
    })
    @RequestMapping(value = "list", method = RequestMethod.POST)
    public ObjectResponse<GetOtpDefinitionListResponse> getOtpDefinitionListPost(@Valid @RequestBody ObjectRequest<GetOtpDefinitionListRequest> request) {
        logger.info("Received getOtpDefinitionListPost request");
        final GetOtpDefinitionListResponse response = otpDefinitionService.getOtpDefinitionList(request.getRequestObject());
        logger.info("The getOtpDefinitionListPost request succeeded");
        return new ObjectResponse<>(response);
    }

    /**
     * Delete an OTP definition.
     * @param request Delete an OTP request.
     * @return Delete an OTP response.
     * @throws OtpDefinitionNotFoundException Thrown when OTP definition is not found.
     */
    @Operation(summary = "Delete an OTP definition")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OTP definition was updated"),
            @ApiResponse(responseCode = "400", description = "Invalid request, error codes: REQUEST_VALIDATION_FAILED, OTP_DEFINITION_NOT_FOUND"),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
    })
    @RequestMapping(value = "delete", method = RequestMethod.POST)
    public ObjectResponse<DeleteOtpDefinitionResponse> deleteOtpDefinition(@Valid @RequestBody ObjectRequest<DeleteOtpDefinitionRequest> request) throws OtpDefinitionNotFoundException {
        logger.info("Received deleteOtpDefinition request, OTP definition name: {}", request.getRequestObject().getOtpDefinitionName());
        final DeleteOtpDefinitionResponse response = otpDefinitionService.deleteOtpDefinition(request.getRequestObject());
        logger.info("The deleteOtpDefinition request succeeded, OTP definition name: {}", request.getRequestObject().getOtpDefinitionName());
        return new ObjectResponse<>(response);
    }

}
