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
package io.getlime.security.powerauth.app.nextstep.service;

import com.wultra.core.audit.base.Audit;
import com.wultra.core.audit.base.model.AuditDetail;
import io.getlime.security.powerauth.app.nextstep.converter.RoleConverter;
import io.getlime.security.powerauth.app.nextstep.repository.RoleRepository;
import io.getlime.security.powerauth.app.nextstep.repository.UserRoleRepository;
import io.getlime.security.powerauth.app.nextstep.repository.catalogue.RepositoryCatalogue;
import io.getlime.security.powerauth.app.nextstep.repository.model.entity.RoleEntity;
import io.getlime.security.powerauth.lib.nextstep.model.entity.RoleDetail;
import io.getlime.security.powerauth.lib.nextstep.model.exception.DeleteNotAllowedException;
import io.getlime.security.powerauth.lib.nextstep.model.exception.RoleAlreadyExistsException;
import io.getlime.security.powerauth.lib.nextstep.model.exception.RoleNotFoundException;
import io.getlime.security.powerauth.lib.nextstep.model.request.CreateRoleRequest;
import io.getlime.security.powerauth.lib.nextstep.model.request.DeleteRoleRequest;
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

    private final Logger logger = LoggerFactory.getLogger(RoleService.class);
    private static final String AUDIT_TYPE_CONFIGURATION = "CONFIGURATION";

    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final Audit audit;

    private final RoleConverter roleConverter = new RoleConverter();

    /**
     * Role service constructor.
     * @param repositoryCatalogue Repository catalogue.
     * @param audit Audit interface.
     */
    @Autowired
    public RoleService(RepositoryCatalogue repositoryCatalogue, Audit audit) {
        this.roleRepository = repositoryCatalogue.getRoleRepository();
        this.userRoleRepository = repositoryCatalogue.getUserRoleRepository();
        this.audit = audit;
    }

    /**
     * Create a role.
     * @param request Create role request.
     * @return Create role response.
     * @throws RoleAlreadyExistsException Thrown when role already exists.
     */
    @Transactional
    public CreateRoleResponse createRole(CreateRoleRequest request) throws RoleAlreadyExistsException {
        final Optional<RoleEntity> roleOptional = roleRepository.findByName(request.getRoleName());
        if (roleOptional.isPresent()) {
            throw new RoleAlreadyExistsException("Role already exists: " + request.getRoleName());
        }
        RoleEntity role = new RoleEntity();
        role.setName(request.getRoleName());
        role.setDescription(request.getDescription());
        role.setTimestampCreated(new Date());
        role = roleRepository.save(role);
        logger.debug("Role was created, role ID: {}, role name: {}", role.getRoleId(), role.getName());
        audit.info("Role was created", AuditDetail.builder()
                .type(AUDIT_TYPE_CONFIGURATION)
                .param("role", role)
                .build());
        final CreateRoleResponse response = new CreateRoleResponse();
        response.setRoleName(role.getName());
        response.setDescription(role.getDescription());
        return response;
    }

    /**
     * Get list of roles.
     * @return Get role list response.
     */
    @Transactional
    public GetRoleListResponse getRoleList() {
        final Iterable<RoleEntity> roles = roleRepository.findAll();
        final GetRoleListResponse response = new GetRoleListResponse();
        for (RoleEntity role : roles) {
            final RoleDetail roleDetail = roleConverter.fromEntity(role);
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
    public DeleteRoleResponse deleteRole(DeleteRoleRequest request) throws RoleNotFoundException, DeleteNotAllowedException {
        final Optional<RoleEntity> roleOptional = roleRepository.findByName(request.getRoleName());
        if (!roleOptional.isPresent()) {
            throw new RoleNotFoundException("Role not found: " + request.getRoleName());
        }
        final RoleEntity role = roleOptional.get();
        final long existingRoleCount = userRoleRepository.countByRole(role);
        if (existingRoleCount > 0) {
            throw new DeleteNotAllowedException("Role cannot be deleted because it is used: " + request.getRoleName());
        }
        roleRepository.delete(role);
        logger.debug("Role was deleted, role ID: {}, role name: {}", role.getRoleId(), role.getName());
        audit.info("Role was deleted", AuditDetail.builder()
                .type(AUDIT_TYPE_CONFIGURATION)
                .param("roleId", role.getRoleId())
                .param("roleName", role.getName())
                .build());
        final DeleteRoleResponse response = new DeleteRoleResponse();
        response.setRoleName(role.getName());
        return response;
    }

}
