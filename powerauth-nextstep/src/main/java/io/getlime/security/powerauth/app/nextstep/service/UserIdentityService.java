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

import com.fasterxml.jackson.core.JsonProcessingException;
import io.getlime.security.powerauth.app.nextstep.converter.CredentialConverter;
import io.getlime.security.powerauth.app.nextstep.converter.ExtrasConverter;
import io.getlime.security.powerauth.app.nextstep.converter.UserContactConverter;
import io.getlime.security.powerauth.app.nextstep.converter.ValueListConverter;
import io.getlime.security.powerauth.app.nextstep.repository.*;
import io.getlime.security.powerauth.app.nextstep.repository.model.entity.*;
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
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.*;
import java.util.stream.Collectors;

/**
 * This service handles persistence of user identities.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Service
public class UserIdentityService {

    private final Logger logger = LoggerFactory.getLogger(UserIdentityService.class);

    private final UserIdentityRepository userIdentityRepository;
    private final UserIdentityHistoryRepository userIdentityHistoryRepository;
    private final UserContactService userContactService;
    private final UserContactRepository userContactRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final CredentialDefinitionRepository credentialDefinitionRepository;
    private final CredentialRepository credentialRepository;
    private final OtpRepository otpRepository;
    private final UserIdentityLookupService userIdentityLookupService;
    private final CredentialService credentialService;
    private final EndToEndEncryptionService endToEndEncryptionService;

    private final UserContactConverter userContactConverter = new UserContactConverter();
    private final CredentialConverter credentialConverter = new CredentialConverter();
    private final ExtrasConverter extrasConverter = new ExtrasConverter();
    private final ValueListConverter valueListConverter = new ValueListConverter();

    /**
     * Service constructor.
     * @param userIdentityRepository User identity repository.
     * @param userIdentityHistoryRepository User identity history repository.
     * @param userContactService User contact service.
     * @param userContactRepository User contact repository.
     * @param roleRepository Role repository.
     * @param userRoleRepository User role repository.
     * @param credentialDefinitionRepository Credential definition repository.
     * @param credentialRepository Credential repository.
     * @param otpRepository OTP repository.
     * @param userIdentityLookupService User identity lookup service.
     * @param credentialService Credential service.
     * @param endToEndEncryptionService End-to-end encryption service.
     */
    @Autowired
    public UserIdentityService(UserIdentityRepository userIdentityRepository, UserIdentityHistoryRepository userIdentityHistoryRepository, UserContactService userContactService, UserContactRepository userContactRepository, RoleRepository roleRepository, UserRoleRepository userRoleRepository, CredentialDefinitionRepository credentialDefinitionRepository, CredentialRepository credentialRepository, OtpRepository otpRepository, UserIdentityLookupService userIdentityLookupService, CredentialService credentialService, EndToEndEncryptionService endToEndEncryptionService) {
        this.userIdentityRepository = userIdentityRepository;
        this.userIdentityHistoryRepository = userIdentityHistoryRepository;
        this.userContactService = userContactService;
        this.userContactRepository = userContactRepository;
        this.roleRepository = roleRepository;
        this.userRoleRepository = userRoleRepository;
        this.credentialDefinitionRepository = credentialDefinitionRepository;
        this.credentialRepository = credentialRepository;
        this.otpRepository = otpRepository;
        this.userIdentityLookupService = userIdentityLookupService;
        this.credentialService = credentialService;
        this.endToEndEncryptionService = endToEndEncryptionService;
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
    @Transactional(rollbackOn = Throwable.class)
    public CreateUserResponse createUserIdentity(CreateUserRequest request) throws UserAlreadyExistsException, InvalidRequestException, CredentialDefinitionNotFoundException, InvalidConfigurationException, CredentialValidationFailedException, EncryptionException {
        Optional<UserIdentityEntity> userOptional = userIdentityRepository.findById(request.getUserId());
        UserIdentityEntity user;
        Map<String, RoleEntity> roleEntities = new HashMap<>();
        if (request.getRoles() != null) {
            roleEntities = collectRoleEntities(request.getRoles());
        }
        Map<String, CredentialDefinitionEntity> credentialDefinitions = new HashMap<>();
        if (request.getCredentials() != null) {
            for (CreateUserRequest.NewCredential credential : request.getCredentials()) {
                Optional<CredentialDefinitionEntity> credentialDefinitionOptional = credentialDefinitionRepository.findByName(credential.getCredentialName());
                if (!credentialDefinitionOptional.isPresent()) {
                    throw new CredentialDefinitionNotFoundException("Credential definition not found: " + credential.getCredentialName());
                }
                CredentialDefinitionEntity credentialDefinition = credentialDefinitionOptional.get();
                if (credentialDefinition.getStatus() != CredentialDefinitionStatus.ACTIVE) {
                    throw new CredentialDefinitionNotFoundException("Credential definition is not ACTIVE: " + credential.getCredentialName());
                }
                credentialDefinitions.put(credential.getCredentialName(), credentialDefinition);
            }
        }
        if (userOptional.isPresent()) {
            user = userOptional.get();
            if (user.getStatus() != UserIdentityStatus.REMOVED) {
                throw new UserAlreadyExistsException("User identity already exists: " + request.getUserId());
            }
            // Revive user identity
            // TODO - auditing
            user.setStatus(UserIdentityStatus.ACTIVE);
        } else {
            user = new UserIdentityEntity();
        }
        if (request.getExtras() != null) {
            try {
                String extras = extrasConverter.fromMap(request.getExtras());
                user.setExtras(extras);
            } catch (JsonProcessingException ex) {
                throw new InvalidRequestException(ex);
            }
        }
        user.setUserId(request.getUserId());
        user.setStatus(UserIdentityStatus.ACTIVE);
        user.setTimestampCreated(new Date());
        userIdentityRepository.save(user);

        CreateUserResponse response = new CreateUserResponse();
        response.setUserId(user.getUserId());
        response.setUserIdentityStatus(user.getStatus());
        response.getExtras().putAll(request.getExtras());

        List<CredentialSecretDetail> newCredentials = new ArrayList<>();
        if (request.getCredentials() != null) {
            for (CreateUserRequest.NewCredential credential : request.getCredentials()) {
                List<CreateUserRequest.CredentialHistory> credentialHistory = credential.getCredentialHistory();
                CredentialDefinitionEntity credentialDefinition = credentialDefinitions.get(credential.getCredentialName());
                String credentialValueRequest = credential.getCredentialValue();
                if (credentialValueRequest != null && credentialDefinition.isE2eEncryptionEnabled()) {
                    credentialValueRequest = endToEndEncryptionService.decryptCredential(credentialValueRequest, credentialDefinition);
                }
                CredentialValidationMode validationMode = credential.getValidationMode();
                if (validationMode == null) {
                    validationMode = CredentialValidationMode.VALIDATE_USERNAME_AND_CREDENTIAL;
                }
                CredentialSecretDetail credentialDetail = credentialService.createCredential(user, credentialDefinition,
                        credential.getCredentialType(), credential.getUsername(), credentialValueRequest, validationMode);
                if (credentialHistory != null && !credentialHistory.isEmpty()) {
                    int dateCount = credentialHistory.size();
                    // Use unique timestamps in seconds to keep order of credential history
                    long createdTimestamp = new Date().getTime() - (dateCount * 1000L);
                    for (CreateUserRequest.CredentialHistory h : credentialHistory) {
                        Date createdDate = new Date(createdTimestamp);
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
                    String credentialValueResponse = credentialDetail.getCredentialValue();
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
            List<UserContactDetail> contacts = new ArrayList<>();
            request.getContacts().forEach(newContact -> {
                UserContactDetail contactDetail = new UserContactDetail();
                contactDetail.setContactName(newContact.getContactName());
                contactDetail.setContactType(newContact.getContactType());
                contactDetail.setContactValue(newContact.getContactValue());
                contactDetail.setPrimary(newContact.isPrimary());
                contacts.add(contactDetail);
            });
            List<UserContactDetail> addedContacts = updateContacts(user, contacts);
            response.getContacts().addAll(addedContacts);
        }
        // Save user identity snapshot to the history table
        saveUserIdentityHistory(user);
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
    @Transactional(rollbackOn = Throwable.class)
    public UpdateUserResponse updateUserIdentity(UpdateUserRequest request) throws UserNotFoundException, InvalidRequestException, CredentialDefinitionNotFoundException, InvalidConfigurationException, CredentialValidationFailedException, EncryptionException {
        Optional<UserIdentityEntity> userOptional = userIdentityRepository.findById(request.getUserId());
        if (!userOptional.isPresent()) {
            throw new UserNotFoundException("User identity not found: " + request.getUserId());
        }
        // The findUser() method is not used to allow update REMOVED -> ACTIVE
        UserIdentityEntity user = userOptional.get();
        Map<String, RoleEntity> roleEntities = new HashMap<>();
        if (request.getRoles() != null) {
            roleEntities = collectRoleEntities(request.getRoles());
        }
        Map<String, CredentialDefinitionEntity> credentialDefinitions = new HashMap<>();
        if (request.getCredentials() != null) {
            for (UpdateUserRequest.UpdatedCredential credential : request.getCredentials()) {
                Optional<CredentialDefinitionEntity> credentialDefinitionOptional = credentialDefinitionRepository.findByName(credential.getCredentialName());
                if (!credentialDefinitionOptional.isPresent()) {
                    throw new CredentialDefinitionNotFoundException("Credential definition not found: " + credential.getCredentialName());
                }
                CredentialDefinitionEntity credentialDefinition = credentialDefinitionOptional.get();
                if (credentialDefinition.getStatus() != CredentialDefinitionStatus.ACTIVE) {
                    throw new CredentialDefinitionNotFoundException("Credential definition is not ACTIVE: " + credential.getCredentialName());
                }
                credentialDefinitions.put(credential.getCredentialName(), credentialDefinition);
            }
        }
        if (request.getUserIdentityStatus() != null) {
            user.setStatus(request.getUserIdentityStatus());
        }
        if (request.getExtras() != null) {
            try {
                String extras = extrasConverter.fromMap(request.getExtras());
                user.setExtras(extras);
            } catch (JsonProcessingException ex) {
                throw new InvalidRequestException(ex);
            }
        }
        user.setTimestampLastUpdated(new Date());
        userIdentityRepository.save(user);

        UpdateUserResponse response = new UpdateUserResponse();
        response.setUserId(user.getUserId());
        response.setUserIdentityStatus(user.getStatus());
        if (request.getExtras() != null) {
            response.getExtras().putAll(request.getExtras());
        }
        List<CredentialSecretDetail> newCredentials = new ArrayList<>();
        if (request.getCredentials() != null) {
            if (request.getUserIdentityStatus() != UserIdentityStatus.REMOVED) {
                // Update credentials and set their status to ACTIVE but only in case user identity status is not REMOVED
                for (UpdateUserRequest.UpdatedCredential credential : request.getCredentials()) {
                    CredentialDefinitionEntity credentialDefinition = credentialDefinitions.get(credential.getCredentialName());
                    String credentialValueRequest = credential.getCredentialValue();
                    if (credentialValueRequest != null && credentialDefinition.isE2eEncryptionEnabled()) {
                        credentialValueRequest = endToEndEncryptionService.decryptCredential(credentialValueRequest, credentialDefinition);
                    }
                    CredentialSecretDetail credentialDetail = credentialService.createCredential(user, credentialDefinition,
                            credential.getCredentialType(), credential.getUsername(), credentialValueRequest, CredentialValidationMode.VALIDATE_USERNAME_AND_CREDENTIAL);
                    // Return generated credential value, with possible end2end encryption
                    if (credentialValueRequest == null && credentialDefinition.isE2eEncryptionEnabled()) {
                        String credentialValueResponse = credentialDetail.getCredentialValue();
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
            List<UserContactDetail> contacts = new ArrayList<>();
            request.getContacts().forEach(newContact -> {
                UserContactDetail contactDetail = new UserContactDetail();
                contactDetail.setContactName(newContact.getContactName());
                contactDetail.setContactType(newContact.getContactType());
                contactDetail.setContactValue(newContact.getContactValue());
                contactDetail.setPrimary(newContact.isPrimary());
                contacts.add(contactDetail);
            });
            List<UserContactDetail> updatedContacts = updateContacts(user, contacts);
            response.getContacts().addAll(updatedContacts);
        }
        // Save user identity snapshot to the history table
        saveUserIdentityHistory(user);
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
     */
    @Transactional
    public GetUserDetailResponse getUserDetail(GetUserDetailRequest request) throws UserNotFoundException, InvalidRequestException, InvalidConfigurationException, EncryptionException {
        return getUserDetail(request.getUserId(), request.isIncludeRemoved());
    }

    /**
     * Get user identity detail. This method is not transactional.
     * @param userId User ID.
     * @param includeRemoved Whether removed data should be returned.
     * @return User identity detail response.
     * @throws UserNotFoundException Thrown when user identity is not found.
     * @throws InvalidRequestException Thrown when request is invalid.
     * @throws InvalidConfigurationException Thrown when Next Step configuration is invalid.
     * @throws EncryptionException Thrown when decryption fails.
     */
    public GetUserDetailResponse getUserDetail(String userId, boolean includeRemoved) throws UserNotFoundException, InvalidRequestException, InvalidConfigurationException, EncryptionException {
        UserIdentityEntity user = userIdentityLookupService.findUser(userId, includeRemoved);
        GetUserDetailResponse response = new GetUserDetailResponse();
        response.setUserId(user.getUserId());
        response.setUserIdentityStatus(user.getStatus());
        response.setTimestampCreated(user.getTimestampCreated());
        response.setTimestampLastUpdated(user.getTimestampLastUpdated());
        if (user.getExtras() != null) {
            try {
                Map<String, Object> extras = extrasConverter.fromString(user.getExtras());
                response.getExtras().putAll(extras);
            } catch (JsonProcessingException ex) {
                throw new InvalidRequestException(ex);
            }
        }
        List<UserRoleEntity> userRoles = userRoleRepository.findAllByUserAndStatus(user, UserRoleStatus.ACTIVE);
        userRoles.forEach(userRole -> response.getRoles().add(userRole.getRole().getName()));
        List<UserContactEntity> userContacts = userContactRepository.findAllByUser(user);
        for (UserContactEntity userContact: userContacts) {
            UserContactDetail contactDetail = userContactConverter.fromEntity(userContact);
            response.getContacts().add(contactDetail);
        }
        List<CredentialEntity> credentials = credentialRepository.findAllByUser(user);
        for (CredentialEntity credential: credentials) {
            if (credential.getStatus() == CredentialStatus.REMOVED && !includeRemoved) {
                continue;
            }
            CredentialDetail credentialDetail = credentialConverter.fromEntity(credential);
            boolean credentialChangeRequired;
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
        Iterable<UserIdentityEntity> users = userIdentityRepository.findAllById(request.getUserIds());
        List<String> updatedUserIds = new ArrayList<>();
        for (UserIdentityEntity user: users) {
            if (user.getStatus() != request.getUserIdentityStatus()) {
                user.setStatus(request.getUserIdentityStatus());
                // Save user identity and a snapshot to the history table
                userIdentityRepository.save(user);
                saveUserIdentityHistory(user);
            }
            updatedUserIds.add(user.getUserId());
        }
        if (updatedUserIds.isEmpty()) {
            throw new UserNotFoundException("No user identity found for update");
        }
        UpdateUsersResponse response = new UpdateUsersResponse();
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
        UserIdentityEntity user = userIdentityLookupService.findUser(request.getUserId());
        user.setStatus(UserIdentityStatus.REMOVED);
        // Save user identity and a snapshot to the history table
        userIdentityRepository.save(user);
        saveUserIdentityHistory(user);
        removeAllCredentials(user);
        removeAllOtps(user);
        DeleteUserResponse response = new DeleteUserResponse();
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
        UserIdentityEntity user = userIdentityLookupService.findUser(request.getUserId());
        if (user.getStatus() != UserIdentityStatus.ACTIVE) {
            throw new UserNotActiveException("User identity is not BLOCKED: " + request.getUserId());
        }
        user.setStatus(UserIdentityStatus.BLOCKED);
        // Save user identity and a snapshot to the history table
        userIdentityRepository.save(user);
        saveUserIdentityHistory(user);
        BlockUserResponse response = new BlockUserResponse();
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
        UserIdentityEntity user = userIdentityLookupService.findUser(request.getUserId());
        if (user.getStatus() != UserIdentityStatus.BLOCKED) {
            throw new UserNotBlockedException("User identity is not BLOCKED: " + request.getUserId());
        }
        user.setStatus(UserIdentityStatus.ACTIVE);
        // Save user identity and a snapshot to the history table
        userIdentityRepository.save(user);
        saveUserIdentityHistory(user);
        UnblockUserResponse response = new UnblockUserResponse();
        response.setUserId(user.getUserId());
        response.setUserIdentityStatus(user.getStatus());
        return response;
    }

    /**
     * Save snapshot of user identity into user identity history. This method is not transactional.
     * @param user User identity entity.
     */
    public void saveUserIdentityHistory(UserIdentityEntity user) {
        UserIdentityHistoryEntity history = new UserIdentityHistoryEntity();
        history.setUser(user);
        history.setStatus(user.getStatus());
        List<UserRoleEntity> userRoles = userRoleRepository.findAllByUser(user);
        List<String> roles = userRoles.stream()
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
        userIdentityHistoryRepository.save(history);
    }

    /**
     * Collect roles into role entities.
     *
     * @param roles Role names.
     * @return Role entities.
     * @throws InvalidRequestException Thrown in case any of the roles is not defined.
     */
    private Map<String, RoleEntity> collectRoleEntities(List<String> roles) throws InvalidRequestException {
        Map<String, RoleEntity> roleEntities = new HashMap<>();
        for (String roleName : roles) {
            Optional<RoleEntity> roleOptional = roleRepository.findByName(roleName);
            if (!roleOptional.isPresent()) {
                throw new InvalidRequestException("User role not found: " + roleName);
            }
            roleEntities.put(roleName, roleOptional.get());
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
        List<UserRoleEntity> existingRoles = userRoleRepository.findAllByUser(user);
        Map<String, UserRoleEntity> existingRoleMap = new HashMap<>();
        existingRoles.forEach(userRole -> existingRoleMap.put(userRole.getRole().getName(), userRole));
        for (String roleToAdd : roles) {
            UserRoleEntity existingRole = existingRoleMap.get(roleToAdd);
            if (existingRole == null) {
                // Persist new role
                UserRoleEntity userRole = new UserRoleEntity();
                userRole.setUser(user);
                userRole.setRole(roleEntities.get(roleToAdd));
                userRole.setStatus(UserRoleStatus.ACTIVE);
                userRole.setTimestampCreated(new Date());
                userRoleRepository.save(userRole);
            } else if (existingRole.getStatus() == UserRoleStatus.REMOVED) {
                // Make removed role active
                existingRole.setStatus(UserRoleStatus.ACTIVE);
                existingRole.setTimestampLastUpdated(new Date());
                userRoleRepository.save(existingRole);
            }
        }
        existingRoleMap.keySet().forEach(roleName -> {
            if (!roles.contains(roleName)) {
                // Remove role if not already removed
                UserRoleEntity existingRole = existingRoleMap.get(roleName);
                if (existingRole.getStatus() != UserRoleStatus.REMOVED) {
                    existingRole.setStatus(UserRoleStatus.REMOVED);
                    userRoleRepository.save(existingRole);
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
        List<UserContactDetail> contactListResponse = new ArrayList<>();
        List<UserContactEntity> existingContacts = userContactRepository.findAllByUser(user);
        Map<String, UserContactEntity> existingContactMap = new HashMap<>();
        existingContacts.forEach(userContact -> existingContactMap.put(userContact.getName(), userContact));
        // Persist new or update existing contacts
        for (UserContactDetail contactToAdd : contacts) {
            UserContactEntity userContact = existingContactMap.get(contactToAdd.getContactName());
            if (userContact == null) {
                userContact = new UserContactEntity();
                userContact.setName(contactToAdd.getContactName());
                userContact.setUser(user);
                userContact.setTimestampCreated(new Date());
            } else {
                userContact.setTimestampLastUpdated(new Date());
            }
            userContact.setType(contactToAdd.getContactType());
            userContact.setValue(contactToAdd.getContactValue());
            userContact.setPrimary(contactToAdd.isPrimary());
            userContactRepository.save(userContact);
            UserContactDetail contactDetail = new UserContactDetail();
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
                userContactRepository.delete(contactEntity);
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
        List<CredentialEntity> existingCredentials = credentialRepository.findAllByUser(user);
        List<String> credentialsToKeep = activeCredentials
                .stream()
                .map(CredentialSecretDetail::getCredentialName)
                .collect(Collectors.toList());
        existingCredentials.forEach(credential -> {
            if (!credentialsToKeep.contains(credential.getCredentialDefinition().getName())
                    && credential.getStatus() != CredentialStatus.REMOVED) {
                credential.setStatus(CredentialStatus.REMOVED);
                credentialRepository.save(credential);
            }
        });
    }

    /**
     * Remove all credentials for user.
     * @param user User identity entity.
     */
    private void removeAllCredentials(UserIdentityEntity user) {
        List<CredentialEntity> existingCredentials = credentialRepository.findAllByUser(user);
        existingCredentials.forEach(credential -> {
            if (credential.getStatus() != CredentialStatus.REMOVED) {
                credential.setStatus(CredentialStatus.REMOVED);
                credentialRepository.save(credential);
            }
        });
    }

    /**
     * Remove all OTPs for user.
     * @param user User identity entity.
     */
    private void removeAllOtps(UserIdentityEntity user) {
        List<OtpEntity> otps = otpRepository.findAllByUserId(user.getUserId());
        otps.forEach(otp -> {
            if (otp.getStatus() != OtpStatus.REMOVED) {
                otp.setStatus(OtpStatus.REMOVED);
                otpRepository.save(otp);
            }
        });
    }

}