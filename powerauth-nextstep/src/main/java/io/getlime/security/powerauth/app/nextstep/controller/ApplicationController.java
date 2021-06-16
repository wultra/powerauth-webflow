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
package io.getlime.security.powerauth.app.nextstep.controller;

import io.getlime.core.rest.model.base.request.ObjectRequest;
import io.getlime.core.rest.model.base.response.ObjectResponse;
import io.getlime.security.powerauth.app.nextstep.service.ApplicationService;
import io.getlime.security.powerauth.lib.nextstep.model.exception.ApplicationAlreadyExistsException;
import io.getlime.security.powerauth.lib.nextstep.model.exception.ApplicationNotFoundException;
import io.getlime.security.powerauth.lib.nextstep.model.request.CreateApplicationRequest;
import io.getlime.security.powerauth.lib.nextstep.model.request.DeleteApplicationRequest;
import io.getlime.security.powerauth.lib.nextstep.model.request.GetApplicationListRequest;
import io.getlime.security.powerauth.lib.nextstep.model.request.UpdateApplicationRequest;
import io.getlime.security.powerauth.lib.nextstep.model.response.CreateApplicationResponse;
import io.getlime.security.powerauth.lib.nextstep.model.response.DeleteApplicationResponse;
import io.getlime.security.powerauth.lib.nextstep.model.response.GetApplicationListResponse;
import io.getlime.security.powerauth.lib.nextstep.model.response.UpdateApplicationResponse;
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
 * REST controller for Next Step applications.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@RestController
@RequestMapping("application")
@Validated
public class ApplicationController {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationController.class);

    private final ApplicationService applicationService;

    /**
     * REST controller constructor.
     * @param applicationService Application service.
     */
    @Autowired
    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    /**
     * Create an application.
     * @param request Create application request.
     * @return Create application response.
     * @throws ApplicationAlreadyExistsException Thrown when application already exists.
     */
    @Operation(summary = "Create an application")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Application was created"),
            @ApiResponse(responseCode = "400", description = "Invalid request, error codes: REQUEST_VALIDATION_FAILED, APPLICATION_ALREADY_EXISTS"),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
    })
    @RequestMapping(method = RequestMethod.POST)
    public ObjectResponse<CreateApplicationResponse> createApplication(@Valid @RequestBody ObjectRequest<CreateApplicationRequest> request) throws ApplicationAlreadyExistsException {
        logger.info("Received createApplication request, application name: {}", request.getRequestObject().getApplicationName());
        final CreateApplicationResponse response = applicationService.createApplication(request.getRequestObject());
        logger.info("The createApplication request succeeded, application name: {}", request.getRequestObject().getApplicationName());
        return new ObjectResponse<>(response);
    }

    /**
     * Update an application via PUT method.
     * @param request Update application request.
     * @return Update application response.
     * @throws ApplicationNotFoundException Thrown when application is not found.
     */
    @Operation(summary = "Update an application")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Application was updated"),
            @ApiResponse(responseCode = "400", description = "Invalid request, error codes: REQUEST_VALIDATION_FAILED, APPLICATION_NOT_FOUND"),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
    })
    @RequestMapping(method = RequestMethod.PUT)
    public ObjectResponse<UpdateApplicationResponse> updateApplication(@Valid @RequestBody ObjectRequest<UpdateApplicationRequest> request) throws ApplicationNotFoundException {
        logger.info("Received updateApplication request, application name: {}", request.getRequestObject().getApplicationName());
        final UpdateApplicationResponse response = applicationService.updateApplication(request.getRequestObject());
        logger.info("The updateApplication request succeeded, application name: {}", request.getRequestObject().getApplicationName());
        return new ObjectResponse<>(response);
    }

    /**
     * Update an application via POST method.
     * @param request Update application request.
     * @return Update application response.
     * @throws ApplicationNotFoundException Thrown when application is not found.
     */
    @Operation(summary = "Update an application")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Application was updated"),
            @ApiResponse(responseCode = "400", description = "Invalid request, error codes: REQUEST_VALIDATION_FAILED, APPLICATION_NOT_FOUND"),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
    })
    @RequestMapping(value = "update", method = RequestMethod.POST)
    public ObjectResponse<UpdateApplicationResponse> updateApplicationPost(@Valid @RequestBody ObjectRequest<UpdateApplicationRequest> request) throws ApplicationNotFoundException {
        logger.info("Received updateApplicationPost request, application name: {}", request.getRequestObject().getApplicationName());
        final UpdateApplicationResponse response = applicationService.updateApplication(request.getRequestObject());
        logger.info("The updateApplicationPost request succeeded, application name: {}", request.getRequestObject().getApplicationName());
        return new ObjectResponse<>(response);
    }

    /**
     * Get application list.
     * @param includeRemoved Whether removed applications should be included.
     * @return Get application list response.
     */
    @Operation(summary = "Get application list")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Application list"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
    })
    @RequestMapping(method = RequestMethod.GET)
    public ObjectResponse<GetApplicationListResponse> getApplicationList(@RequestParam boolean includeRemoved) {
        logger.info("Received getApplicationList request");
        GetApplicationListRequest request = new GetApplicationListRequest();
        request.setIncludeRemoved(includeRemoved);
        final GetApplicationListResponse response = applicationService.getApplicationList(request);
        logger.info("The getApplicationList request succeeded");
        return new ObjectResponse<>(response);
    }

    /**
     * Get application list using POST method.
     * @param request Get application list request.
     * @return Get application list response.
     */
    @Operation(summary = "Get application list")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Application list sent in response"),
            @ApiResponse(responseCode = "400", description = "Invalid request, error codes: REQUEST_VALIDATION_FAILED"),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
    })
    @RequestMapping(value = "list", method = RequestMethod.POST)
    public ObjectResponse<GetApplicationListResponse> getApplicationListPost(@Valid @RequestBody ObjectRequest<GetApplicationListRequest> request) {
        logger.info("Received getApplicationListPost request");
        final GetApplicationListResponse response = applicationService.getApplicationList(request.getRequestObject());
        logger.info("The getApplicationListPost request succeeded");
        return new ObjectResponse<>(response);
    }

    /**
     * Delete an application.
     * @param request Delete application request.
     * @return Delete application response.
     * @throws ApplicationNotFoundException Thrown when application is not found.
     */
    @Operation(summary = "Delete an application")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Application was deleted"),
            @ApiResponse(responseCode = "400", description = "Invalid request, error codes: REQUEST_VALIDATION_FAILED, APPLICATION_NOT_FOUND"),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
    })
    @RequestMapping(value = "delete", method = RequestMethod.POST)
    public ObjectResponse<DeleteApplicationResponse> deleteApplication(@Valid @RequestBody ObjectRequest<DeleteApplicationRequest> request) throws ApplicationNotFoundException {
        logger.info("Received deleteApplication request, application name: {}", request.getRequestObject().getApplicationName());
        final DeleteApplicationResponse response = applicationService.deleteApplication(request.getRequestObject());
        logger.info("The deleteApplication request succeeded, application name: {}", request.getRequestObject().getApplicationName());
        return new ObjectResponse<>(response);
    }

}
