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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
    @RequestMapping(method = RequestMethod.POST)
    public ObjectResponse<CreateRoleResponse> createRole(@Valid @RequestBody ObjectRequest<CreateRoleRequest> request) throws RoleAlreadyExistsException {
        CreateRoleResponse response = roleService.createRole(request.getRequestObject());
        return new ObjectResponse<>(response);
    }

    /**
     * Get role list.
     * @param request Get role list request.
     * @return Get role list response.
     */
    @RequestMapping(value = "list", method = RequestMethod.POST)
    public ObjectResponse<GetRoleListResponse> getRoleList(@Valid @RequestBody ObjectRequest<GetRoleListRequest> request) {
        GetRoleListResponse response = roleService.getRoleList(request.getRequestObject());
        return new ObjectResponse<>(response);
    }

    /**
     * Delete a role.
     * @param request Delete role request.
     * @return Delete role response.
     * @throws RoleNotFoundException Thrown when role is not found.
     * @throws DeleteNotAllowedException Thrown when delete action is not allowed.
     */
    @RequestMapping(value = "delete", method = RequestMethod.POST)
    public ObjectResponse<DeleteRoleResponse> deleteRole(@Valid @RequestBody ObjectRequest<DeleteRoleRequest> request) throws RoleNotFoundException, DeleteNotAllowedException {
        DeleteRoleResponse response = roleService.deleteRole(request.getRequestObject());
        return new ObjectResponse<>(response);
    }

}
