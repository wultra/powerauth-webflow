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

import io.getlime.security.powerauth.app.nextstep.converter.CredentialConverter;
import io.getlime.security.powerauth.app.nextstep.repository.CredentialRepository;
import io.getlime.security.powerauth.app.nextstep.repository.model.entity.CredentialDefinitionEntity;
import io.getlime.security.powerauth.app.nextstep.repository.model.entity.CredentialEntity;
import io.getlime.security.powerauth.app.nextstep.repository.model.entity.CredentialPolicyEntity;
import io.getlime.security.powerauth.app.nextstep.repository.model.entity.UserIdentityEntity;
import io.getlime.security.powerauth.lib.nextstep.model.entity.CredentialDetail;
import io.getlime.security.powerauth.lib.nextstep.model.entity.CredentialSecretDetail;
import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.CredentialStatus;
import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.CredentialType;
import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.CredentialValidationError;
import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.CredentialValidationResult;
import io.getlime.security.powerauth.lib.nextstep.model.exception.*;
import io.getlime.security.powerauth.lib.nextstep.model.request.*;
import io.getlime.security.powerauth.lib.nextstep.model.response.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.*;

/**
 * This service handles persistence of credentials.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Service
public class CredentialService {

    private final Logger logger = LoggerFactory.getLogger(CredentialService.class);

    private final UserIdentityLookupService userIdentityLookupService;
    private final CredentialDefinitionService credentialDefinitionService;
    private final CredentialRepository credentialRepository;

    private final CredentialConverter credentialConverter = new CredentialConverter();

    /**
     * Credential service constructor.
     * @param userIdentityLookupService User identity lookup service.
     * @param credentialDefinitionService Credential definition service.
     * @param credentialRepository Credential repository.
     */
    @Autowired
    public CredentialService(UserIdentityLookupService userIdentityLookupService, CredentialDefinitionService credentialDefinitionService, CredentialRepository credentialRepository) {
        this.userIdentityLookupService = userIdentityLookupService;
        this.credentialDefinitionService = credentialDefinitionService;
        this.credentialRepository = credentialRepository;
    }

    /**
     * Create a credential. Username and/or credential value is generated in case it is not specified.
     * @param request Create credential request.
     * @return Create credential response.
     * @throws UserNotFoundException Thrown when user identity is not found.
     * @throws CredentialDefinitionNotFoundException Thrown when credential definition is not found.
     * @throws UsernameAlreadyExistsException Thrown when username already exists.
     */
    @Transactional
    public CreateCredentialResponse createCredential(CreateCredentialRequest request) throws UserNotFoundException, CredentialDefinitionNotFoundException, UsernameAlreadyExistsException {
        UserIdentityEntity user = userIdentityLookupService.findUser(request.getUserId());
        CredentialDefinitionEntity credentialDefinition = credentialDefinitionService.findCredentialDefinition(request.getCredentialName());
        CredentialType credentialType = request.getCredentialType();
        String username = request.getUsername();
        String credentialValue = request.getCredentialValue();
        CredentialSecretDetail credentialDetail = createCredential(user, credentialDefinition, credentialType, username, credentialValue);
        CreateCredentialResponse response = new CreateCredentialResponse();
        response.setCredentialName(credentialDetail.getCredentialName());
        response.setCredentialType(credentialDetail.getCredentialType());
        response.setUserId(user.getUserId());
        response.setCredentialStatus(credentialDetail.getCredentialStatus());
        response.setUsername(credentialDetail.getUsername());
        if (request.getCredentialValue() == null) {
            // Return generated credential value
            response.setCredentialValue(credentialDetail.getCredentialValue());
        }
        return response;
    }

    /**
     * Update a credential.
     * @param request Update credential request.
     * @return Update credential response.
     * @throws UserNotFoundException Thrown when user is not found.
     * @throws CredentialDefinitionNotFoundException Thrown when credential definition is not found.
     * @throws CredentialNotFoundException Thrown when credential is not found.
     */
    @Transactional
    public UpdateCredentialResponse updateCredential(UpdateCredentialRequest request) throws UserNotFoundException, CredentialDefinitionNotFoundException, CredentialNotFoundException {
        UserIdentityEntity user = userIdentityLookupService.findUser(request.getUserId());
        CredentialDefinitionEntity credentialDefinition = credentialDefinitionService.findCredentialDefinition(request.getCredentialName());
        Optional<CredentialEntity> credentialOptional = credentialRepository.findByCredentialDefinitionAndUserId(credentialDefinition, user);
        if (!credentialOptional.isPresent()) {
            throw new CredentialNotFoundException("Credential not found: " + request.getCredentialName() + ", user ID: " + user.getUserId());
        }
        CredentialEntity credential = credentialOptional.get();
        if (credential.getStatus() == CredentialStatus.REMOVED && request.getCredentialStatus() == null) {
            throw new CredentialNotFoundException("Credential is REMOVED: " + request.getCredentialName() + ", user ID: " + user.getUserId());
        }
        if (credential.getStatus() == CredentialStatus.REMOVED && request.getCredentialStatus() == CredentialStatus.REMOVED) {
            throw new CredentialNotFoundException("Credential is already REMOVED: " + request.getCredentialName() + ", user ID: " + user.getUserId());
        }
        if (request.getCredentialType() != null) {
            credential.setType(request.getCredentialType());
        }
        // TODO - username and credentialValue validation
        // TODO - check that username is available
        if (request.getUsername() != null) {
            credential.setUsername(request.getUsername());
        }
        if (request.getCredentialValue() != null) {
            credential.setValue(request.getCredentialValue());
            credential.setTimestampLastCredentialChange(new Date());
        }
        if (request.getCredentialStatus() != null) {
            credential.setStatus(request.getCredentialStatus());
                if (credential.getStatus() == CredentialStatus.BLOCKED_TEMPORARY || credential.getStatus() == CredentialStatus.BLOCKED_PERMANENT){
                // For blocked credentials set timestamp when credential was blocked
                credential.setTimestampBlocked(new Date());
            } else if (credential.getStatus() == CredentialStatus.ACTIVE) {
                // Reset counters for active credentials
                credential.setFailedAttemptCounterSoft(0L);
                credential.setFailedAttemptCounterHard(0L);
            }
        }
        credential.setTimestampLastUpdated(new Date());
        credentialRepository.save(credential);
        UpdateCredentialResponse response = new UpdateCredentialResponse();
        response.setUserId(user.getUserId());
        response.setCredentialName(credential.getCredentialDefinition().getName());
        response.setCredentialType(credential.getType());
        response.setCredentialStatus(credential.getStatus());
        response.setUsername(credential.getUsername());
        return response;
    }

    /**
     * Get credential list for a user identity.
     * @param request Get credential list request.
     * @return Get credential list response.
     * @throws UserNotFoundException Thrown when user identity is not found.
     */
    @Transactional
    public GetUserCredentialListResponse getCredentialList(GetUserCredentialListRequest request) throws UserNotFoundException {
        UserIdentityEntity user = userIdentityLookupService.findUser(request.getUserId());
        GetUserCredentialListResponse response = new GetUserCredentialListResponse();
        response.setUserId(user.getUserId());
        List<CredentialEntity> credentials = credentialRepository.findAllByUserId(user);
        for (CredentialEntity credential: credentials) {
            if (credential.getStatus() == CredentialStatus.REMOVED && !request.isIncludeRemoved()) {
                continue;
            }
            CredentialDetail credentialDetail = credentialConverter.fromEntity(credential);
            response.getCredentials().add(credentialDetail);
        }
        return response;
    }

    /**
     * Validate a credential.
     * @param request Validate credential request.
     * @return Validate credential response.
     * @throws CredentialDefinitionNotFoundException Thrown when credential definition is not found.
     * @throws InvalidRequestException Thrown when request is invalid.
     */
    @Transactional
    public ValidateCredentialResponse validateCredential(ValidateCredentialRequest request) throws CredentialDefinitionNotFoundException, InvalidRequestException {
        CredentialDefinitionEntity credentialDefinition = credentialDefinitionService.findCredentialDefinition(request.getCredentialName());
        List<CredentialValidationError> validationErrors = new ArrayList<>();
        switch (request.getValidationMode()) {
            case VALIDATE_USERNAME:
                validationErrors.addAll(validateUsername(request.getUsername(), credentialDefinition));
                break;

            case VALIDATE_CREDENTIAL:
                validationErrors.addAll(validateCredentialValue(request.getCredentialValue(), credentialDefinition));
                break;

            case VALIDATE_USERNAME_AND_CREDENTIAL:
                validationErrors.addAll(validateUsername(request.getUsername(), credentialDefinition));
                validationErrors.addAll(validateCredentialValue(request.getCredentialValue(), credentialDefinition));
                break;

            default:
                throw new InvalidRequestException("Invalid validation mode: " + request.getValidationMode());

        }
        ValidateCredentialResponse response = new ValidateCredentialResponse();
        if (validationErrors.isEmpty()) {
            response.setValidationResult(CredentialValidationResult.SUCCEEDED);
        } else {
            response.setValidationResult(CredentialValidationResult.FAILED);
        }
        response.getValidationErrors().addAll(validationErrors);
        return response;
    }

    /**
     * Reset a credential. Generate a new credential value, set credential status to ACTIVE, and reset failed attempt counters.
     * @param request Reset credential request.
     * @return Reset credential response.
     * @throws UserNotFoundException Thrown when user identity is not found.
     * @throws CredentialDefinitionNotFoundException Thrown when credential definition is not found.
     * @throws CredentialNotFoundException Thrown when credential isnot found.
     */
    @Transactional
    public ResetCredentialResponse resetCredential(ResetCredentialRequest request) throws UserNotFoundException, CredentialDefinitionNotFoundException, CredentialNotFoundException {
        UserIdentityEntity user = userIdentityLookupService.findUser(request.getUserId());
        CredentialDefinitionEntity credentialDefinition = credentialDefinitionService.findCredentialDefinition(request.getCredentialName());
        Optional<CredentialEntity> credentialOptional = credentialRepository.findByCredentialDefinitionAndUserId(credentialDefinition, user);
        if (!credentialOptional.isPresent()) {
            throw new CredentialNotFoundException("Credential not found: " + request.getCredentialName() + ", user ID: " + user.getUserId());
        }
        CredentialEntity credential = credentialOptional.get();
        if (credential.getStatus() == CredentialStatus.REMOVED) {
            throw new CredentialNotFoundException("Credential is REMOVED: " + request.getCredentialName() + ", user ID: " + user.getUserId());
        }
        credential.setType(request.getCredentialType());
        // TODO - implement password generation algorithm
        credential.setValue("s3cret");
        credential.setTimestampLastUpdated(new Date());
        credential.setTimestampLastCredentialChange(new Date());
        credential.setFailedAttemptCounterSoft(0L);
        credential.setFailedAttemptCounterHard(0L);
        credential.setStatus(CredentialStatus.ACTIVE);
        credential.setTimestampBlocked(null);
        credentialRepository.save(credential);
        ResetCredentialResponse response = new ResetCredentialResponse();
        response.setUserId(user.getUserId());
        response.setCredentialName(credential.getCredentialDefinition().getName());
        response.setUsername(credential.getUsername());
        response.setCredentialValue(credential.getValue());
        response.setCredentialStatus(credential.getStatus());
        return response;
    }

    /**
     * Delete a credential (status = REMOVED).
     * @param request Delete credential request.
     * @return Delete credential response.
     * @throws UserNotFoundException Thrown when user identity is not found.
     * @throws CredentialDefinitionNotFoundException Thrown when credential definition is not found.
     * @throws CredentialNotFoundException Thrown when credential is not found.
     * @throws CredentialNotFoundException Thrown when credential is not found.
     */
    @Transactional
    public DeleteCredentialResponse deleteCredential(DeleteCredentialRequest request) throws UserNotFoundException, CredentialDefinitionNotFoundException, CredentialNotFoundException {
        UserIdentityEntity user = userIdentityLookupService.findUser(request.getUserId());
        CredentialDefinitionEntity credentialDefinition = credentialDefinitionService.findCredentialDefinition(request.getCredentialName());
        Optional<CredentialEntity> credentialOptional = credentialRepository.findByCredentialDefinitionAndUserId(credentialDefinition, user);
        if (!credentialOptional.isPresent()) {
            throw new CredentialNotFoundException("Credential not found: " + request.getCredentialName() + ", user ID: " + user.getUserId());
        }
        CredentialEntity credential = credentialOptional.get();
        if (credential.getStatus() == CredentialStatus.REMOVED) {
            throw new CredentialNotFoundException("Credential is already REMOVED: " + request.getCredentialName() + ", user ID: " + user.getUserId());
        }
        credential.setStatus(CredentialStatus.REMOVED);
        credentialRepository.save(credential);
        DeleteCredentialResponse response = new DeleteCredentialResponse();
        response.setUserId(user.getUserId());
        response.setCredentialName(credential.getCredentialDefinition().getName());
        response.setCredentialStatus(credential.getStatus());
        return response;
    }

    /**
     * Block a credential (status = BLOCKED_PERMANENT).
     * @param request Block credential request.
     * @return Block credential response.
     * @throws UserNotFoundException Thrown when user identity is not found.
     * @throws CredentialDefinitionNotFoundException Thrown when credential definition is not found.
     * @throws CredentialNotFoundException Thrown when credential is not found.
     * @throws CredentialNotActiveException Thrown when credential is not active.
     */
    @Transactional
    public BlockCredentialResponse blockCredential(BlockCredentialRequest request) throws UserNotFoundException, CredentialDefinitionNotFoundException, CredentialNotFoundException, CredentialNotActiveException {
        UserIdentityEntity user = userIdentityLookupService.findUser(request.getUserId());
        CredentialDefinitionEntity credentialDefinition = credentialDefinitionService.findCredentialDefinition(request.getCredentialName());
        Optional<CredentialEntity> credentialOptional = credentialRepository.findByCredentialDefinitionAndUserId(credentialDefinition, user);
        if (!credentialOptional.isPresent()) {
            throw new CredentialNotFoundException("Credential not found: " + request.getCredentialName() + ", user ID: " + user.getUserId());
        }
        CredentialEntity credential = credentialOptional.get();
        if (credential.getStatus() != CredentialStatus.ACTIVE) {
            throw new CredentialNotActiveException("Credential is not ACTIVE: " + request.getCredentialName() + ", user ID: " + user.getUserId());
        }
        credential.setStatus(CredentialStatus.BLOCKED_PERMANENT);
        credential.setTimestampBlocked(new Date());
        credentialRepository.save(credential);
        BlockCredentialResponse response = new BlockCredentialResponse();
        response.setUserId(user.getUserId());
        response.setCredentialName(credential.getCredentialDefinition().getName());
        response.setCredentialStatus(credential.getStatus());
        return response;
    }

    /**
     * Unblock a credential (status = ACTIVE).
     * @param request Unblock credential request.
     * @return Unblock credential response.
     * @throws UserNotFoundException Thrown in case user is not found.
     * @throws CredentialDefinitionNotFoundException Thrown in case credential definition is not found.
     * @throws CredentialNotFoundException Thrown in case credential is not found.
     * @throws CredentialNotBlockedException Thrown in case credential is not blocked.
     */
    @Transactional
    public UnblockCredentialResponse unblockCredential(UnblockCredentialRequest request) throws UserNotFoundException, CredentialDefinitionNotFoundException, CredentialNotFoundException, CredentialNotBlockedException {
        UserIdentityEntity user = userIdentityLookupService.findUser(request.getUserId());
        CredentialDefinitionEntity credentialDefinition = credentialDefinitionService.findCredentialDefinition(request.getCredentialName());
        Optional<CredentialEntity> credentialOptional = credentialRepository.findByCredentialDefinitionAndUserId(credentialDefinition, user);
        if (!credentialOptional.isPresent()) {
            throw new CredentialNotFoundException("Credential not found: " + request.getCredentialName() + ", user ID: " + user.getUserId());
        }
        CredentialEntity credential = credentialOptional.get();
        if (credential.getStatus() == CredentialStatus.REMOVED) {
            throw new CredentialNotFoundException("Credential is REMOVED: " + request.getCredentialName() + ", user ID: " + user.getUserId());
        }
        if (credential.getStatus() != CredentialStatus.BLOCKED_PERMANENT && credential.getStatus() != CredentialStatus.BLOCKED_TEMPORARY) {
            throw new CredentialNotBlockedException("Credential is not BLOCKED_PERMANENT or BLOCKED_TEMPORARY: " + request.getCredentialName() + ", user ID: " + user.getUserId());
        }
        credential.setFailedAttemptCounterSoft(0L);
        credential.setFailedAttemptCounterHard(0L);
        credential.setStatus(CredentialStatus.ACTIVE);
        credential.setTimestampBlocked(null);
        credentialRepository.save(credential);
        UnblockCredentialResponse response = new UnblockCredentialResponse();
        response.setUserId(user.getUserId());
        response.setCredentialName(credential.getCredentialDefinition().getName());
        response.setCredentialStatus(credential.getStatus());
        return response;
    }

    /**
     * Find a credential. This method is not transactional.
     * @param credentialDefinition Credential definition.
     * @param user User identity entity.
     * @return Credential.
     * @throws CredentialNotFoundException Thrown when credential is not found.
     * @throws CredentialNotActiveException Thrown when credential is not active.
     */
    public CredentialEntity findCredential(CredentialDefinitionEntity credentialDefinition, UserIdentityEntity user) throws CredentialNotFoundException, CredentialNotActiveException {
        Optional<CredentialEntity> credentialOptional = credentialRepository.findByCredentialDefinitionAndUserId(credentialDefinition, user);
        if (!credentialOptional.isPresent()) {
            throw new CredentialNotFoundException("Credential not found: " + credentialDefinition.getName() + ", user ID: " + user.getUserId());
        }
        CredentialEntity credential = credentialOptional.get();
        if (credential.getStatus() == CredentialStatus.REMOVED) {
            throw new CredentialNotFoundException("Credential is REMOVED: " + credentialDefinition.getName() + ", user ID: " + user.getUserId());
        }
        if (credential.getStatus() != CredentialStatus.ACTIVE) {
            throw new CredentialNotActiveException("Credential is not ACTIVE: " + credentialDefinition.getName() + ", user ID: " + user.getUserId());
        }
        return credential;
    }


    /**
     * Validate a username.
     * @param username Username.
     * @param credentialDefinition Credential definition.
     * @return List of validation errors.
     */
    private List<CredentialValidationError> validateUsername(String username, CredentialDefinitionEntity credentialDefinition) {
        List<CredentialValidationError> validationErrors = new ArrayList<>();
        if (username == null || username.isEmpty()) {
            validationErrors.add(CredentialValidationError.USERNAME_EMPTY);
            return validationErrors;
        }
        CredentialPolicyEntity credentialPolicy = credentialDefinition.getCredentialPolicy();
        Integer minLength = credentialPolicy.getUsernameLengthMin();
        Integer maxLength = credentialPolicy.getUsernameLengthMax();
        String allowedChars = credentialPolicy.getUsernameAllowedChars();
        if (minLength != null && username.length() < minLength) {
            validationErrors.add(CredentialValidationError.USERNAME_TOO_SHORT);
        }
        if (minLength != null && username.length() > maxLength) {
            validationErrors.add(CredentialValidationError.USERNAME_TOO_LONG);
        }
        if (allowedChars != null && !username.matches(allowedChars)) {
            validationErrors.add(CredentialValidationError.USERNAME_CONTAINS_INVALID_CHARACTERS);
        }
        Optional<CredentialEntity> credentialOptional = credentialRepository.findByCredentialDefinitionAndUsername(credentialDefinition, username);
        if (credentialOptional.isPresent()) {
            validationErrors.add(CredentialValidationError.USERNAME_ALREADY_EXISTS);
        }
        return validationErrors;
    }

    /**
     * Validate a credential value.
     * @param credentialValue Credential value.
     * @param credentialDefinition Credential definition.
     * @return List of validation errors.
     */
    private List<CredentialValidationError> validateCredentialValue(String credentialValue, CredentialDefinitionEntity credentialDefinition) {
        List<CredentialValidationError> validationErrors = new ArrayList<>();
        if (credentialValue == null || credentialValue.isEmpty()) {
            validationErrors.add(CredentialValidationError.CREDENTIAL_EMPTY);
            return validationErrors;
        }
        CredentialPolicyEntity credentialPolicy = credentialDefinition.getCredentialPolicy();
        Integer minLength = credentialPolicy.getCredentialLengthMin();
        Integer maxLength = credentialPolicy.getCredentialLengthMax();
        String allowedChars = credentialPolicy.getCredentialAllowedChars();
        if (minLength != null && credentialValue.length() < minLength) {
            validationErrors.add(CredentialValidationError.CREDENTIAL_TOO_SHORT);
        }
        if (minLength != null && credentialValue.length() > maxLength) {
            validationErrors.add(CredentialValidationError.CREDENTIAL_TOO_LONG);
        }
        if (allowedChars != null && !credentialValue.matches(allowedChars)) {
            validationErrors.add(CredentialValidationError.CREDENTIAL_CONTAINS_INVALID_CHARACTERS);
        }
        // TODO - credential history check
        return validationErrors;
    }

    /**
     * Create a credential. In case the credential is already defined in the database, reuse the existing record.
     * Method is not transactional.
     *
     * @param user User identity entity.
     * @param credentialDefinition Credential definition entity.
     * @param credentialType Credential type.
     * @param username Username, use null for generated username.
     * @param credentialValue Credential value, use null for generated credential value.
     * @throws UsernameAlreadyExistsException Thrown when username already exists.
     */
    public CredentialSecretDetail createCredential(UserIdentityEntity user, CredentialDefinitionEntity credentialDefinition,
                                                   CredentialType credentialType, String username, String credentialValue) throws UsernameAlreadyExistsException {
        // Lookup credential in case it already exists
        CredentialEntity credential = null;
        Optional<CredentialEntity> credentialOptional = credentialRepository.findByCredentialDefinitionAndUserId(credentialDefinition, user);
        if (credentialOptional.isPresent()) {
            // TODO - auditing
            credential = credentialOptional.get();
            credential.setTimestampLastUpdated(new Date());
            credential.setTimestampLastCredentialChange(new Date());
        }
        if (credential == null) {
            credential = new CredentialEntity();
            credential.setCredentialId(UUID.randomUUID().toString());
            credential.setCredentialDefinition(credentialDefinition);
            credential.setUserId(user);
            credential.setTimestampCreated(new Date());
        }
        if (username != null) {
            Optional<CredentialEntity> existingCredentialOptional = credentialRepository.findByCredentialDefinitionAndUsername(credentialDefinition, username);
            if (existingCredentialOptional.isPresent()) {
                CredentialEntity existingCredential = existingCredentialOptional.get();
                if (!existingCredential.getUserId().equals(user)) {
                    throw new UsernameAlreadyExistsException("Username already exists: " + username + ", credential name: " + credentialDefinition.getName());
                }
            }
        }
        credential.setType(credentialType);
        // TODO - username and credentialValue validation
        // TODO - check that username is available
        // TODO - generate username if username is null
        credential.setUsername(username);
        // TODO - generate credential value if credential value is null
        credential.setValue(credentialValue);
        credential.setStatus(CredentialStatus.ACTIVE);
        credential.setTimestampBlocked(null);
        // Counters are reset even in case of an existing credential
        credential.setAttemptCounter(0L);
        credential.setFailedAttemptCounterSoft(0L);
        credential.setFailedAttemptCounterHard(0L);
        credentialRepository.save(credential);
        CredentialSecretDetail credentialDetail = new CredentialSecretDetail();
        credentialDetail.setCredentialName(credential.getCredentialDefinition().getName());
        credentialDetail.setCredentialType(credential.getType());
        credentialDetail.setCredentialStatus(CredentialStatus.ACTIVE);
        credentialDetail.setUsername(credential.getUsername());
        if (credentialValue == null) {
            // TODO - generated credential will be set here
            credentialDetail.setCredentialValue(credential.getValue());
        }
        credentialDetail.setTimestampCreated(credential.getTimestampCreated());
        credentialDetail.setTimestampLastUpdated(credential.getTimestampLastUpdated());
        credentialDetail.setTimestampExpired(credential.getTimestampExpires());
        credentialDetail.setTimestampBlocked(credential.getTimestampBlocked());
        credentialDetail.setTimestampLastCredentialChange(credential.getTimestampLastCredentialChange());
        return credentialDetail;
    }

}