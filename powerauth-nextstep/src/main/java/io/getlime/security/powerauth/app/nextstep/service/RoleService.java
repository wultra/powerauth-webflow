/*
 * Copyright 2012 Wultra s.r.o.
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

import io.getlime.security.powerauth.app.nextstep.repository.RoleRepository;
import io.getlime.security.powerauth.app.nextstep.repository.model.entity.RoleEntity;
import io.getlime.security.powerauth.lib.nextstep.model.entity.RoleDetail;
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
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Date;
import java.util.Optional;

/**
 * This service handles persistence of roles.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Service
public class RoleService {

    private final RoleRepository roleRepository;

    private final Logger logger = LoggerFactory.getLogger(RoleService.class);

    @Autowired
    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

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

    @Transactional
    public GetRoleListResponse getRoleList(GetRoleListRequest request) {
        Iterable<RoleEntity> roles = roleRepository.findAll();
        GetRoleListResponse response = new GetRoleListResponse();
        for (RoleEntity role: roles) {
            // TODO - use converter
            RoleDetail roleDetail = new RoleDetail();
            roleDetail.setRoleName(role.getName());
            roleDetail.setDescription(role.getDescription());
            roleDetail.setTimestampCreated(role.getTimestampCreated());
            roleDetail.setTimestampLastUpdated(role.getTimestampLastUpdated());
            response.getRoles().add(roleDetail);
        }
        return response;
    }

    @Transactional
    public DeleteRoleResponse deleteRole(DeleteRoleRequest request) throws RoleNotFoundException {
        Optional<RoleEntity> roleOptional = roleRepository.findByName(request.getRoleName());
        if (!roleOptional.isPresent()) {
            throw new RoleNotFoundException("Role not found: " + request.getRoleName());
        }
        RoleEntity role = roleOptional.get();
        roleRepository.delete(role);
        DeleteRoleResponse response = new DeleteRoleResponse();
        response.setRoleName(role.getName());
        return response;
    }

}
