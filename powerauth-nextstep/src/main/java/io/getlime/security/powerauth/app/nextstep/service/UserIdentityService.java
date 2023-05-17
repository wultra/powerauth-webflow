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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.wultra.core.audit.base.Audit;
import com.wultra.core.audit.base.model.AuditDetail;
import io.getlime.security.powerauth.app.nextstep.converter.CredentialConverter;
import io.getlime.security.powerauth.app.nextstep.converter.ExtrasConverter;
import io.getlime.security.powerauth.app.nextstep.converter.UserContactConverter;
import io.getlime.security.powerauth.app.nextstep.converter.ValueListConverter;
import io.getlime.security.powerauth.app.nextstep.repository.OtpRepository;
import io.getlime.security.powerauth.app.nextstep.repository.RoleRepository;
import io.getlime.security.powerauth.app.nextstep.repository.UserIdentityRepository;
import io.getlime.security.powerauth.app.nextstep.repository.catalogue.RepositoryCatalogue;
import io.getlime.security.powerauth.app.nextstep.repository.model.entity.*;
import io.getlime.security.powerauth.app.nextstep.service.catalogue.ServiceCatalogue;
import io.getlime.security.powerauth.lib.nextstep.model.entity.CredentialDetail;
import io.getlime.security.powerauth.lib.nextstep.model.entity.CredentialSecretDetail;
import io.getlime.security.powerauth.lib.nextstep.model.entity.UserContactDetail;
import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.*;
import io.getlime.security.powerauth.lib.nextstep.model.exception.*;
import io.getlime.security.powerauth.lib.nextstep.model.request.*;
import io.getlime.security.powerauth.lib.nextstep.model.response.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This service handles persistence of user identities.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Service
public class UserIdentityService {

    private final Logger logger = LoggerFactory.getLogger(UserIdentityService.class);
    private static final String AUDIT_TYPE_USER_IDENTITY = "USER_IDENTITY";

    private final UserIdentityRepository userIdentityRepository;
    private final RoleRepository roleRepository;
    private final OtpRepository otpRepository;
    private final ServiceCatalogue serviceCatalogue;
    private final Audit audit;

    private final UserContactConverter userContactConverter = new UserContactConverter();
    private final CredentialConverter credentialConverter = new CredentialConverter();
    private final ExtrasConverter extrasConverter = new ExtrasConverter();
    private final ValueListConverter valueListConverter = new ValueListConverter();

    /**
     * Service constructor.
     * @param repositoryCatalogue Repository catalogue.
     * @param serviceCatalogue Service catalogue.
     * @param audit Audit interface.
     */
    @Autowired
    public UserIdentityService(RepositoryCatalogue repositoryCatalogue, @Lazy ServiceCatalogue serviceCatalogue, Audit audit) {
        this.userIdentityRepository = repositoryCatalogue.getUserIdentityRepository();
        this.roleRepository = repositoryCatalogue.getRoleRepository();
        this.otpRepository = repositoryCatalogue.getOtpRepository();
        this.serviceCatalogue = serviceCatalogue;
        this.audit = audit;
    }

    /**
     * Create a user identity.
     * @param request Create user identity request.
     * @return Create user identity response.
     * @throws UserAlreadyExistsException Thrown when user identity already exists.
     * @throws InvalidRequestException Thrown when request is invalid.
     * @throws CredentialDefinitionNotFoundException Thrown when credential definition is not found.
     * @throws InvalidConfigurationException Thrown when Next Step configuration is invalid.
     * @throws CredentialValidationFailedException Thrown when credential validation fails.
     * @throws EncryptionException Thrown when encryption or decryption fails.
     */
    @Transactional(rollbackFor = Throwable.class)
    public CreateUserResponse createUserIdentity(CreateUserRequest request) throws UserAlreadyExistsException, InvalidRequestException, CredentialDefinitionNotFoundException, InvalidConfigurationException, CredentialValidationFailedException, EncryptionException {
        final CredentialDefinitionService credentialDefinitionService = serviceCatalogue.getCredentialDefinitionService();
        final EndToEndEncryptionService endToEndEncryptionService = serviceCatalogue.getEndToEndEncryptionService();
        final CredentialService credentialService = serviceCatalogue.getCredentialService();

        final Optional<UserIdentityEntity> userOptional = userIdentityRepository.findById(request.getUserId());
        UserIdentityEntity user;
        Map<String, RoleEntity> roleEntities = new HashMap<>();
        if (request.getRoles() != null) {
            roleEntities = collectRoleEntities(request.getRoles());
        }
        final Map<String, CredentialDefinitionEntity> credentialDefinitions = new HashMap<>();
        if (request.getCredentials() != null) {
            for (CreateUserRequest.NewCredential credential : request.getCredentials()) {
                final CredentialDefinitionEntity credentialDefinition = credentialDefinitionService.findActiveCredentialDefinition(credential.getCredentialName());
                credentialDefinitions.put(credential.getCredentialName(), credentialDefinition);
            }
        }
        if (userOptional.isPresent()) {
            user = userOptional.get();
            if (user.getStatus() != UserIdentityStatus.REMOVED) {
                throw new UserAlreadyExistsException("User identity already exists: " + request.getUserId());
            }
            audit.info("User identity was reactivated", AuditDetail.builder()
                    .type(AUDIT_TYPE_USER_IDENTITY)
                    .param("userId", user.getUserId())
                    .build());
            user.setStatus(UserIdentityStatus.ACTIVE);
        } else {
            user = new UserIdentityEntity();
        }
        if (request.getExtras() != null) {
            try {
                final String extras = extrasConverter.fromMap(request.getExtras());
                user.setExtras(extras);
            } catch (JsonProcessingException ex) {
                throw new InvalidRequestException(ex);
            }
        }
        user.setUserId(request.getUserId());
        user.setStatus(UserIdentityStatus.ACTIVE);
        user.setTimestampCreated(new Date());

        final CreateUserResponse response = new CreateUserResponse();
        response.setUserId(user.getUserId());
        response.setUserIdentityStatus(user.getStatus());
        response.getExtras().putAll(request.getExtras());

        final List<CredentialSecretDetail> newCredentials = new ArrayList<>();
        if (request.getCredentials() != null) {
            for (CreateUserRequest.NewCredential credential : request.getCredentials()) {
                final List<CreateUserRequest.CredentialHistory> credentialHistory = credential.getCredentialHistory();
                final CredentialDefinitionEntity credentialDefinition = credentialDefinitions.get(credential.getCredentialName());
                String credentialValueRequest = credential.getCredentialValue();
                if (credentialValueRequest != null && credentialDefinition.isE2eEncryptionEnabled()) {
                    credentialValueRequest = endToEndEncryptionService.decryptCredential(credentialValueRequest, credentialDefinition);
                }
                CredentialValidationMode validationMode = credential.getValidationMode();
                if (validationMode == null) {
                    validationMode = CredentialValidationMode.VALIDATE_USERNAME_AND_CREDENTIAL;
                }
                final CredentialSecretDetail credentialDetail = credentialService.createCredential(user, credentialDefinition,
                        credential.getCredentialType(), credential.getUsername(), credentialValueRequest,
                        credential.getTimestampExpires(), validationMode);
                if (credentialHistory != null && !credentialHistory.isEmpty()) {
                    final int dateCount = credentialHistory.size();
                    // Use unique timestamps in seconds to keep order of credential history
                    long createdTimestamp = new Date().getTime() - (dateCount * 1000L);
                    for (CreateUserRequest.CredentialHistory h : credentialHistory) {
                        final Date createdDate = new Date(createdTimestamp);
                        String credentialValueHistory = h.getCredentialValue();
                        if (credentialDefinition.isE2eEncryptionEnabled()) {
                            credentialValueHistory = endToEndEncryptionService.decryptCredential(credentialValueHistory, credentialDefinition);
                        }
                        credentialService.importCredentialHistory(user, credentialDefinition, h.getUsername(), credentialValueHistory, createdDate);
                        createdTimestamp += 1000;
                    }
                }
                // Return generated credential value, with possible end2end encryption
                if (credentialValueRequest == null
                        && credentialDefinition.isE2eEncryptionEnabled()
                        && (credentialDetail.getCredentialType() == CredentialType.PERMANENT || credentialDefinition.isE2eEncryptionForTemporaryCredentialEnabled())) {
                    final String credentialValueResponse = credentialDetail.getCredentialValue();
                    credentialDetail.setCredentialValue(endToEndEncryptionService.encryptCredential(credentialValueResponse, credentialDefinition));
                }
                newCredentials.add(credentialDetail);
            }
        }
        // Remove inactive credentials, in case no credentials are sent in request, all credentials are removed
        removeInactiveCredentials(user, newCredentials);
        response.getCredentials().addAll(newCredentials);
        if (request.getRoles() != null) {
            updateRoles(user, request.getRoles(), roleEntities);
            response.getRoles().addAll(request.getRoles());
        }
        if (request.getContacts() != null) {
            final List<UserContactDetail> contacts = new ArrayList<>();
            request.getContacts().forEach(newContact -> {
                UserContactDetail contactDetail = new UserContactDetail();
                contactDetail.setContactName(newContact.getContactName());
                contactDetail.setContactType(newContact.getContactType());
                contactDetail.setContactValue(newContact.getContactValue());
                contactDetail.setPrimary(newContact.isPrimary());
                contacts.add(contactDetail);
            });
            final List<UserContactDetail> addedContacts = updateContacts(user, contacts);
            response.getContacts().addAll(addedContacts);
        }
        // Save user identity and a snapshot to the history table
        updateUserIdentityHistory(user);
        user = userIdentityRepository.save(user);
        logger.debug("User identity was created, user ID: {}", user.getUserId());
        audit.info("User identity was created", AuditDetail.builder()
                .type(AUDIT_TYPE_USER_IDENTITY)
                .param("userId", user.getUserId())
                .build());
        return response;
    }

    /**
     * Update user identity.
     * @param request Update user identity request.
     * @return Update user identity response.
     * @throws UserNotFoundException Thrown when user identity is not found.
     * @throws InvalidRequestException Thrown when request is invalid.
     * @throws CredentialDefinitionNotFoundException Thrown when credential definition is not found.
     * @throws InvalidConfigurationException Thrown when Next Step configuration is invalid.
     * @throws CredentialValidationFailedException Thrown when credential validation fails.
     * @throws EncryptionException Thrown when encryption or decryption fails.
     */
    @Transactional(rollbackFor = Throwable.class)
    public UpdateUserResponse updateUserIdentity(UpdateUserRequest request) throws UserNotFoundException, InvalidRequestException, CredentialDefinitionNotFoundException, InvalidConfigurationException, CredentialValidationFailedException, EncryptionException {
        final CredentialDefinitionService credentialDefinitionService = serviceCatalogue.getCredentialDefinitionService();
        final EndToEndEncryptionService endToEndEncryptionService = serviceCatalogue.getEndToEndEncryptionService();
        final CredentialService credentialService = serviceCatalogue.getCredentialService();

        UserIdentityEntity user = userIdentityRepository.findById(request.getUserId()).orElseThrow(() ->
                new UserNotFoundException("User identity not found: " + request.getUserId()));
        // The findUser() method is not used to allow update REMOVED -> ACTIVE
        Map<String, RoleEntity> roleEntities = new HashMap<>();
        if (request.getRoles() != null) {
            roleEntities = collectRoleEntities(request.getRoles());
        }
        final Map<String, CredentialDefinitionEntity> credentialDefinitions = new HashMap<>();
        if (request.getCredentials() != null) {
            for (UpdateUserRequest.UpdatedCredential credential : request.getCredentials()) {
                final CredentialDefinitionEntity credentialDefinition = credentialDefinitionService.findActiveCredentialDefinition(credential.getCredentialName());
                credentialDefinitions.put(credential.getCredentialName(), credentialDefinition);
            }
        }
        if (request.getUserIdentityStatus() != null) {
            user.setStatus(request.getUserIdentityStatus());
        }
        if (request.getExtras() != null) {
            try {
                final String extras = extrasConverter.fromMap(request.getExtras());
                user.setExtras(extras);
            } catch (JsonProcessingException ex) {
                throw new InvalidRequestException(ex);
            }
        }
        user.setTimestampLastUpdated(new Date());

        final UpdateUserResponse response = new UpdateUserResponse();
        response.setUserId(user.getUserId());
        response.setUserIdentityStatus(user.getStatus());
        if (request.getExtras() != null) {
            response.getExtras().putAll(request.getExtras());
        }
        final List<CredentialSecretDetail> newCredentials = new ArrayList<>();
        if (request.getCredentials() != null) {
            if (request.getUserIdentityStatus() != UserIdentityStatus.REMOVED) {
                // Update credentials and set their status to ACTIVE but only in case user identity status is not REMOVED
                for (UpdateUserRequest.UpdatedCredential credential : request.getCredentials()) {
                    final CredentialDefinitionEntity credentialDefinition = credentialDefinitions.get(credential.getCredentialName());
                    String credentialValueRequest = credential.getCredentialValue();
                    if (credentialValueRequest != null && credentialDefinition.isE2eEncryptionEnabled()) {
                        credentialValueRequest = endToEndEncryptionService.decryptCredential(credentialValueRequest, credentialDefinition);
                    }
                    final CredentialSecretDetail credentialDetail = credentialService.createCredential(user, credentialDefinition,
                            credential.getCredentialType(), credential.getUsername(), credentialValueRequest,
                            credential.getTimestampExpires(), CredentialValidationMode.VALIDATE_USERNAME_AND_CREDENTIAL);
                    // Return generated credential value, with possible end2end encryption
                    if (credentialValueRequest == null && credentialDefinition.isE2eEncryptionEnabled()) {
                        final String credentialValueResponse = credentialDetail.getCredentialValue();
                        credentialDetail.setCredentialValue(endToEndEncryptionService.encryptCredential(credentialValueResponse, credentialDefinition));
                    }
                    newCredentials.add(credentialDetail);
                }
            }
            // Remove inactive credentials, when requested status is REMOVED, all credentials are removed
            removeInactiveCredentials(user, newCredentials);
        }
        response.getCredentials().addAll(newCredentials);
        if (request.getRoles() != null) {
            // Roles from the request are set, obsolete roles are removed
            updateRoles(user, request.getRoles(), roleEntities);
            response.getRoles().addAll(request.getRoles());
        }
        if (request.getContacts() != null) {
            // Contacts from the request are set, obsolete contacts are removed
            final List<UserContactDetail> contacts = new ArrayList<>();
            request.getContacts().forEach(newContact -> {
                UserContactDetail contactDetail = new UserContactDetail();
                contactDetail.setContactName(newContact.getContactName());
                contactDetail.setContactType(newContact.getContactType());
                contactDetail.setContactValue(newContact.getContactValue());
                contactDetail.setPrimary(newContact.isPrimary());
                contacts.add(contactDetail);
            });
            final List<UserContactDetail> updatedContacts = updateContacts(user, contacts);
            response.getContacts().addAll(updatedContacts);
        }
        // Save user identity snapshot to the history table
        updateUserIdentityHistory(user);
        user = userIdentityRepository.save(user);
        logger.debug("User identity was updated, user ID: {}", user.getUserId());
        audit.info("User identity was updated", AuditDetail.builder()
                .type(AUDIT_TYPE_USER_IDENTITY)
                .param("userId", user.getUserId())
                .build());
        return response;
    }

    /**
     * Get user identity detail.
     * @param request User identity detail request.
     * @return User identity detail response.
     * @throws UserNotFoundException Thrown when user identity is not found.
     * @throws InvalidRequestException Thrown when request is invalid.
     * @throws InvalidConfigurationException Thrown when Next Step configuration is invalid.
     * @throws EncryptionException Thrown when decryption fails.
     * @throws CredentialDefinitionNotFoundException Thrown when credential definition is not found.
     */
    @Transactional
    public GetUserDetailResponse getUserDetail(GetUserDetailRequest request) throws UserNotFoundException, InvalidRequestException, InvalidConfigurationException, EncryptionException, CredentialDefinitionNotFoundException {
        final CredentialDefinitionService credentialDefinitionService = serviceCatalogue.getCredentialDefinitionService();
        CredentialDefinitionEntity credentialDefinition = null;
        final String credentialName = request.getCredentialName();
        if (credentialName != null) {
            credentialDefinition = credentialDefinitionService.findActiveCredentialDefinition(request.getCredentialName());
        }
        return getUserDetail(request.getUserId(), credentialDefinition, request.isIncludeRemoved());
    }

    /**
     * Get user identity detail. This method is not transactional.
     * @param userId User ID.
     * @param credentialDefinition Credential definition for optional filter.
     * @param includeRemoved Whether removed data should be returned.
     * @return User identity detail response.
     * @throws UserNotFoundException Thrown when user identity is not found.
     * @throws InvalidRequestException Thrown when request is invalid.
     * @throws InvalidConfigurationException Thrown when Next Step configuration is invalid.
     * @throws EncryptionException Thrown when decryption fails.
     */
    public GetUserDetailResponse getUserDetail(String userId, CredentialDefinitionEntity credentialDefinition, boolean includeRemoved) throws UserNotFoundException, InvalidRequestException, InvalidConfigurationException, EncryptionException {
        final UserIdentityLookupService userIdentityLookupService = serviceCatalogue.getUserIdentityLookupService();
        final CredentialService credentialService = serviceCatalogue.getCredentialService();
        final UserIdentityEntity user = userIdentityLookupService.findUser(userId, includeRemoved);
        final GetUserDetailResponse response = new GetUserDetailResponse();
        response.setUserId(user.getUserId());
        response.setUserIdentityStatus(user.getStatus());
        response.setTimestampCreated(user.getTimestampCreated());
        response.setTimestampLastUpdated(user.getTimestampLastUpdated());
        if (user.getExtras() != null) {
            try {
                final Map<String, Object> extras = extrasConverter.fromString(user.getExtras());
                response.getExtras().putAll(extras);
            } catch (JsonProcessingException ex) {
                throw new InvalidRequestException(ex);
            }
        }
        final List<UserRoleEntity> userRoles = user.getRoles().stream().filter(r -> r.getStatus() == UserRoleStatus.ACTIVE).toList();
        userRoles.forEach(userRole -> response.getRoles().add(userRole.getRole().getName()));
        final Set<UserContactEntity> userContacts = user.getContacts();
        for (UserContactEntity userContact: userContacts) {
            final UserContactDetail contactDetail = userContactConverter.fromEntity(userContact);
            response.getContacts().add(contactDetail);
        }
        final Set<CredentialEntity> credentials = user.getCredentials();
        for (CredentialEntity credential: credentials) {
            if (credential.getStatus() == CredentialStatus.REMOVED && !includeRemoved) {
                continue;
            }
            // Apply filter by credential definition if requested
            if (credentialDefinition != null && !credential.getCredentialDefinition().equals(credentialDefinition)) {
                continue;
            }
            final CredentialDetail credentialDetail = credentialConverter.fromEntity(credential);
            final boolean credentialChangeRequired;
            if (credential.getCredentialDefinition().getHashingConfig() == null) {
                credentialChangeRequired = credentialService.isCredentialChangeRequired(credential, credential.getValue());
            } else {
                credentialChangeRequired = credentialService.isCredentialChangeRequired(credential, null);
            }
            credentialDetail.setCredentialChangeRequired(credentialChangeRequired);
            response.getCredentials().add(credentialDetail);
        }
        return response;
    }

    /**
     * Update multiple user statuses.
     * @param request Update users request.
     * @return Update users response.
     * @throws UserNotFoundException Thrown when no user identity is found.
     */
    @Transactional
    public UpdateUsersResponse updateUsers(UpdateUsersRequest request) throws UserNotFoundException {
        final List<String> updatedUserIds = new ArrayList<>();
        try (final Stream<UserIdentityEntity> users = userIdentityRepository.findAllByUserIdIn(request.getUserIds())) {
            users.forEach(user -> {
                if (user.getStatus() != request.getUserIdentityStatus()) {
                    user.setStatus(request.getUserIdentityStatus());
                    user.setTimestampLastUpdated(new Date());
                    // Save user identity and a snapshot to the history table
                    updateUserIdentityHistory(user);
                    user = userIdentityRepository.save(user);
                    logger.debug("User identity was updated, user ID: {}", user.getUserId());
                    audit.info("User identity was updated", AuditDetail.builder()
                            .type(AUDIT_TYPE_USER_IDENTITY)
                            .param("userId", user.getUserId())
                            .param("userStatus", user.getStatus())
                            .build());
                }
                updatedUserIds.add(user.getUserId());
            });
        }
        if (updatedUserIds.isEmpty()) {
            throw new UserNotFoundException("No user identity found for update");
        }
        final UpdateUsersResponse response = new UpdateUsersResponse();
        response.getUserIds().addAll(updatedUserIds);
        response.setUserIdentityStatus(request.getUserIdentityStatus());
        return response;
    }

    /**
     * Delete a user identity.
     * @param request Delete user identity request.
     * @return Delete user identity response.
     * @throws UserNotFoundException Thrown when user identity is not found.
     */
    @Transactional
    public DeleteUserResponse deleteUser(DeleteUserRequest request) throws UserNotFoundException {
        final UserIdentityLookupService userIdentityLookupService = serviceCatalogue.getUserIdentityLookupService();
        UserIdentityEntity user = userIdentityLookupService.findUser(request.getUserId());
        user.setStatus(UserIdentityStatus.REMOVED);
        user.setTimestampLastUpdated(new Date());
        removeAllCredentials(user);
        removeAllOtps(user);
        // Save user identity and a snapshot to the history table
        updateUserIdentityHistory(user);
        user = userIdentityRepository.save(user);
        logger.debug("User identity was removed, user ID: {}", user.getUserId());
        audit.info("User identity was removed", AuditDetail.builder()
                .type(AUDIT_TYPE_USER_IDENTITY)
                .param("userId", user.getUserId())
                .build());
        final DeleteUserResponse response = new DeleteUserResponse();
        response.setUserId(user.getUserId());
        response.setUserIdentityStatus(user.getStatus());
        return response;
    }

    /**
     * Block a user identity.
     * @param request Block user identity request.
     * @return Block user identity response.
     * @throws UserNotFoundException Thrown when user identity is not found.
     * @throws UserNotActiveException Thrown when user identity is not active.
     */
    @Transactional
    public BlockUserResponse blockUser(BlockUserRequest request) throws UserNotFoundException, UserNotActiveException {
        final UserIdentityLookupService userIdentityLookupService = serviceCatalogue.getUserIdentityLookupService();
        UserIdentityEntity user = userIdentityLookupService.findUser(request.getUserId());
        if (user.getStatus() != UserIdentityStatus.ACTIVE) {
            throw new UserNotActiveException("User identity is not ACTIVE: " + request.getUserId());
        }
        user.setStatus(UserIdentityStatus.BLOCKED);
        user.setTimestampLastUpdated(new Date());
        // Save user identity and a snapshot to the history table
        updateUserIdentityHistory(user);
        user = userIdentityRepository.save(user);
        logger.debug("User identity was blocked, user ID: {}", user.getUserId());
        audit.info("User identity was blocked", AuditDetail.builder()
                .type(AUDIT_TYPE_USER_IDENTITY)
                .param("userId", user.getUserId())
                .build());
        final BlockUserResponse response = new BlockUserResponse();
        response.setUserId(user.getUserId());
        response.setUserIdentityStatus(user.getStatus());
        return response;
    }

    /**
     * Unblock a user identity.
     * @param request Unblock user identity request.
     * @return Unblock user identity response.
     * @throws UserNotFoundException Thrown when user identity is not found.
     * @throws UserNotBlockedException Thrown when user identity is not blocked.
     */
    @Transactional
    public UnblockUserResponse unblockUser(UnblockUserRequest request) throws UserNotFoundException, UserNotBlockedException {
        final UserIdentityLookupService userIdentityLookupService = serviceCatalogue.getUserIdentityLookupService();
        UserIdentityEntity user = userIdentityLookupService.findUser(request.getUserId());
        if (user.getStatus() != UserIdentityStatus.BLOCKED) {
            throw new UserNotBlockedException("User identity is not BLOCKED: " + request.getUserId());
        }
        user.setStatus(UserIdentityStatus.ACTIVE);
        user.setTimestampLastUpdated(new Date());
        // Save user identity and a snapshot to the history table
        updateUserIdentityHistory(user);
        user = userIdentityRepository.save(user);
        logger.debug("User identity was unblocked, user ID: {}", user.getUserId());
        audit.info("User identity was unblocked", AuditDetail.builder()
                .type(AUDIT_TYPE_USER_IDENTITY)
                .param("userId", user.getUserId())
                .build());
        final UnblockUserResponse response = new UnblockUserResponse();
        response.setUserId(user.getUserId());
        response.setUserIdentityStatus(user.getStatus());
        return response;
    }

    /**
     * Save snapshot of user identity into user identity history. This method is not transactional.
     * @param user User identity entity.
     */
    public void updateUserIdentityHistory(UserIdentityEntity user) {
        final UserIdentityHistoryEntity history = new UserIdentityHistoryEntity();
        history.setUser(user);
        history.setStatus(user.getStatus());
        final Set<UserRoleEntity> userRoles = user.getRoles();
        final List<String> roles = userRoles.stream()
                .filter(role -> role.getStatus() == UserRoleStatus.ACTIVE)
                .map(role -> role.getRole().getName())
                .collect(Collectors.toList());
        try {
            history.setRoles(valueListConverter.fromList(roles));
        } catch (JsonProcessingException ex) {
            // Ignore
        }
        history.setExtras(user.getExtras());
        history.setTimestampCreated(new Date());
        user.getUserIdentityHistory().add(history);
    }

    /**
     * Collect roles into role entities.
     *
     * @param roles Role names.
     * @return Role entities.
     * @throws InvalidRequestException Thrown in case any of the roles is not defined.
     */
    private Map<String, RoleEntity> collectRoleEntities(List<String> roles) throws InvalidRequestException {
        final Map<String, RoleEntity> roleEntities = new HashMap<>();
        for (String roleName : roles) {
            final RoleEntity role = roleRepository.findByName(roleName).orElseThrow(() ->
                    new InvalidRequestException("User role not found: " + roleName));
            roleEntities.put(roleName, role);
        }
        return roleEntities;
    }

    /**
     * Update roles for user, merge state in the request and in the database.
     *
     * @param user User identity entity.
     * @param roles User roles to be set.
     * @param roleEntities Role entities present in the database.
     */
    private void updateRoles(UserIdentityEntity user, List<String> roles, Map<String, RoleEntity> roleEntities) {
        final Set<UserRoleEntity> existingRoles = user.getRoles();
        final Map<String, UserRoleEntity> existingRoleMap = new HashMap<>();
        existingRoles.forEach(userRole -> existingRoleMap.put(userRole.getRole().getName(), userRole));
        for (String roleToAdd : roles) {
            final UserRoleEntity existingRole = existingRoleMap.get(roleToAdd);
            if (existingRole == null) {
                // Persist new role
                final UserRoleEntity userRole = new UserRoleEntity();
                userRole.setUser(user);
                userRole.setRole(roleEntities.get(roleToAdd));
                userRole.setStatus(UserRoleStatus.ACTIVE);
                userRole.setTimestampCreated(new Date());
                user.getRoles().add(userRole);
            } else if (existingRole.getStatus() == UserRoleStatus.REMOVED) {
                // Make removed role active
                existingRole.setStatus(UserRoleStatus.ACTIVE);
                existingRole.setTimestampLastUpdated(new Date());
            }
        }
        existingRoleMap.keySet().forEach(roleName -> {
            if (!roles.contains(roleName)) {
                // Remove role if not already removed
                final UserRoleEntity existingRole = existingRoleMap.get(roleName);
                if (existingRole.getStatus() != UserRoleStatus.REMOVED) {
                    existingRole.setStatus(UserRoleStatus.REMOVED);
                }
            }
        });
    }

    /**
     * Update contacts for user, merge state in the request and in the database.
     *
     * @param user User identity entity.
     * @param contacts Contacts to be set.
     * @return Contact details with resolved timestamps after merge with status in the database.
     */
    private List<UserContactDetail> updateContacts(UserIdentityEntity user, List<UserContactDetail> contacts) {
        final UserContactService userContactService = serviceCatalogue.getUserContactService();
        final List<UserContactDetail> contactListResponse = new ArrayList<>();
        final Set<UserContactEntity> existingContacts = user.getContacts();
        final Map<String, UserContactEntity> existingContactMap = new HashMap<>();
        existingContacts.forEach(userContact -> existingContactMap.put(userContact.getName(), userContact));
        // Persist new or update existing contacts
        for (UserContactDetail contactToAdd : contacts) {
            UserContactEntity userContact = existingContactMap.get(contactToAdd.getContactName());
            if (userContact == null) {
                userContact = new UserContactEntity();
                userContact.setName(contactToAdd.getContactName());
                userContact.setUser(user);
                userContact.setTimestampCreated(new Date());
                user.getContacts().add(userContact);
            } else {
                userContact.setTimestampLastUpdated(new Date());
            }
            userContact.setType(contactToAdd.getContactType());
            userContact.setValue(contactToAdd.getContactValue());
            userContact.setPrimary(contactToAdd.isPrimary());
            final UserContactDetail contactDetail = new UserContactDetail();
            contactDetail.setContactName(userContact.getName());
            contactDetail.setContactType(userContact.getType());
            contactDetail.setContactValue(userContact.getValue());
            contactDetail.setPrimary(userContact.isPrimary());
            contactDetail.setTimestampCreated(userContact.getTimestampCreated());
            contactDetail.setTimestampLastUpdated(userContact.getTimestampLastUpdated());
            contactListResponse.add(contactDetail);
        }
        // Remove obsolete contacts
        existingContactMap.forEach((contactName, contactEntity) -> {
            if (contacts.stream().noneMatch(c -> c.getContactName().equals(contactName))) {
                user.getContacts().remove(contactEntity);
            }
        });
        // Ensure primary contacts are unique
        userContactService.ensurePrimaryContactsAreUnique(user);
        return contactListResponse;
    }

    /**
     * Remove all inactive credentials for user.
     * @param user User identity entity.
     * @param activeCredentials Credentials which should remain active.
     */
    private void removeInactiveCredentials(UserIdentityEntity user, List<CredentialSecretDetail> activeCredentials) {
        final Set<CredentialEntity> existingCredentials = user.getCredentials();
        final List<String> credentialsToKeep = activeCredentials
                .stream()
                .map(CredentialSecretDetail::getCredentialName)
                .toList();
        existingCredentials.forEach(credential -> {
            if (!credentialsToKeep.contains(credential.getCredentialDefinition().getName())
                    && credential.getStatus() != CredentialStatus.REMOVED) {
                credential.setStatus(CredentialStatus.REMOVED);
            }
        });
    }

    /**
     * Remove all credentials for user.
     * @param user User identity entity.
     */
    private void removeAllCredentials(UserIdentityEntity user) {
        final Set<CredentialEntity> existingCredentials = user.getCredentials();
        existingCredentials.forEach(credential -> {
            if (credential.getStatus() != CredentialStatus.REMOVED) {
                credential.setStatus(CredentialStatus.REMOVED);
            }
        });
    }

    /**
     * Remove all OTPs for user.
     * @param user User identity entity.
     */
    private void removeAllOtps(UserIdentityEntity user) {
        otpRepository.removeOtpsForUserId(user.getUserId());
    }

}