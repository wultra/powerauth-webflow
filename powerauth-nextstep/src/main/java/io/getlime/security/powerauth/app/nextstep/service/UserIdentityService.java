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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.getlime.security.powerauth.app.nextstep.repository.*;
import io.getlime.security.powerauth.app.nextstep.repository.model.entity.*;
import io.getlime.security.powerauth.lib.nextstep.model.entity.CredentialSecretDetail;
import io.getlime.security.powerauth.lib.nextstep.model.entity.UserContactDetail;
import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.CredentialStatus;
import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.UserIdentityStatus;
import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.UserRoleStatus;
import io.getlime.security.powerauth.lib.nextstep.model.exception.InvalidConfigurationException;
import io.getlime.security.powerauth.lib.nextstep.model.exception.InvalidRequestException;
import io.getlime.security.powerauth.lib.nextstep.model.exception.UserAlreadyExistsException;
import io.getlime.security.powerauth.lib.nextstep.model.request.CreateUserRequest;
import io.getlime.security.powerauth.lib.nextstep.model.request.UpdateUserRequest;
import io.getlime.security.powerauth.lib.nextstep.model.response.CreateUserResponse;
import io.getlime.security.powerauth.lib.nextstep.model.response.UpdateUserResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.*;

/**
 * This service handles persistence of user identities.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Service
public class UserIdentityService {

    private final UserIdentityRepository userIdentityRepository;
    private final UserContactRepository userContactRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final CredentialDefinitionRepository credentialDefinitionRepository;
    private final CredentialRepository credentialRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final Logger logger = LoggerFactory.getLogger(UserIdentityService.class);

    @Autowired
    public UserIdentityService(UserIdentityRepository userIdentityRepository, UserContactRepository userContactRepository, RoleRepository roleRepository, UserRoleRepository userRoleRepository, CredentialDefinitionRepository credentialDefinitionRepository, CredentialRepository credentialRepository) {
        this.userIdentityRepository = userIdentityRepository;
        this.userContactRepository = userContactRepository;
        this.roleRepository = roleRepository;
        this.userRoleRepository = userRoleRepository;
        this.credentialDefinitionRepository = credentialDefinitionRepository;
        this.credentialRepository = credentialRepository;
    }

    @Transactional
    public CreateUserResponse createUserIdentity(CreateUserRequest request) throws UserAlreadyExistsException, InvalidConfigurationException, InvalidRequestException {
        // TODO - finish support for more complex use cases (revive, generate credentials)
        Optional<UserIdentityEntity> userOptional = userIdentityRepository.findById(request.getUserId());
        UserIdentityEntity user;
        Map<String, RoleEntity> roles = new HashMap<>();
        if (request.getRoles() != null) {
            for (String roleName : request.getRoles()) {
                Optional<RoleEntity> roleOptional = roleRepository.findByName(roleName);
                if (!roleOptional.isPresent()) {
                    throw new InvalidConfigurationException("User role not found: " + roleName);
                }
                roles.put(roleName, roleOptional.get());
            }
        }
        Map<String, CredentialDefinitionEntity> credentialDefinitions = new HashMap<>();
        if (request.getCredentials() != null) {
            for (CreateUserRequest.NewCredential credential : request.getCredentials()) {
                Optional<CredentialDefinitionEntity> credentialDefinitionOptional = credentialDefinitionRepository.findByName(credential.getCredentialName());
                if (!credentialDefinitionOptional.isPresent()) {
                    throw new InvalidConfigurationException("Credential definition not found: " + credential.getCredentialName());
                }
                credentialDefinitions.put(credential.getCredentialName(), credentialDefinitionOptional.get());
            }
        }
        if (userOptional.isPresent()) {
            user = userOptional.get();
            if (user.getStatus() != UserIdentityStatus.REMOVED) {
                throw new UserAlreadyExistsException("User identity already exists: " + request.getUserId());
            }
            // Revive user identity
            user.setStatus(UserIdentityStatus.ACTIVE);
            // Remove existing user roles
            List<UserRoleEntity> existingRoles = userRoleRepository.findAllByUserId(user.getUserId());
            for (UserRoleEntity role : existingRoles) {
                userRoleRepository.delete(role);
            }
            // Remove existing user contacts
            Iterable<UserContactEntity> existingContacts = userContactRepository.findAllByUserId(user.getUserId());
            for (UserContactEntity contact : existingContacts) {
                userContactRepository.delete(contact);
            }
            // Credentials are updated later
        } else {
            user = new UserIdentityEntity();
        }
        // TODO - create converter
        if (request.getExtras() != null) {
            try {
                String extras = objectMapper.writeValueAsString(request.getExtras());
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
        // TODO - extract extras from user
        response.getExtras().putAll(request.getExtras());

        if (request.getRoles() != null) {
            for (String roleName : request.getRoles()) {
                UserRoleEntity userRole = new UserRoleEntity();
                userRole.setUserId(user);
                userRole.setRole(roles.get(roleName));
                userRole.setStatus(UserRoleStatus.ACTIVE);
                userRole.setTimestampCreated(new Date());
                userRoleRepository.save(userRole);
                response.getRoles().add(roleName);
            }
        }
        if (request.getContacts() != null) {
            for (CreateUserRequest.NewContact contact : request.getContacts()) {
                UserContactEntity userContact = new UserContactEntity();
                userContact.setUserId(user);
                userContact.setName(contact.getContactName());
                userContact.setType(contact.getContactType());
                userContact.setValue(contact.getContactValue());
                userContact.setPrimary(contact.isPrimary());
                userContact.setTimestampCreated(new Date());
                userContactRepository.save(userContact);
                UserContactDetail contactDetail = new UserContactDetail();
                contactDetail.setContactName(userContact.getName());
                contactDetail.setContactType(userContact.getType());
                contactDetail.setContactValue(userContact.getValue());
                contactDetail.setPrimary(userContact.isPrimary());
                contactDetail.setTimestampCreated(userContact.getTimestampCreated());
                response.getContacts().add(contactDetail);
            }
        }
        if (request.getCredentials() != null) {
            for (CreateUserRequest.NewCredential credential : request.getCredentials()) {
                // TODO - revive existing credentials and merge them with request
                CredentialEntity userCredential = new CredentialEntity();
                userCredential.setCredentialId(UUID.randomUUID().toString());
                userCredential.setCredentialDefinition(credentialDefinitions.get(credential.getCredentialName()));
                userCredential.setUserId(user);
                userCredential.setType(credential.getCredentialType());
                // TODO - generate username if username is null
                userCredential.setUsername(credential.getUsername());
                // TODO - generate credential value if credential value is null
                userCredential.setValue(credential.getCredentialValue());
                userCredential.setStatus(CredentialStatus.ACTIVE);
                userCredential.setAttemptCounter(0L);
                userCredential.setFailedAttemptCounterSoft(0L);
                userCredential.setFailedAttemptCounterHard(0L);
                userCredential.setTimestampCreated(new Date());
                credentialRepository.save(userCredential);
                CredentialSecretDetail credentialDetail = new CredentialSecretDetail();
                credentialDetail.setCredentialName(userCredential.getCredentialDefinition().getName());
                credentialDetail.setCredentialType(userCredential.getType());
                credentialDetail.setCredentialStatus(CredentialStatus.ACTIVE);
                credentialDetail.setUsername(userCredential.getUsername());
                credentialDetail.setCredentialValue(userCredential.getValue());
                credentialDetail.setTimestampCreated(userCredential.getTimestampCreated());
                response.getCredentials().add(credentialDetail);
            }
        }
        return response;
    }

    @Transactional
    public UpdateUserResponse updateUserIdentity(UpdateUserRequest request) {
        return new UpdateUserResponse();
    }

}