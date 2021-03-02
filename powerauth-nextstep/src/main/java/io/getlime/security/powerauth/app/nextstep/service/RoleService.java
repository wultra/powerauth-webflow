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
package io.getlime.security.powerauth.app.nextstep.service;

import io.getlime.security.powerauth.app.nextstep.converter.RoleConverter;
import io.getlime.security.powerauth.app.nextstep.repository.RoleRepository;
import io.getlime.security.powerauth.app.nextstep.repository.UserRoleRepository;
import io.getlime.security.powerauth.app.nextstep.repository.model.entity.RoleEntity;
import io.getlime.security.powerauth.app.nextstep.repository.model.entity.UserRoleEntity;
import io.getlime.security.powerauth.lib.nextstep.model.entity.RoleDetail;
import io.getlime.security.powerauth.lib.nextstep.model.exception.RoleAlreadyExistsException;
import io.getlime.security.powerauth.lib.nextstep.model.exception.RoleCannotBeDeletedException;
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
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * This service handles persistence of roles.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Service
public class RoleService {

    private final Logger logger = LoggerFactory.getLogger(RoleService.class);

    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;

    private final RoleConverter roleConverter = new RoleConverter();

    /**
     * Role service constructor.
     * @param roleRepository Role repository.
     * @param userRoleRepository User role repository.
     */
    @Autowired
    public RoleService(RoleRepository roleRepository, UserRoleRepository userRoleRepository) {
        this.roleRepository = roleRepository;
        this.userRoleRepository = userRoleRepository;
    }

    /**
     * Create a role.
     * @param request Create role request.
     * @return Create role response.
     * @throws RoleAlreadyExistsException Thrown when role already exists.
     */
    @Transactional
    public CreateRoleResponse createRole(CreateRoleRequest request) throws RoleAlreadyExistsException {
        Optional<RoleEntity> roleOptional = roleRepository.findByName(request.getRoleName());
        if (roleOptional.isPresent()) {
            throw new RoleAlreadyExistsException("Role already exists: " + request.getRoleName());
        }
        RoleEntity role = new RoleEntity();
        role.setName(request.getRoleName());
        role.setDescription(request.getDescription());
        role.setTimestampCreated(new Date());
        roleRepository.save(role);
        CreateRoleResponse response = new CreateRoleResponse();
        response.setRoleName(role.getName());
        response.setDescription(role.getDescription());
        return response;
    }

    /**
     * Get list of roles.
     * @param request Get role list request.
     * @return Get role list response.
     */
    @Transactional
    public GetRoleListResponse getRoleList(GetRoleListRequest request) {
        Iterable<RoleEntity> roles = roleRepository.findAll();
        GetRoleListResponse response = new GetRoleListResponse();
        for (RoleEntity role : roles) {
            RoleDetail roleDetail = roleConverter.fromEntity(role);
            response.getRoles().add(roleDetail);
        }
        return response;
    }

    /**
     * Delete a role.
     * @param request Delete role request.
     * @return Delete role response.
     * @throws RoleNotFoundException Thrown when role is not found.
     */
    @Transactional
    public DeleteRoleResponse deleteRole(DeleteRoleRequest request) throws RoleNotFoundException, RoleCannotBeDeletedException {
        Optional<RoleEntity> roleOptional = roleRepository.findByName(request.getRoleName());
        if (!roleOptional.isPresent()) {
            throw new RoleNotFoundException("Role not found: " + request.getRoleName());
        }
        RoleEntity role = roleOptional.get();
        List<UserRoleEntity> usedRoles = userRoleRepository.findAllByRole(role);
        if (!usedRoles.isEmpty()) {
            throw new RoleCannotBeDeletedException("Role cannot be deleted because it is used: " + request.getRoleName());
        }
        roleRepository.delete(role);
        DeleteRoleResponse response = new DeleteRoleResponse();
        response.setRoleName(role.getName());
        return response;
    }

}
