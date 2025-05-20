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
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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
    @PostMapping
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
    @GetMapping
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
    @PostMapping("list")
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
    @PostMapping("delete")
    public ObjectResponse<DeleteRoleResponse> deleteRole(@Valid @RequestBody ObjectRequest<DeleteRoleRequest> request) throws RoleNotFoundException, DeleteNotAllowedException {
        logger.info("Received deleteRole request, role name: {}", request.getRequestObject().getRoleName());
        final DeleteRoleResponse response = roleService.deleteRole(request.getRequestObject());
        logger.info("The deleteRole request succeeded, role name: {}", request.getRequestObject().getRoleName());
        return new ObjectResponse<>(response);
    }

}
