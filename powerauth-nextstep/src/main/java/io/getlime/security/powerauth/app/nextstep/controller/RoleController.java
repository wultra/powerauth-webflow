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
import io.getlime.security.powerauth.app.nextstep.service.RoleService;
import io.getlime.security.powerauth.lib.nextstep.model.exception.DeleteNotAllowedException;
import io.getlime.security.powerauth.lib.nextstep.model.exception.RoleAlreadyExistsException;
import io.getlime.security.powerauth.lib.nextstep.model.exception.RoleNotFoundException;
import io.getlime.security.powerauth.lib.nextstep.model.request.CreateRoleRequest;
import io.getlime.security.powerauth.lib.nextstep.model.request.DeleteRoleRequest;
import io.getlime.security.powerauth.lib.nextstep.model.request.GetRoleListRequest;
import io.getlime.security.powerauth.lib.nextstep.model.response.CreateRoleResponse;
import io.getlime.security.powerauth.lib.nextstep.model.response.DeleteRoleResponse;
import io.getlime.security.powerauth.lib.nextstep.model.response.GetRoleListResponse;
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
 * REST controller for user roles.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@RestController
@RequestMapping("role")
@Validated
public class RoleController {

    private static final Logger logger = LoggerFactory.getLogger(RoleController.class);

    private final RoleService roleService;

    /**
     * REST controller constructor.
     * @param roleService Role service.
     */
    @Autowired
    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    /**
     * Create a role.
     * @param request Create role request.
     * @return Create role response.
     * @throws RoleAlreadyExistsException Thrown when role already exists.
     */
    @Operation(summary = "Create a role")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Role was created"),
            @ApiResponse(responseCode = "400", description = "Invalid request, error codes: REQUEST_VALIDATION_FAILED, ROLE_ALREADY_EXISTS"),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
    })
    @RequestMapping(method = RequestMethod.POST)
    public ObjectResponse<CreateRoleResponse> createRole(@Valid @RequestBody ObjectRequest<CreateRoleRequest> request) throws RoleAlreadyExistsException {
        logger.info("Received createRole request, role name: {}", request.getRequestObject().getRoleName());
        final CreateRoleResponse response = roleService.createRole(request.getRequestObject());
        logger.info("The createRole request succeeded, role name: {}", request.getRequestObject().getRoleName());
        return new ObjectResponse<>(response);
    }

    /**
     * Get role list.
     * @return Get role list response.
     */
    @Operation(summary = "Get role list")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Role list sent in response"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
    })
    @RequestMapping(method = RequestMethod.GET)
    public ObjectResponse<GetRoleListResponse> getRoleList() {
        logger.info("Received getRoleList request");
        final GetRoleListResponse response = roleService.getRoleList();
        logger.info("The getRoleList request succeeded");
        return new ObjectResponse<>(response);
    }

    /**
     * Get role list using POST method.
     * @param request Get role list request.
     * @return Get role list response.
     */
    @Operation(summary = "Get role list")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Role list sent in response"),
            @ApiResponse(responseCode = "400", description = "Invalid request, error codes: REQUEST_VALIDATION_FAILED"),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
    })
    @RequestMapping(value = "list", method = RequestMethod.POST)
    public ObjectResponse<GetRoleListResponse> getRoleListPost(@Valid @RequestBody ObjectRequest<GetRoleListRequest> request) {
        logger.info("Received getRoleListPost request");
        final GetRoleListResponse response = roleService.getRoleList();
        logger.info("The getRoleListPost request succeeded");
        return new ObjectResponse<>(response);
    }

    /**
     * Delete a role.
     * @param request Delete role request.
     * @return Delete role response.
     * @throws RoleNotFoundException Thrown when role is not found.
     * @throws DeleteNotAllowedException Thrown when delete action is not allowed.
     */
    @Operation(summary = "Delete a role")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Role was deleted"),
            @ApiResponse(responseCode = "400", description = "Invalid request, error codes: REQUEST_VALIDATION_FAILED, ROLE_NOT_FOUND, DELETE_NOT_ALLOWED"),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
    })
    @RequestMapping(value = "delete", method = RequestMethod.POST)
    public ObjectResponse<DeleteRoleResponse> deleteRole(@Valid @RequestBody ObjectRequest<DeleteRoleRequest> request) throws RoleNotFoundException, DeleteNotAllowedException {
        logger.info("Received deleteRole request, role name: {}", request.getRequestObject().getRoleName());
        final DeleteRoleResponse response = roleService.deleteRole(request.getRequestObject());
        logger.info("The deleteRole request succeeded, role name: {}", request.getRequestObject().getRoleName());
        return new ObjectResponse<>(response);
    }

}
