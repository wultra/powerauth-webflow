/*
 * PowerAuth Web Flow and related software components
 * Copyright (C) 2019 Wultra s.r.o.
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
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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
            @ApiResponse(responseCode = "200", description = "Organization was created"),
            @ApiResponse(responseCode = "400", description = "Invalid request, error codes: REQUEST_VALIDATION_FAILED, ORGANIZATION_ALREADY_EXISTS"),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
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
            @ApiResponse(responseCode = "200", description = "Organization detail sent in response"),
            @ApiResponse(responseCode = "400", description = "Invalid request, error codes: REQUEST_VALIDATION_FAILED, ORGANIZATION_NOT_FOUND"),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
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
            @ApiResponse(responseCode = "200", description = "Organization detail sent in response"),
            @ApiResponse(responseCode = "400", description = "Invalid request, error codes: REQUEST_VALIDATION_FAILED, ORGANIZATION_NOT_FOUND"),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
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
            @ApiResponse(responseCode = "200", description = "Organization list sent in response"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
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
            @ApiResponse(responseCode = "200", description = "Organization list sent in response"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
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
            @ApiResponse(responseCode = "200", description = "Organization was deleted"),
            @ApiResponse(responseCode = "400", description = "Invalid request, error codes: REQUEST_VALIDATION_FAILED, ORGANIZATION_NOT_FOUND, DELETE_NOT_ALLOWED"),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
    })
    @RequestMapping(value = "delete", method = RequestMethod.POST)
    public ObjectResponse<DeleteOrganizationResponse> deleteOrganization(@Valid @RequestBody ObjectRequest<DeleteOrganizationRequest> request) throws OrganizationNotFoundException, DeleteNotAllowedException {
        logger.info("Received deleteOrganization request, organization ID: {}", request.getRequestObject().getOrganizationId());
        final DeleteOrganizationResponse response = organizationService.deleteOrganization(request.getRequestObject());
        logger.info("The deleteOrganization request succeeded, organization ID: {}", request.getRequestObject().getOrganizationId());
        return new ObjectResponse<>(response);
    }

}
