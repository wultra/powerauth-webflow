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

import io.getlime.security.powerauth.app.nextstep.repository.CredentialDefinitionRepository;
import io.getlime.security.powerauth.app.nextstep.repository.CredentialRepository;
import io.getlime.security.powerauth.app.nextstep.repository.UserIdentityRepository;
import io.getlime.security.powerauth.app.nextstep.repository.catalogue.RepositoryCatalogue;
import io.getlime.security.powerauth.app.nextstep.repository.model.entity.*;
import io.getlime.security.powerauth.app.nextstep.service.adapter.UserLookupCustomizationService;
import io.getlime.security.powerauth.app.nextstep.service.catalogue.ServiceCatalogue;
import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.CredentialStatus;
import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.UserIdentityStatus;
import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.UserRoleStatus;
import io.getlime.security.powerauth.lib.nextstep.model.exception.*;
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
import java.util.stream.Stream;

/**
 * This service handles user identity lookup.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Service
public class UserIdentityLookupService {

    private final UserIdentityRepository userIdentityRepository;
    private final CredentialDefinitionRepository credentialDefinitionRepository;
    private final CredentialRepository credentialRepository;
    private final ServiceCatalogue serviceCatalogue;

    /**
     * Lookup service constructor.
     * @param repositoryCatalogue Repository catalogue.
     * @param serviceCatalogue Service catalogue.
     */
    @Autowired
    public UserIdentityLookupService(RepositoryCatalogue repositoryCatalogue, @Lazy ServiceCatalogue serviceCatalogue) {
        this.userIdentityRepository = repositoryCatalogue.getUserIdentityRepository();
        this.credentialDefinitionRepository = repositoryCatalogue.getCredentialDefinitionRepository();
        this.credentialRepository = repositoryCatalogue.getCredentialRepository();
        this.serviceCatalogue = serviceCatalogue;
    }

    /**
     * Lookup user identities.
     * @param request Lookup user identities request.
     * @return Lookup user identities response.
     * @throws InvalidRequestException Thrown when request is invalid.
     * @throws UserNotFoundException Thrown when user identity is not found.
     * @throws InvalidConfigurationException Thrown when Next Step configuration is invalid.
     * @throws EncryptionException Thrown when decryption fails.
     */
    @Transactional
    public LookupUsersResponse lookupUsers(LookupUsersRequest request) throws InvalidRequestException, UserNotFoundException, InvalidConfigurationException, EncryptionException {
        final UserIdentityService userIdentityService = serviceCatalogue.getUserIdentityService();
        final String username = request.getUsername();
        final String credentialName = request.getCredentialName();
        Date createdStartDate = request.getCreatedStartDate();
        Date createdEndDate = request.getCreatedEndDate();
        // Convert null dates for simpler queries
        if (createdStartDate == null && createdEndDate != null) {
            createdStartDate = new Date(Long.MIN_VALUE);
        }
        if (createdStartDate != null && createdEndDate == null) {
            createdEndDate = new Date();
        }
        final UserIdentityStatus status = request.getUserIdentityStatus();
        final CredentialStatus credentialStatus = request.getCredentialStatus();
        final List<String> roles = request.getRoles();
        List<UserIdentityEntity> lookupResult = new ArrayList<>();
        CredentialDefinitionEntity credentialDefinition = null;
        boolean dateFiltered = false;

        if (credentialName!= null) {
            final Optional<CredentialDefinitionEntity> credentialDefinitionOptional = credentialDefinitionRepository.findByName(credentialName);
            if (!credentialDefinitionOptional.isPresent()) {
                throw new InvalidRequestException("Credential definition not found: " + credentialName);
            }
            credentialDefinition = credentialDefinitionOptional.get();
        }

        // Choose main query based on most exact parameters, filter lookup results in code by additional parameters
        if (username != null && credentialName != null) {
            // When username and credentialName are present, lookup the user identity, single result or no result is found
            final Optional<CredentialEntity> credentialOptional = credentialRepository.findByCredentialDefinitionAndUsername(credentialDefinition, username);
            if (!credentialOptional.isPresent()) {
                throw new UserNotFoundException("User not found, credential definition name: " + credentialName + ", username: " + username);
            }
            final CredentialEntity credential = credentialOptional.get();
            if (credentialStatus == null || credential.getStatus() == credentialStatus) {
                // Filter by credentialStatus in case it is also specified
                UserIdentityEntity user = credential.getUser();
                lookupResult = Collections.singletonList(user);
            }
        } else if (credentialName != null && credentialStatus != null) {
            // When credentialName and credentialStatus are present, lookup the user identities, multiple or zero results are found
            List<UserIdentityEntity> lookupResultTemp = new ArrayList<>();
            try (final Stream<CredentialEntity> credentialEntities = credentialRepository.findAllByCredentialDefinitionAndStatus(credentialDefinition, credentialStatus)) {
                credentialEntities.forEach(credential -> lookupResultTemp.add(credential.getUser()));
            }
            lookupResult = lookupResultTemp;
        } else if (createdStartDate != null && credentialStatus == null) {
            // Lookup the user identities by createdDate, multiple or zero results are found, credentialStatus filter is not allowed
            lookupResult = userIdentityRepository.findUserIdentitiesByCreatedDate(createdStartDate, createdEndDate).collect(Collectors.toList());
            dateFiltered = true;
        } else {
            throw new InvalidRequestException("The lookup query contains an invalid combination of parameters");
        }

        if (createdStartDate != null && !dateFiltered) {
            // Filter by timestampCreated, but only if filter was not already applied
            final Date finalCreatedStartDate = createdStartDate;
            final Date finalCreatedEndDate = createdEndDate;
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
            final List<UserIdentityEntity> filteredList = new ArrayList<>();
            for (UserIdentityEntity user: lookupResult) {
                final List<UserRoleEntity> userRoles = user.getRoles().stream().filter(r -> r.getStatus() == UserRoleStatus.ACTIVE).collect(Collectors.toList());
                final List<String> roleNames = userRoles.stream()
                        .map(roleEntity -> roleEntity.getRole().getName())
                        .collect(Collectors.toList());
                if (roleNames.containsAll(roles)) {
                    filteredList.add(user);
                }
            }
            lookupResult = filteredList;
        }

        final LookupUsersResponse response = new LookupUsersResponse();
        for (UserIdentityEntity user: lookupResult) {
            final GetUserDetailResponse userDetail = userIdentityService.getUserDetail(user.getUserId(), credentialDefinition, false);
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
     * @throws EncryptionException Thrown when decryption fails.
     */
    @Transactional
    public LookupUserResponse lookupUser(LookupUserRequest request) throws InvalidRequestException, UserNotFoundException, InvalidConfigurationException, OperationNotFoundException, EncryptionException {
        final OperationPersistenceService operationPersistenceService = serviceCatalogue.getOperationPersistenceService();
        final UserLookupCustomizationService userLookupCustomizationService = serviceCatalogue.getUserLookupCustomizationService();
        final UserIdentityService userIdentityService = serviceCatalogue.getUserIdentityService();

        final String username = request.getUsername();
        final String credentialName = request.getCredentialName();
        final String operationId = request.getOperationId();
        CredentialDefinitionEntity credentialDefinition = null;

        if (credentialName!= null) {
            final Optional<CredentialDefinitionEntity> credentialDefinitionOptional = credentialDefinitionRepository.findByName(credentialName);
            if (!credentialDefinitionOptional.isPresent()) {
                throw new InvalidRequestException("Credential definition not found: " + credentialName);
            }
            credentialDefinition = credentialDefinitionOptional.get();
            if (credentialDefinition.isDataAdapterProxyEnabled()) {
                // Lookup is performed using Data Adapter
                if (operationId == null) {
                    throw new InvalidRequestException("Operation ID is missing in Data Adapter user lookup request");
                }
                final OperationEntity operation = operationPersistenceService.getOperation(operationId);
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
                final GetUserDetailResponse userDetail = userLookupCustomizationService.lookupUser(username, organizationId, operation);
                if (userDetail == null) {
                    throw new UserNotFoundException("User not found, credential definition name: " + credentialName + ", username: " + username);
                }
                final LookupUserResponse response = new LookupUserResponse();
                response.setUser(userDetail);
                return response;
            }
        }

        // When username and credentialName are present, lookup the user identity, single result or no result is found
        final Optional<CredentialEntity> credentialOptional = credentialRepository.findByCredentialDefinitionAndUsername(credentialDefinition, username);
        if (!credentialOptional.isPresent()) {
            throw new UserNotFoundException("User not found, credential definition name: " + credentialName + ", username: " + username);
        }
        final CredentialEntity credential = credentialOptional.get();
        if (credential.getStatus() == CredentialStatus.REMOVED) {
            throw new UserNotFoundException("User not found, credential definition name: " + credentialName + ", username: " + username);
        }
        final String userId = credential.getUser().getUserId();
        final LookupUserResponse response = new LookupUserResponse();
        final GetUserDetailResponse userDetail = userIdentityService.getUserDetail(userId, credentialDefinition, false);
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
        final Optional<UserIdentityEntity> userOptional = userIdentityRepository.findById(userId);
        if (!userOptional.isPresent()) {
            throw new UserNotFoundException("User identity not found: " + userId);
        }
        final UserIdentityEntity user = userOptional.get();
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
        final Optional<UserIdentityEntity> userOptional = userIdentityRepository.findById(userId);
        if (!userOptional.isPresent()) {
            throw new UserNotFoundException("User identity not found: " + userId);
        }
        final UserIdentityEntity user = userOptional.get();
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
        final Optional<UserIdentityEntity> userOptional = userIdentityRepository.findById(userId);
        if (!userOptional.isPresent()) {
            return Optional.empty();
        }
        final UserIdentityEntity user = userOptional.get();
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
        final Optional<UserIdentityEntity> userOptional = userIdentityRepository.findById(userId);
        if (!userOptional.isPresent()) {
            return Optional.empty();
        }
        final UserIdentityEntity user = userOptional.get();
        if (!includeRemoved && user.getStatus() == UserIdentityStatus.REMOVED) {
            return Optional.empty();
        }
        return Optional.of(user);
    }
}