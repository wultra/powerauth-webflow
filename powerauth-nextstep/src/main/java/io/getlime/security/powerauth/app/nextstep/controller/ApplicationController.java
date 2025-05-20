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
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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
    @PostMapping
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
    @PutMapping
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
    @PostMapping("update")
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
    @GetMapping
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
    @PostMapping("list")
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
    @PostMapping("delete")
    public ObjectResponse<DeleteApplicationResponse> deleteApplication(@Valid @RequestBody ObjectRequest<DeleteApplicationRequest> request) throws ApplicationNotFoundException {
        logger.info("Received deleteApplication request, application name: {}", request.getRequestObject().getApplicationName());
        final DeleteApplicationResponse response = applicationService.deleteApplication(request.getRequestObject());
        logger.info("The deleteApplication request succeeded, application name: {}", request.getRequestObject().getApplicationName());
        return new ObjectResponse<>(response);
    }

}
