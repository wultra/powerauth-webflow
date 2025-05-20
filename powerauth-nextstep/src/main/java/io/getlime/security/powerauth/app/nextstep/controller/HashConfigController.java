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
import io.getlime.security.powerauth.app.nextstep.service.HashConfigService;
import io.getlime.security.powerauth.lib.nextstep.model.exception.HashConfigAlreadyExistsException;
import io.getlime.security.powerauth.lib.nextstep.model.exception.HashConfigNotFoundException;
import io.getlime.security.powerauth.lib.nextstep.model.exception.InvalidConfigurationException;
import io.getlime.security.powerauth.lib.nextstep.model.exception.InvalidRequestException;
import io.getlime.security.powerauth.lib.nextstep.model.request.CreateHashConfigRequest;
import io.getlime.security.powerauth.lib.nextstep.model.request.DeleteHashConfigRequest;
import io.getlime.security.powerauth.lib.nextstep.model.request.GetHashConfigListRequest;
import io.getlime.security.powerauth.lib.nextstep.model.request.UpdateHashConfigRequest;
import io.getlime.security.powerauth.lib.nextstep.model.response.CreateHashConfigResponse;
import io.getlime.security.powerauth.lib.nextstep.model.response.DeleteHashConfigResponse;
import io.getlime.security.powerauth.lib.nextstep.model.response.GetHashConfigListResponse;
import io.getlime.security.powerauth.lib.nextstep.model.response.UpdateHashConfigResponse;
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
 * REST controller for hashing configurations.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@RestController
@RequestMapping("hashconfig")
@Validated
public class HashConfigController {

    private static final Logger logger = LoggerFactory.getLogger(HashConfigController.class);

    private final HashConfigService hashConfigService;

    /**
     * REST controller constructor.
     * @param hashConfigService Hashing configuration service.
     */
    @Autowired
    public HashConfigController(HashConfigService hashConfigService) {
        this.hashConfigService = hashConfigService;
    }

    /**
     * Create a hashing configuration.
     * @param request Create hashing configuration request.
     * @return Create hashing configuration response.
     * @throws InvalidRequestException Thrown when request is invalid.
     * @throws HashConfigAlreadyExistsException Thrown when hashing configuration already exists.
     */
    @Operation(summary = "Create a hashing configuration")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Hashing configuration was created"),
            @ApiResponse(responseCode = "400", description = "Invalid request, error codes: REQUEST_VALIDATION_FAILED, INVALID_REQUEST, HASHING_CONFIG_ALREADY_EXISTS"),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
    })
    @PostMapping
    public ObjectResponse<CreateHashConfigResponse> createHashConfig(@Valid @RequestBody ObjectRequest<CreateHashConfigRequest> request) throws InvalidRequestException, HashConfigAlreadyExistsException {
        logger.info("Received createHashConfig request, hash config name: {}", request.getRequestObject().getHashConfigName());
        final CreateHashConfigResponse response = hashConfigService.createHashConfig(request.getRequestObject());
        logger.info("The createHashConfig request succeeded, hash config name: {}", request.getRequestObject().getHashConfigName());
        return new ObjectResponse<>(response);
    }

    /**
     * Update a hashing configuration via PUT method.
     * @param request Update hashing configuration request.
     * @return Update hashing configuration response.
     * @throws HashConfigNotFoundException Thrown when hashing configuration is not found.
     * @throws InvalidRequestException Thrown when request is invalid.
     */
    @Operation(summary = "Update a hashing configuration")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Hashing configuration was updated"),
            @ApiResponse(responseCode = "400", description = "Invalid request, error codes: REQUEST_VALIDATION_FAILED, HASHING_CONFIG_NOT_FOUND, INVALID_REQUEST"),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
    })
    @PutMapping
    public ObjectResponse<UpdateHashConfigResponse> updateHashConfig(@Valid @RequestBody ObjectRequest<UpdateHashConfigRequest> request) throws HashConfigNotFoundException, InvalidRequestException {
        logger.info("Received updateHashConfig request, hash config name: {}", request.getRequestObject().getHashConfigName());
        final UpdateHashConfigResponse response = hashConfigService.updateHashConfig(request.getRequestObject());
        logger.info("The updateHashConfig request succeeded, hash config name: {}", request.getRequestObject().getHashConfigName());
        return new ObjectResponse<>(response);
    }

    /**
     * Update a hashing configuration via POST method.
     * @param request Update hashing configuration request.
     * @return Update hashing configuration response.
     * @throws HashConfigNotFoundException Thrown when hashing configuration is not found.
     * @throws InvalidRequestException Thrown when request is invalid.
     */
    @Operation(summary = "Update a hashing configuration")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Hashing configuration was updated"),
            @ApiResponse(responseCode = "400", description = "Invalid request, error codes: REQUEST_VALIDATION_FAILED, HASHING_CONFIG_NOT_FOUND, INVALID_REQUEST"),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
    })
    @PostMapping("update")
    public ObjectResponse<UpdateHashConfigResponse> updateHashConfigPost(@Valid @RequestBody ObjectRequest<UpdateHashConfigRequest> request) throws HashConfigNotFoundException, InvalidRequestException {
        logger.info("Received updateHashConfigPost request, hash config name: {}", request.getRequestObject().getHashConfigName());
        final UpdateHashConfigResponse response = hashConfigService.updateHashConfig(request.getRequestObject());
        logger.info("The updateHashConfigPost request succeeded, hash config name: {}", request.getRequestObject().getHashConfigName());
        return new ObjectResponse<>(response);
    }

    /**
     * Get hashing configuration list.
     * @param includeRemoved Whether removed hashing configurations should be included.
     * @return Get hashing configuration list response.
     * @throws InvalidConfigurationException Thrown when Next Step configuration is invalid.
     */
    @Operation(summary = "Get hashing configuration list")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Hashing configuration list sent in response"),
            @ApiResponse(responseCode = "400", description = "Invalid request, error codes: INVALID_CONFIGURATION"),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
    })
    @GetMapping
    public ObjectResponse<GetHashConfigListResponse> getHashConfigList(@RequestParam boolean includeRemoved) throws InvalidConfigurationException {
        logger.info("Received getHashConfigListPost request");
        GetHashConfigListRequest request = new GetHashConfigListRequest();
        request.setIncludeRemoved(includeRemoved);
        final GetHashConfigListResponse response = hashConfigService.getHashConfigList(request);
        logger.info("The getHashConfigListPost request succeeded");
        return new ObjectResponse<>(response);
    }

    /**
     * Get hashing configuration list using POST.
     * @param request Get hashing configuration list request.
     * @return Get hashing configuration list response.
     * @throws InvalidConfigurationException Thrown when Next Step configuration is invalid.
     */
    @Operation(summary = "Get hashing configuration list")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Hashing configuration list sent in response"),
            @ApiResponse(responseCode = "400", description = "Invalid request, error codes: REQUEST_VALIDATION_FAILED, INVALID_CONFIGURATION"),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
    })
    @PostMapping("list")
    public ObjectResponse<GetHashConfigListResponse> getHashConfigListPost(@Valid @RequestBody ObjectRequest<GetHashConfigListRequest> request) throws InvalidConfigurationException {
        logger.info("Received getHashConfigListPost request");
        final GetHashConfigListResponse response = hashConfigService.getHashConfigList(request.getRequestObject());
        logger.info("The getHashConfigListPost request succeeded");
        return new ObjectResponse<>(response);
    }

    /**
     * Delete a hashing configuration.
     * @param request Delete hashing configuration request.
     * @return Delete hashing configuration response.
     * @throws HashConfigNotFoundException Thrown when hashing configuration is not found.
     */
    @Operation(summary = "Delete a hashing configuration")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Hashing configuration was deleted"),
            @ApiResponse(responseCode = "400", description = "Invalid request, error codes: REQUEST_VALIDATION_FAILED, HASHING_CONFIG_NOT_FOUND"),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
    })
    @PostMapping("delete")
    public ObjectResponse<DeleteHashConfigResponse> deleteHashConfig(@Valid @RequestBody ObjectRequest<DeleteHashConfigRequest> request) throws HashConfigNotFoundException {
        logger.info("Received deleteHashConfig request, hash config name: {}", request.getRequestObject().getHashConfigName());
        final DeleteHashConfigResponse response = hashConfigService.deleteHashConfig(request.getRequestObject());
        logger.info("The deleteHashConfig request succeeded, hash config name: {}", request.getRequestObject().getHashConfigName());
        return new ObjectResponse<>(response);
    }

}
