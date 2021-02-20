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
import io.getlime.security.powerauth.app.nextstep.repository.UserRoleRepository;
import io.getlime.security.powerauth.app.nextstep.repository.model.entity.RoleEntity;
import io.getlime.security.powerauth.app.nextstep.repository.model.entity.UserIdentityEntity;
import io.getlime.security.powerauth.app.nextstep.repository.model.entity.UserRoleEntity;
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
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
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

    private final UserIdentityLookupService userIdentityLookupService;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;

    /**
     * Service constructor.
     * @param userIdentityLookupService User identity lookup service.
     * @param roleRepository Role repository.
     * @param userRoleRepository User role repository.
     */
    @Autowired
    public UserRoleService(UserIdentityLookupService userIdentityLookupService, RoleRepository roleRepository, UserRoleRepository userRoleRepository) {
        this.userIdentityLookupService = userIdentityLookupService;
        this.roleRepository = roleRepository;
        this.userRoleRepository = userRoleRepository;
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
        UserIdentityEntity user = userIdentityLookupService.findUser(request.getUserId());
        Optional<RoleEntity> roleOptional = roleRepository.findByName(request.getRoleName());
        if (!roleOptional.isPresent()) {
            throw new InvalidRequestException("Role not found: " + request.getRoleName());
        }
        RoleEntity role = roleOptional.get();
        Optional<UserRoleEntity> userRoleOptional = userRoleRepository.findByUserIdAndRole(user, role);
        UserRoleEntity userRole;
        if (userRoleOptional.isPresent()) {
            userRole = userRoleOptional.get();
            if (userRole.getStatus() == UserRoleStatus.ACTIVE) {
                throw new UserRoleAlreadyAssignedException("Role is already assigned: " + request.getRoleName() + ", user ID: " + user.getUserId());
            }
            userRole.setTimestampLastUpdated(new Date());
        } else {
            userRole = new UserRoleEntity();
            userRole.setUserId(user);
            userRole.setRole(role);
            userRole.setTimestampCreated(new Date());
        }
        userRole.setStatus(UserRoleStatus.ACTIVE);
        userRoleRepository.save(userRole);
        AddUserRoleResponse response = new AddUserRoleResponse();
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
        UserIdentityEntity user = userIdentityLookupService.findUser(request.getUserId());
        Optional<RoleEntity> roleOptional = roleRepository.findByName(request.getRoleName());
        if (!roleOptional.isPresent()) {
            throw new InvalidRequestException("Role not found: " + request.getRoleName());
        }
        RoleEntity role = roleOptional.get();
        Optional<UserRoleEntity> userRoleOptional = userRoleRepository.findByUserIdAndRole(user, role);
        UserRoleEntity userRole;
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
        userRoleRepository.save(userRole);
        RemoveUserRoleResponse response = new RemoveUserRoleResponse();
        response.setUserId(user.getUserId());
        response.setRoleName(role.getName());
        response.setUserRoleStatus(userRole.getStatus());
        return response;
    }

}