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

import io.getlime.security.powerauth.app.nextstep.repository.CredentialDefinitionRepository;
import io.getlime.security.powerauth.app.nextstep.repository.CredentialRepository;
import io.getlime.security.powerauth.app.nextstep.repository.UserIdentityRepository;
import io.getlime.security.powerauth.app.nextstep.repository.UserRoleRepository;
import io.getlime.security.powerauth.app.nextstep.repository.model.entity.*;
import io.getlime.security.powerauth.app.nextstep.service.adapter.UserLookupCustomizationService;
import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.CredentialStatus;
import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.UserIdentityStatus;
import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.UserRoleStatus;
import io.getlime.security.powerauth.lib.nextstep.model.exception.InvalidConfigurationException;
import io.getlime.security.powerauth.lib.nextstep.model.exception.InvalidRequestException;
import io.getlime.security.powerauth.lib.nextstep.model.exception.OperationNotFoundException;
import io.getlime.security.powerauth.lib.nextstep.model.exception.UserNotFoundException;
import io.getlime.security.powerauth.lib.nextstep.model.request.LookupUserRequest;
import io.getlime.security.powerauth.lib.nextstep.model.request.LookupUsersRequest;
import io.getlime.security.powerauth.lib.nextstep.model.response.GetUserDetailResponse;
import io.getlime.security.powerauth.lib.nextstep.model.response.LookupUserResponse;
import io.getlime.security.powerauth.lib.nextstep.model.response.LookupUsersResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.*;
import java.util.stream.Collectors;

/**
 * This service handles user identity lookup.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Service
public class UserIdentityLookupService {

    private final UserIdentityService userIdentityService;
    private final UserIdentityRepository userIdentityRepository;
    private final CredentialDefinitionRepository credentialDefinitionRepository;
    private final CredentialRepository credentialRepository;
    private final UserRoleRepository userRoleRepository;
    private final UserLookupCustomizationService userLookupCustomizationService;
    private final OperationPersistenceService operationPersistenceService;

    /**
     * Lookup service constructor.
     * @param userIdentityService User identity service.
     * @param userIdentityRepository User identity repository.
     * @param credentialDefinitionRepository Credential definition repository.
     * @param credentialRepository Credential repository.
     * @param userRoleRepository User role repository.
     * @param userLookupCustomizationService User identity customization service.
     * @param operationPersistenceService Operation persistence service.
     */
    @Autowired
    public UserIdentityLookupService(@Lazy UserIdentityService userIdentityService, UserIdentityRepository userIdentityRepository, CredentialDefinitionRepository credentialDefinitionRepository, CredentialRepository credentialRepository, UserRoleRepository userRoleRepository, UserLookupCustomizationService userLookupCustomizationService, @Lazy OperationPersistenceService operationPersistenceService) {
        this.userIdentityService = userIdentityService;
        this.userIdentityRepository = userIdentityRepository;
        this.credentialDefinitionRepository = credentialDefinitionRepository;
        this.credentialRepository = credentialRepository;
        this.userRoleRepository = userRoleRepository;
        this.userLookupCustomizationService = userLookupCustomizationService;
        this.operationPersistenceService = operationPersistenceService;
    }

    /**
     * Lookup user identities.
     * @param request Lookup user identities request.
     * @return Lookup user identities response.
     * @throws InvalidRequestException Thrown when request is invalid.
     * @throws UserNotFoundException Thrown when user identity is not found.
     * @throws InvalidConfigurationException Thrown when Next Step configuration is invalid.
     */
    @Transactional
    public LookupUsersResponse lookupUsers(LookupUsersRequest request) throws InvalidRequestException, UserNotFoundException, InvalidConfigurationException {
        String username = request.getUsername();
        String credentialName = request.getCredentialName();
        Date createdStartDate = request.getCreatedStartDate();
        Date createdEndDate = request.getCreatedEndDate();
        // Convert null dates for simpler queries
        if (createdStartDate == null && createdEndDate != null) {
            createdStartDate = new Date(Long.MIN_VALUE);
        }
        if (createdStartDate != null && createdEndDate == null) {
            createdEndDate = new Date();
        }
        UserIdentityStatus status = request.getUserIdentityStatus();
        CredentialStatus credentialStatus = request.getCredentialStatus();
        List<String> roles = request.getRoles();
        List<UserIdentityEntity> lookupResult = new ArrayList<>();
        CredentialDefinitionEntity credentialDefinition = null;
        boolean dateFiltered = false;

        if (credentialName!= null) {
            Optional<CredentialDefinitionEntity> credentialDefinitionOptional = credentialDefinitionRepository.findByName(credentialName);
            if (!credentialDefinitionOptional.isPresent()) {
                throw new InvalidRequestException("Credential definition not found: " + credentialName);
            }
            credentialDefinition = credentialDefinitionOptional.get();
        }

        // Choose main query based on most exact parameters, filter lookup results in code by additional parameters
        if (username != null && credentialName != null) {
            // When username and credentialName are present, lookup the user identity, single result or no result is found
            Optional<CredentialEntity> credentialOptional = credentialRepository.findByCredentialDefinitionAndUsername(credentialDefinition, username);
            if (!credentialOptional.isPresent()) {
                throw new UserNotFoundException("User not found, credential definition name: " + credentialName + ", username: " + username);
            }
            CredentialEntity credential = credentialOptional.get();
            if (credentialStatus == null || credential.getStatus() == credentialStatus) {
                // Filter by credentialStatus in case it is also specified
                UserIdentityEntity user = credential.getUser();
                lookupResult = Collections.singletonList(user);
            }
        } else if (credentialName != null && credentialStatus != null) {
            // When credentialName and credentialStatus are present, lookup the user identities, multiple or zero results are found
            List<CredentialEntity> credentialEntities = credentialRepository.findAllByCredentialDefinitionAndStatus(credentialDefinition, credentialStatus);
            for (CredentialEntity credential: credentialEntities) {
                lookupResult.add(credential.getUser());
            }
        } else if (createdStartDate != null && credentialStatus == null) {
            // Lookup the user identities by createdDate, multiple or zero results are found, credentialStatus filter is not allowed
            lookupResult = userIdentityRepository.findUserIdentitiesByCreatedDate(createdStartDate, createdEndDate);
            dateFiltered = true;
        } else {
            throw new InvalidRequestException("The lookup query contains an invalid combination of parameters");
        }

        if (createdStartDate != null && !dateFiltered) {
            // Filter by timestampCreated, but only if filter was not alreast applied
            Date finalCreatedStartDate = createdStartDate;
            Date finalCreatedEndDate = createdEndDate;
            lookupResult = lookupResult.stream()
                    .filter(user -> (user.getTimestampCreated().after(finalCreatedStartDate) && user.getTimestampCreated().before(finalCreatedEndDate)))
                    .collect(Collectors.toList());
        }

        if (status != null) {
            // Filter by status
            lookupResult = lookupResult.stream()
                    .filter(user -> user.getStatus() == status)
                    .collect(Collectors.toList());
        } else {
            // Do not return REMOVED user identities unless requested in lookup request
            lookupResult = lookupResult.stream()
                    .filter(user -> user.getStatus() != UserIdentityStatus.REMOVED)
                    .collect(Collectors.toList());
        }

        if (roles != null && !roles.isEmpty()) {
            // Filter by roles
            List<UserIdentityEntity> filteredList = new ArrayList<>();
            for (UserIdentityEntity user: lookupResult) {
                List<UserRoleEntity> userRoles = userRoleRepository.findAllByUserAndStatus(user, UserRoleStatus.ACTIVE);
                List<String> roleNames = userRoles.stream()
                        .map(roleEntity -> roleEntity.getRole().getName())
                        .collect(Collectors.toList());
                if (roleNames.containsAll(roles)) {
                    filteredList.add(user);
                }
            }
            lookupResult = filteredList;
        }

        LookupUsersResponse response = new LookupUsersResponse();
        for (UserIdentityEntity user: lookupResult) {
            GetUserDetailResponse userDetail = userIdentityService.getUserDetail(user.getUserId(), false);
            response.getUsers().add(userDetail);
        }
        return response;
    }

    /**
     * Lookup a single user identity.
     * @param request Lookup user identities request.
     * @return Lookup user identities response.
     * @throws InvalidRequestException Thrown when request is invalid.
     * @throws UserNotFoundException Thrown when user identity is not found.
     * @throws InvalidConfigurationException Thrown when Next Step configuration is invalid.
     * @throws OperationNotFoundException Thrown when operation is not found.
     */
    @Transactional
    public LookupUserResponse lookupUser(LookupUserRequest request) throws InvalidRequestException, UserNotFoundException, InvalidConfigurationException, OperationNotFoundException {
        String username = request.getUsername();
        String credentialName = request.getCredentialName();
        String operationId = request.getOperationId();
        CredentialDefinitionEntity credentialDefinition = null;

        if (credentialName!= null) {
            Optional<CredentialDefinitionEntity> credentialDefinitionOptional = credentialDefinitionRepository.findByName(credentialName);
            if (!credentialDefinitionOptional.isPresent()) {
                throw new InvalidRequestException("Credential definition not found: " + credentialName);
            }
            credentialDefinition = credentialDefinitionOptional.get();
            if (credentialDefinition.isDataAdapterProxyEnabled()) {
                // Lookup is performed using Data Adapter
                if (operationId == null) {
                    throw new InvalidRequestException("Operation ID is missing in Data Adapter user lookup request");
                }
                OperationEntity operation = operationPersistenceService.getOperation(operationId);
                String organizationId = null;
                if (operation.getOrganization() != null) {
                    organizationId = operation.getOrganization().getOrganizationId();
                } else {
                    // Organization is not set for the operation yet, use organization from credential if available
                    OrganizationEntity organization = credentialDefinition.getOrganization();
                    if (organization != null) {
                        organizationId = organization.getOrganizationId();
                    }
                }
                GetUserDetailResponse userDetail = userLookupCustomizationService.lookupUser(username, organizationId, operation);
                if (userDetail == null) {
                    throw new UserNotFoundException("User not found, credential definition name: " + credentialName + ", username: " + username);
                }
                LookupUserResponse response = new LookupUserResponse();
                response.setUser(userDetail);
                return response;
            }
        }

        // When username and credentialName are present, lookup the user identity, single result or no result is found
        Optional<CredentialEntity> credentialOptional = credentialRepository.findByCredentialDefinitionAndUsername(credentialDefinition, username);
        if (!credentialOptional.isPresent()) {
            throw new UserNotFoundException("User not found, credential definition name: " + credentialName + ", username: " + username);
        }
        CredentialEntity credential = credentialOptional.get();
        if (credential.getStatus() == CredentialStatus.REMOVED) {
            throw new UserNotFoundException("User not found, credential definition name: " + credentialName + ", username: " + username);
        }
        String userId = credential.getUser().getUserId();
        LookupUserResponse response = new LookupUserResponse();
        GetUserDetailResponse userDetail = userIdentityService.getUserDetail(userId, false);
        response.setUser(userDetail);
        return response;
    }

    /**
     * Find a user identity. This method is not transactional and should be used for utility purposes only.
     * @param userId User ID.
     * @return User identity entity.
     * @throws UserNotFoundException Thrown when user identity entity is not found.
     */
    public UserIdentityEntity findUser(String userId) throws UserNotFoundException {
        Optional<UserIdentityEntity> userOptional = userIdentityRepository.findById(userId);
        if (!userOptional.isPresent()) {
            throw new UserNotFoundException("User identity not found: " + userId);
        }
        UserIdentityEntity user = userOptional.get();
        if (user.getStatus() == UserIdentityStatus.REMOVED) {
            throw new UserNotFoundException("User identity is REMOVED: " + userId);
        }
        return user;
    }

    /**
     * Find a user identity. This method is not transactional and should be used for utility purposes only.
     * @param userId User ID.
     * @param includeRemoved Whether removed user identities should be returned.
     * @return User identity entity.
     * @throws UserNotFoundException Thrown when user identity entity is not found.
     */
    public UserIdentityEntity findUser(String userId, boolean includeRemoved) throws UserNotFoundException {
        Optional<UserIdentityEntity> userOptional = userIdentityRepository.findById(userId);
        if (!userOptional.isPresent()) {
            throw new UserNotFoundException("User identity not found: " + userId);
        }
        UserIdentityEntity user = userOptional.get();
        if (!includeRemoved &&  user.getStatus() == UserIdentityStatus.REMOVED) {
            throw new UserNotFoundException("User identity is REMOVED: " + userId);
        }
        return user;
    }

    /**
     * Find an optional user identity. This method is not transactional and should be used for utility purposes only.
     * @param userId User ID.
     * @return Optional user identity.
     */
    public Optional<UserIdentityEntity> findUserOptional(String userId) {
        Optional<UserIdentityEntity> userOptional = userIdentityRepository.findById(userId);
        if (!userOptional.isPresent()) {
            return Optional.empty();
        }
        UserIdentityEntity user = userOptional.get();
        if (user.getStatus() == UserIdentityStatus.REMOVED) {
            return Optional.empty();
        }
        return Optional.of(user);
    }

    /**
     * Find an optional user identity. This method is not transactional and should be used for utility purposes only.
     * @param userId User ID.
     * @param includeRemoved Whether removed user identities should be included.
     * @return Optional user identity.
     */
    public Optional<UserIdentityEntity> findUserOptional(String userId, boolean includeRemoved) {
        Optional<UserIdentityEntity> userOptional = userIdentityRepository.findById(userId);
        if (!userOptional.isPresent()) {
            return Optional.empty();
        }
        UserIdentityEntity user = userOptional.get();
        if (!includeRemoved && user.getStatus() == UserIdentityStatus.REMOVED) {
            return Optional.empty();
        }
        return Optional.of(user);
    }
}