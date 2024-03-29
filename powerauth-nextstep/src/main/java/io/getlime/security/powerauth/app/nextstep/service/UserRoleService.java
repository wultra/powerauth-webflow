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
import io.getlime.security.powerauth.app.nextstep.repository.RoleRepository;
import io.getlime.security.powerauth.app.nextstep.repository.UserIdentityRepository;
import io.getlime.security.powerauth.app.nextstep.repository.catalogue.RepositoryCatalogue;
import io.getlime.security.powerauth.app.nextstep.repository.model.entity.RoleEntity;
import io.getlime.security.powerauth.app.nextstep.repository.model.entity.UserIdentityEntity;
import io.getlime.security.powerauth.app.nextstep.repository.model.entity.UserRoleEntity;
import io.getlime.security.powerauth.app.nextstep.service.catalogue.ServiceCatalogue;
import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.UserRoleStatus;
import io.getlime.security.powerauth.lib.nextstep.model.exception.InvalidRequestException;
import io.getlime.security.powerauth.lib.nextstep.model.exception.UserNotFoundException;
import io.getlime.security.powerauth.lib.nextstep.model.exception.UserRoleAlreadyAssignedException;
import io.getlime.security.powerauth.lib.nextstep.model.exception.UserRoleNotAssignedException;
import io.getlime.security.powerauth.lib.nextstep.model.request.AddUserRoleRequest;
import io.getlime.security.powerauth.lib.nextstep.model.request.RemoveUserRoleRequest;
import io.getlime.security.powerauth.lib.nextstep.model.response.AddUserRoleResponse;
import io.getlime.security.powerauth.lib.nextstep.model.response.RemoveUserRoleResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Optional;

/**
 * This service handles persistence of user roles.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Service
public class UserRoleService {

    private final Logger logger = LoggerFactory.getLogger(UserRoleService.class);
    private static final String AUDIT_TYPE_USER_IDENTITY = "USER_IDENTITY";

    private final RoleRepository roleRepository;
    private final UserIdentityRepository userIdentityRepository;
    private final ServiceCatalogue serviceCatalogue;
    private final Audit audit;

    /**
     * Service constructor.
     * @param repositoryCatalogue Repository catalogue.
     * @param serviceCatalogue Service catalogue.
     * @param audit Audit interface.
     */
    @Autowired
    public UserRoleService(RepositoryCatalogue repositoryCatalogue, @Lazy ServiceCatalogue serviceCatalogue, Audit audit) {
        this.roleRepository = repositoryCatalogue.getRoleRepository();
        this.userIdentityRepository = repositoryCatalogue.getUserIdentityRepository();
        this.serviceCatalogue = serviceCatalogue;
        this.audit = audit;
    }

    /**
     * Add user role to a user identity.
     * @param request Add user role request.
     * @return Add user role response.
     * @throws UserNotFoundException Thrown when user identity is not found.
     * @throws InvalidRequestException Thrown when request is invalid.
     * @throws UserRoleAlreadyAssignedException Thrown when user role is already assigned.
     */
    @Transactional
    public AddUserRoleResponse addUserRole(AddUserRoleRequest request) throws UserNotFoundException, InvalidRequestException, UserRoleAlreadyAssignedException {
        final UserIdentityLookupService userIdentityLookupService = serviceCatalogue.getUserIdentityLookupService();
        final UserIdentityService userIdentityService = serviceCatalogue.getUserIdentityService();

        UserIdentityEntity user = userIdentityLookupService.findUser(request.getUserId());
        final RoleEntity role = roleRepository.findByName(request.getRoleName()).orElseThrow(() ->
                new InvalidRequestException("Role not found: " + request.getRoleName()));
        final Optional<UserRoleEntity> userRoleOptional = user.getRoles().stream().filter(r -> r.getRole().equals(role)).findFirst();
        final UserRoleEntity userRole;
        if (userRoleOptional.isPresent()) {
            userRole = userRoleOptional.get();
            if (userRole.getStatus() == UserRoleStatus.ACTIVE) {
                throw new UserRoleAlreadyAssignedException("Role is already assigned: " + request.getRoleName() + ", user ID: " + user.getUserId());
            }
            userRole.setTimestampLastUpdated(new Date());
        } else {
            userRole = new UserRoleEntity();
            userRole.setUser(user);
            userRole.setRole(role);
            userRole.setTimestampCreated(new Date());
            user.getRoles().add(userRole);
        }
        userRole.setStatus(UserRoleStatus.ACTIVE);
        // Save user identity and a snapshot to the history table
        userIdentityService.updateUserIdentityHistory(user);
        user = userIdentityRepository.save(user);
        logger.debug("User role was added, user ID: {}, role name: {}", user.getUserId(), role.getName());
        audit.info("User role was added", AuditDetail.builder()
                .type(AUDIT_TYPE_USER_IDENTITY)
                .param("userId", user.getUserId())
                .param("roleName", role.getName())
                .build());
        final AddUserRoleResponse response = new AddUserRoleResponse();
        response.setUserId(user.getUserId());
        response.setRoleName(role.getName());
        response.setUserRoleStatus(userRole.getStatus());
        return response;
    }

    /**
     * Remove user role for a user identity.
     * @param request Remove user role request.
     * @return Remove user role response.
     * @throws UserNotFoundException Thrown when user identity is not found.
     * @throws InvalidRequestException Thrown when request is invalid.
     * @throws UserRoleNotAssignedException Thrown when user role is not assigned to user identity.
     */
    @Transactional
    public RemoveUserRoleResponse removeUserRole(RemoveUserRoleRequest request) throws UserNotFoundException, InvalidRequestException, UserRoleNotAssignedException {
        final UserIdentityLookupService userIdentityLookupService = serviceCatalogue.getUserIdentityLookupService();
        final UserIdentityService userIdentityService = serviceCatalogue.getUserIdentityService();

        UserIdentityEntity user = userIdentityLookupService.findUser(request.getUserId());
        final RoleEntity role = roleRepository.findByName(request.getRoleName()).orElseThrow(() ->
                new InvalidRequestException("Role not found: " + request.getRoleName()));
        final Optional<UserRoleEntity> userRoleOptional = user.getRoles().stream().filter(r -> r.getRole().equals(role)).findFirst();
        final UserRoleEntity userRole;
        if (userRoleOptional.isPresent()) {
            userRole = userRoleOptional.get();
            if (userRole.getStatus() == UserRoleStatus.REMOVED) {
                throw new UserRoleNotAssignedException("Role is not assigned: " + request.getRoleName() + ", user ID: " + user.getUserId());
            }
            userRole.setStatus(UserRoleStatus.REMOVED);
            userRole.setTimestampLastUpdated(new Date());
        } else {
            throw new UserRoleNotAssignedException("Role is not assigned: " + request.getRoleName() + ", user ID: " + user.getUserId());
        }
        user.getRoles().remove(userRole);
        // Save user identity and a snapshot to the history table
        userIdentityService.updateUserIdentityHistory(user);
        user = userIdentityRepository.save(user);
        logger.debug("User role was removed, user ID: {}, role name: {}", user.getUserId(), role.getName());
        audit.info("User role was removed", AuditDetail.builder()
                .type(AUDIT_TYPE_USER_IDENTITY)
                .param("userId", user.getUserId())
                .param("roleName", role.getName())
                .build());
        final RemoveUserRoleResponse response = new RemoveUserRoleResponse();
        response.setUserId(user.getUserId());
        response.setRoleName(role.getName());
        response.setUserRoleStatus(userRole.getStatus());
        return response;
    }

}