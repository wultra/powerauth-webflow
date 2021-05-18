/*
 * Copyright 2019 Wultra s.r.o.
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
import io.getlime.security.powerauth.app.nextstep.service.OrganizationService;
import io.getlime.security.powerauth.lib.nextstep.model.exception.DeleteNotAllowedException;
import io.getlime.security.powerauth.lib.nextstep.model.exception.OrganizationAlreadyExistsException;
import io.getlime.security.powerauth.lib.nextstep.model.exception.OrganizationNotFoundException;
import io.getlime.security.powerauth.lib.nextstep.model.request.CreateOrganizationRequest;
import io.getlime.security.powerauth.lib.nextstep.model.request.DeleteOrganizationRequest;
import io.getlime.security.powerauth.lib.nextstep.model.request.GetOrganizationDetailRequest;
import io.getlime.security.powerauth.lib.nextstep.model.request.GetOrganizationListRequest;
import io.getlime.security.powerauth.lib.nextstep.model.response.CreateOrganizationResponse;
import io.getlime.security.powerauth.lib.nextstep.model.response.DeleteOrganizationResponse;
import io.getlime.security.powerauth.lib.nextstep.model.response.GetOrganizationDetailResponse;
import io.getlime.security.powerauth.lib.nextstep.model.response.GetOrganizationListResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * REST controller class related to Next Step organizations.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@RestController
@RequestMapping(value = "organization")
@Validated
public class OrganizationController {

    private static final Logger logger = LoggerFactory.getLogger(OrganizationController.class);

    private final OrganizationService organizationService;

    /**
     * REST controller constructor.
     * @param organizationService Organization service.
     */
    @Autowired
    public OrganizationController(OrganizationService organizationService) {
        this.organizationService = organizationService;
    }

    /**
     * Create an organization.
     * @param request Create organization request.
     * @return Create organization response.
     * @throws OrganizationAlreadyExistsException Thrown when organization already exists.
     */
    @Operation(summary = "Create an organization")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Organization was created", content = {
                    @Content(mediaType = "application/json")
            }),
            @ApiResponse(responseCode = "400", description = "Invalid request, error codes: REQUEST_VALIDATION_FAILED, ORGANIZATION_ALREADY_EXISTS", content = {
                    @Content(mediaType = "application/json")
            }),
            @ApiResponse(responseCode = "500", description = "Unexpected error", content = {
                    @Content(mediaType = "application/json")
            })
    })
    @RequestMapping(method = RequestMethod.POST)
    public ObjectResponse<CreateOrganizationResponse> createOrganization(@Valid @RequestBody ObjectRequest<CreateOrganizationRequest> request) throws OrganizationAlreadyExistsException {
        logger.info("Received createOrganization request, organization ID: {}", request.getRequestObject().getOrganizationId());
        final CreateOrganizationResponse response = organizationService.createOrganization(request.getRequestObject());
        logger.info("The createOrganization request succeeded, organization ID: {}", request.getRequestObject().getOrganizationId());
        return new ObjectResponse<>(response);
    }

    /**
     * Get organization detail.
     *
     * @param organizationId Organization ID.
     * @return Get organization detail response.
     * @throws OrganizationNotFoundException Thrown in case organization does not exist.
     */
    @Operation(summary = "Get organization detail")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Organization detail sent in response", content = {
                    @Content(mediaType = "application/json")
            }),
            @ApiResponse(responseCode = "400", description = "Invalid request, error codes: REQUEST_VALIDATION_FAILED, ORGANIZATION_NOT_FOUND", content = {
                    @Content(mediaType = "application/json")
            }),
            @ApiResponse(responseCode = "500", description = "Unexpected error", content = {
                    @Content(mediaType = "application/json")
            })
    })
    @RequestMapping(value = "detail", method = RequestMethod.GET)
    public ObjectResponse<GetOrganizationDetailResponse> getOrganizationDetail(@RequestParam @NotBlank @Size(min = 2, max = 256) String organizationId) throws OrganizationNotFoundException {
        logger.info("Received getOrganizationDetail request, organization ID: {}", organizationId);
        GetOrganizationDetailRequest request = new GetOrganizationDetailRequest();
        request.setOrganizationId(organizationId);
        final GetOrganizationDetailResponse response = organizationService.getOrganizationDetail(request);
        logger.info("The getOrganizationDetail request succeeded, organization ID: {}", organizationId);
        return new ObjectResponse<>(response);
    }

    /**
     * Get organization detail using POST method.
     *
     * @param request Get organization detail request.
     * @return Get organization detail response.
     * @throws OrganizationNotFoundException Thrown in case organization does not exist.
     */
    @Operation(summary = "Get organization detail")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Organization detail sent in response", content = {
                    @Content(mediaType = "application/json")
            }),
            @ApiResponse(responseCode = "400", description = "Invalid request, error codes: REQUEST_VALIDATION_FAILED, ORGANIZATION_NOT_FOUND", content = {
                    @Content(mediaType = "application/json")
            }),
            @ApiResponse(responseCode = "500", description = "Unexpected error", content = {
                    @Content(mediaType = "application/json")
            })
    })
    @RequestMapping(value = "detail", method = RequestMethod.POST)
    public ObjectResponse<GetOrganizationDetailResponse> getOrganizationDetailPost(@Valid @RequestBody ObjectRequest<GetOrganizationDetailRequest> request) throws OrganizationNotFoundException {
        logger.info("Received getOrganizationDetailPost request, organization ID: {}", request.getRequestObject().getOrganizationId());
        final GetOrganizationDetailResponse response = organizationService.getOrganizationDetail(request.getRequestObject());
        logger.info("The getOrganizationDetailPost request succeeded, organization ID: {}", request.getRequestObject().getOrganizationId());
        return new ObjectResponse<>(response);
    }

    /**
     * List organizations defined in Next Step service.
     *
     * @return Get organizations response.
     */
    @Operation(summary = "Get organization list")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Organization list sent in response", content = {
                    @Content(mediaType = "application/json")
            }),
            @ApiResponse(responseCode = "500", description = "Unexpected error", content = {
                    @Content(mediaType = "application/json")
            })
    })
    @RequestMapping(method = RequestMethod.GET)
    public ObjectResponse<GetOrganizationListResponse> getOrganizationList() {
        logger.info("Received getOrganizationList request");
        final GetOrganizationListResponse response = organizationService.getOrganizationList();
        logger.info("The getOrganizationList request succeeded, number of organizations: {}", response.getOrganizations().size());
        return new ObjectResponse<>(response);
    }

    /**
     * List organizations defined in Next Step service using POST method.
     *
     * @param request Get organizations request.
     * @return Get organizations response.
     */
    @Operation(summary = "Get organization list")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Organization list sent in response", content = {
                    @Content(mediaType = "application/json")
            }),
            @ApiResponse(responseCode = "500", description = "Unexpected error", content = {
                    @Content(mediaType = "application/json")
            })
    })
    @RequestMapping(value = "list", method = RequestMethod.POST)
    public ObjectResponse<GetOrganizationListResponse> getOrganizationListPost(@Valid @RequestBody ObjectRequest<GetOrganizationListRequest> request) {
        logger.info("Received getOrganizationList request");
        final GetOrganizationListResponse response = organizationService.getOrganizationList();
        logger.info("The getOrganizationList request succeeded, number of organizations: {}", response.getOrganizations().size());
        return new ObjectResponse<>(response);
    }

    /**
     * Delete an organization.
     * @param request Delete organization request.
     * @return Delete organization response.
     * @throws OrganizationNotFoundException Thrown when organization is not found.
     * @throws DeleteNotAllowedException Thrown when delete action is not allowed.
     */
    @Operation(summary = "Delete an organization")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Organization was deleted", content = {
                    @Content(mediaType = "application/json")
            }),
            @ApiResponse(responseCode = "400", description = "Invalid request, error codes: REQUEST_VALIDATION_FAILED, ORGANIZATION_NOT_FOUND, DELETE_NOT_ALLOWED", content = {
                    @Content(mediaType = "application/json")
            }),
            @ApiResponse(responseCode = "500", description = "Unexpected error", content = {
                    @Content(mediaType = "application/json")
            })
    })
    @RequestMapping(value = "delete", method = RequestMethod.POST)
    public ObjectResponse<DeleteOrganizationResponse> deleteOrganization(@Valid @RequestBody ObjectRequest<DeleteOrganizationRequest> request) throws OrganizationNotFoundException, DeleteNotAllowedException {
        logger.info("Received deleteOrganization request, organization ID: {}", request.getRequestObject().getOrganizationId());
        final DeleteOrganizationResponse response = organizationService.deleteOrganization(request.getRequestObject());
        logger.info("The deleteOrganization request succeeded, organization ID: {}", request.getRequestObject().getOrganizationId());
        return new ObjectResponse<>(response);
    }

}
