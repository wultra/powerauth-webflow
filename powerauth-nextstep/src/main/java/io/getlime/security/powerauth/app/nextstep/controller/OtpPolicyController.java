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
import io.getlime.security.powerauth.app.nextstep.service.OtpPolicyService;
import io.getlime.security.powerauth.lib.nextstep.model.exception.InvalidConfigurationException;
import io.getlime.security.powerauth.lib.nextstep.model.exception.InvalidRequestException;
import io.getlime.security.powerauth.lib.nextstep.model.exception.OtpPolicyAlreadyExistsException;
import io.getlime.security.powerauth.lib.nextstep.model.exception.OtpPolicyNotFoundException;
import io.getlime.security.powerauth.lib.nextstep.model.request.CreateOtpPolicyRequest;
import io.getlime.security.powerauth.lib.nextstep.model.request.DeleteOtpPolicyRequest;
import io.getlime.security.powerauth.lib.nextstep.model.request.GetOtpPolicyListRequest;
import io.getlime.security.powerauth.lib.nextstep.model.request.UpdateOtpPolicyRequest;
import io.getlime.security.powerauth.lib.nextstep.model.response.CreateOtpPolicyResponse;
import io.getlime.security.powerauth.lib.nextstep.model.response.DeleteOtpPolicyResponse;
import io.getlime.security.powerauth.lib.nextstep.model.response.GetOtpPolicyListResponse;
import io.getlime.security.powerauth.lib.nextstep.model.response.UpdateOtpPolicyResponse;
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
 * REST controller for OTP policy definitions.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@RestController
@RequestMapping("otp/policy")
@Validated
public class OtpPolicyController {

    private static final Logger logger = LoggerFactory.getLogger(OtpPolicyController.class);

    private final OtpPolicyService otpPolicyService;

    /**
     * REST controller constructor.
     * @param otpPolicyService OTP policy service.
     */
    @Autowired
    public OtpPolicyController(OtpPolicyService otpPolicyService) {
        this.otpPolicyService = otpPolicyService;
    }

    /**
     * Create an OTP policy.
     * @param request Create OTP policy request.
     * @return Create OTP policy response.
     * @throws OtpPolicyAlreadyExistsException Thrown when OTP policy already exists.
     * @throws InvalidRequestException Thrown when request is invalid.
     */
    @Operation(summary = "Create an OTP policy")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OTP policy was created"),
            @ApiResponse(responseCode = "400", description = "Invalid request, error codes: REQUEST_VALIDATION_FAILED, OTP_POLICY_ALREADY_EXISTS, INVALID_REQUEST"),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
    })
    @PostMapping
    public ObjectResponse<CreateOtpPolicyResponse> createOtpPolicy(@Valid @RequestBody ObjectRequest<CreateOtpPolicyRequest> request) throws OtpPolicyAlreadyExistsException, InvalidRequestException {
        logger.info("Received createOtpPolicy request, OTP policy name: {}", request.getRequestObject().getOtpPolicyName());
        final CreateOtpPolicyResponse response = otpPolicyService.createOtpPolicy(request.getRequestObject());
        logger.info("The createOtpPolicy request succeeded, OTP policy name: {}", request.getRequestObject().getOtpPolicyName());
        return new ObjectResponse<>(response);
    }

    /**
     * Update an OTP policy via PUT method.
     * @param request Update OTP policy request.
     * @return Update OTP policy response.
     * @throws OtpPolicyNotFoundException Thrown when OTP policy is not found.
     * @throws InvalidRequestException Thrown when request is invalid.
     */
    @Operation(summary = "Update an OTP policy")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OTP policy was updated"),
            @ApiResponse(responseCode = "400", description = "Invalid request, error codes: REQUEST_VALIDATION_FAILED, OTP_POLICY_NOT_FOUND, INVALID_REQUEST"),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
    })
    @PutMapping
    public ObjectResponse<UpdateOtpPolicyResponse> updateOtpPolicy(@Valid @RequestBody ObjectRequest<UpdateOtpPolicyRequest> request) throws OtpPolicyNotFoundException, InvalidRequestException {
        logger.info("Received updateOtpPolicy request, OTP policy name: {}", request.getRequestObject().getOtpPolicyName());
        final UpdateOtpPolicyResponse response = otpPolicyService.updateOtpPolicy(request.getRequestObject());
        logger.info("The updateOtpPolicy request succeeded, OTP policy name: {}", request.getRequestObject().getOtpPolicyName());
        return new ObjectResponse<>(response);
    }

    /**
     * Update an OTP policy via POST method.
     * @param request Update OTP policy request.
     * @return Update OTP policy response.
     * @throws OtpPolicyNotFoundException Thrown when OTP policy is not found.
     * @throws InvalidRequestException Thrown when request is invalid.
     */
    @Operation(summary = "Update an OTP policy")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OTP policy was updated"),
            @ApiResponse(responseCode = "400", description = "Invalid request, error codes: REQUEST_VALIDATION_FAILED, OTP_POLICY_NOT_FOUND, INVALID_REQUEST"),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
    })
    @PostMapping("update")
    public ObjectResponse<UpdateOtpPolicyResponse> updateOtpPolicyPost(@Valid @RequestBody ObjectRequest<UpdateOtpPolicyRequest> request) throws OtpPolicyNotFoundException, InvalidRequestException {
        logger.info("Received updateOtpPolicyPost request, OTP policy name: {}", request.getRequestObject().getOtpPolicyName());
        final UpdateOtpPolicyResponse response = otpPolicyService.updateOtpPolicy(request.getRequestObject());
        logger.info("The updateOtpPolicyPost request succeeded, OTP policy name: {}", request.getRequestObject().getOtpPolicyName());
        return new ObjectResponse<>(response);
    }

    /**
     * Get OTP policy list.
     * @param includeRemoved Whether removed OTP policies should be included.
     * @return Get OTP policy list response.
     * @throws InvalidConfigurationException Thrown when Next Step configuration is invalid.
     */
    @Operation(summary = "Get an OTP policy list")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OTP policy list sent in response"),
            @ApiResponse(responseCode = "400", description = "Invalid request, error codes: INVALID_CONFIGURATION"),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
    })
    @GetMapping
    public ObjectResponse<GetOtpPolicyListResponse> getOtpPolicyList(@RequestParam boolean includeRemoved) throws InvalidConfigurationException {
        logger.info("Received getOtpPolicyListPost request");
        GetOtpPolicyListRequest request = new GetOtpPolicyListRequest();
        request.setIncludeRemoved(includeRemoved);
        final GetOtpPolicyListResponse response = otpPolicyService.getOtpPolicyList(request);
        logger.info("The getOtpPolicyListPost request succeeded");
        return new ObjectResponse<>(response);
    }

    /**
     * Get OTP policy list using POST method.
     * @param request Get OTP policy list request.
     * @return Get OTP policy list response.
     * @throws InvalidConfigurationException Thrown when Next Step configuration is invalid.
     */
    @Operation(summary = "Get an OTP policy list")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OTP policy list sent in response"),
            @ApiResponse(responseCode = "400", description = "Invalid request, error codes: REQUEST_VALIDATION_FAILED, INVALID_CONFIGURATION"),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
    })
    @PostMapping("list")
    public ObjectResponse<GetOtpPolicyListResponse> getOtpPolicyListPost(@Valid @RequestBody ObjectRequest<GetOtpPolicyListRequest> request) throws InvalidConfigurationException {
        logger.info("Received getOtpPolicyListPost request");
        final GetOtpPolicyListResponse response = otpPolicyService.getOtpPolicyList(request.getRequestObject());
        logger.info("The getOtpPolicyListPost request succeeded");
        return new ObjectResponse<>(response);
    }

    /**
     * Delete an OTP policy.
     * @param request Delete OTP policy request.
     * @return Delete OTP policy response.
     * @throws OtpPolicyNotFoundException Thrown when OTP policy is not found.
     */
    @Operation(summary = "Delete an OTP policy")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OTP policy was deleted"),
            @ApiResponse(responseCode = "400", description = "Invalid request, error codes: REQUEST_VALIDATION_FAILED, OTP_POLICY_NOT_FOUND"),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
    })
    @PostMapping("delete")
    public ObjectResponse<DeleteOtpPolicyResponse> deleteOtpPolicy(@Valid @RequestBody ObjectRequest<DeleteOtpPolicyRequest> request) throws OtpPolicyNotFoundException {
        logger.info("Received deleteOtpPolicy request, OTP policy name: {}", request.getRequestObject().getOtpPolicyName());
        final DeleteOtpPolicyResponse response = otpPolicyService.deleteOtpPolicy(request.getRequestObject());
        logger.info("The deleteOtpPolicy request succeeded, OTP policy name: {}", request.getRequestObject().getOtpPolicyName());
        return new ObjectResponse<>(response);
    }

}
