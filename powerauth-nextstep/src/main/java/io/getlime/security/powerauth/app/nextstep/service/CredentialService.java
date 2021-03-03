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

import io.getlime.security.powerauth.app.nextstep.converter.CredentialConverter;
import io.getlime.security.powerauth.app.nextstep.converter.ParameterConverter;
import io.getlime.security.powerauth.app.nextstep.repository.CredentialRepository;
import io.getlime.security.powerauth.app.nextstep.repository.model.entity.CredentialDefinitionEntity;
import io.getlime.security.powerauth.app.nextstep.repository.model.entity.CredentialEntity;
import io.getlime.security.powerauth.app.nextstep.repository.model.entity.CredentialPolicyEntity;
import io.getlime.security.powerauth.app.nextstep.repository.model.entity.UserIdentityEntity;
import io.getlime.security.powerauth.lib.nextstep.model.entity.CredentialDetail;
import io.getlime.security.powerauth.lib.nextstep.model.entity.CredentialSecretDetail;
import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.*;
import io.getlime.security.powerauth.lib.nextstep.model.entity.error.CredentialValidationError;
import io.getlime.security.powerauth.lib.nextstep.model.exception.*;
import io.getlime.security.powerauth.lib.nextstep.model.request.*;
import io.getlime.security.powerauth.lib.nextstep.model.response.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.*;

/**
 * This service handles persistence of credentials.
 *
 * TODO:
 * - hashing of credentials using Argon2i
 * - encryption of credentials stored in database
 * - end-to-end encryption for credentials
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Service
public class CredentialService {

    private final Logger logger = LoggerFactory.getLogger(CredentialService.class);

    private static final int GENERATE_USERNAME_MAX_ATTEMPTS = 100;

    private final UserIdentityLookupService userIdentityLookupService;
    private final CredentialDefinitionService credentialDefinitionService;
    private final CredentialRepository credentialRepository;
    private final CredentialHistoryService credentialHistoryService;
    private final IdGeneratorService idGeneratorService;

    private final CredentialConverter credentialConverter = new CredentialConverter();
    private final ParameterConverter parameterConverter = new ParameterConverter();

    /**
     * Credential service constructor.
     * @param userIdentityLookupService User identity lookup service.
     * @param credentialDefinitionService Credential definition service.
     * @param credentialRepository Credential repository.
     * @param credentialHistoryService Credential history service.
     * @param idGeneratorService ID generator service.
     */
    @Autowired
    public CredentialService(UserIdentityLookupService userIdentityLookupService, CredentialDefinitionService credentialDefinitionService, CredentialRepository credentialRepository, CredentialHistoryService credentialHistoryService, IdGeneratorService idGeneratorService) {
        this.userIdentityLookupService = userIdentityLookupService;
        this.credentialDefinitionService = credentialDefinitionService;
        this.credentialRepository = credentialRepository;
        this.credentialHistoryService = credentialHistoryService;
        this.idGeneratorService = idGeneratorService;
    }

    /**
     * Create a credential. Username and/or credential value is generated in case it is not specified.
     * @param request Create credential request.
     * @return Create credential response.
     * @throws UserNotFoundException Thrown when user identity is not found.
     * @throws CredentialDefinitionNotFoundException Thrown when credential definition is not found.
     * @throws InvalidConfigurationException Thrown when Next Step configuration is invalid.
     * @throws InvalidRequestException Thrown when request is invalid.
     * @throws CredentialValidationFailedException Thrown when credential validation fails.
     */
    @Transactional
    public CreateCredentialResponse createCredential(CreateCredentialRequest request) throws UserNotFoundException, CredentialDefinitionNotFoundException, InvalidConfigurationException, InvalidRequestException, CredentialValidationFailedException {
        UserIdentityEntity user = userIdentityLookupService.findUser(request.getUserId());
        CredentialDefinitionEntity credentialDefinition = credentialDefinitionService.findActiveCredentialDefinition(request.getCredentialName());
        CredentialType credentialType = request.getCredentialType();
        String username = request.getUsername();
        String credentialValue = request.getCredentialValue();
        CredentialValidationMode validationMode = request.getValidationMode();
        List<CreateCredentialRequest.CredentialHistory> credentialHistory = request.getCredentialHistory();
        if (validationMode == null) {
            validationMode = CredentialValidationMode.VALIDATE_USERNAME_AND_CREDENTIAL;
        }
        CredentialSecretDetail credentialDetail = createCredential(user, credentialDefinition, credentialType, username, credentialValue, validationMode);
        if (credentialHistory != null && !credentialHistory.isEmpty()) {
            for (CreateCredentialRequest.CredentialHistory h : credentialHistory) {
                importCredentialHistory(user, credentialDefinition, h.getUsername(), h.getCredentialValue());
            }
        }
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
        response.setCredentialChangeRequired(credentialDetail.isCredentialChangeRequired());
        return response;
    }

    /**
     * Update a credential.
     * @param request Update credential request.
     * @return Update credential response.
     * @throws UserNotFoundException Thrown when user is not found.
     * @throws CredentialDefinitionNotFoundException Thrown when credential definition is not found.
     * @throws CredentialNotFoundException Thrown when credential is not found.
     * @throws CredentialValidationFailedException Thrown when credential validation fails.
     * @throws InvalidRequestException Thrown when request is invalid.
     */
    @Transactional
    public UpdateCredentialResponse updateCredential(UpdateCredentialRequest request) throws UserNotFoundException, CredentialDefinitionNotFoundException, CredentialNotFoundException, CredentialValidationFailedException, InvalidRequestException {
        UserIdentityEntity user = userIdentityLookupService.findUser(request.getUserId());
        CredentialDefinitionEntity credentialDefinition = credentialDefinitionService.findActiveCredentialDefinition(request.getCredentialName());
        Optional<CredentialEntity> credentialOptional = credentialRepository.findByCredentialDefinitionAndUser(credentialDefinition, user);
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
        String username;
        CredentialValidationMode validationMode;
        if (request.getUsername() != null) {
            username = request.getUsername();
            credential.setUsername(username);
            validationMode = CredentialValidationMode.VALIDATE_USERNAME_AND_CREDENTIAL;
        } else {
            username = credential.getUsername();
            validationMode = CredentialValidationMode.VALIDATE_CREDENTIAL;
        }
        if (request.getCredentialValue() != null) {
            List<CredentialValidationFailure> validationErrors = validateCredential(user, credentialDefinition, username, request.getCredentialValue(), validationMode);
            if (!validationErrors.isEmpty()) {
                CredentialValidationError error = new CredentialValidationError(CredentialValidationFailedException.CODE, "Validation failed", validationErrors);
                throw new CredentialValidationFailedException("Validation failed for user ID: " + user.getUserId(), error);
            }
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
                credential.setFailedAttemptCounterSoft(0);
                credential.setFailedAttemptCounterHard(0);
            }
        }
        credential.setTimestampLastUpdated(new Date());
        credentialRepository.save(credential);
        // Save credential into credential history
        credentialHistoryService.createCredentialHistory(credential);
        UpdateCredentialResponse response = new UpdateCredentialResponse();
        response.setUserId(user.getUserId());
        response.setCredentialName(credential.getCredentialDefinition().getName());
        response.setCredentialType(credential.getType());
        response.setCredentialStatus(credential.getStatus());
        response.setUsername(credential.getUsername());
        boolean credentialChangeRequired = isCredentialChangeRequired(credential);
        response.setCredentialChangeRequired(credentialChangeRequired);
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
        List<CredentialEntity> credentials = credentialRepository.findAllByUser(user);
        for (CredentialEntity credential: credentials) {
            if (credential.getStatus() == CredentialStatus.REMOVED && !request.isIncludeRemoved()) {
                continue;
            }
            CredentialDetail credentialDetail = credentialConverter.fromEntity(credential);
            credentialDetail.setCredentialChangeRequired(isCredentialChangeRequired(credential));
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
     * @throws UserNotFoundException Thrown when user is not found.
     */
    @Transactional
    public ValidateCredentialResponse validateCredential(ValidateCredentialRequest request) throws CredentialDefinitionNotFoundException, InvalidRequestException, UserNotFoundException {
        // TODO - move to a separate service
        UserIdentityEntity user = userIdentityLookupService.findUser(request.getUserId());
        CredentialDefinitionEntity credentialDefinition = credentialDefinitionService.findActiveCredentialDefinition(request.getCredentialName());
        List<CredentialValidationFailure> validationErrors = validateCredential(user, credentialDefinition, request.getUsername(), request.getCredentialValue(), request.getValidationMode());
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
     * Validate credential.
     * @param user User identity entity.
     * @param credentialDefinition Credential definition entity.
     * @param username Username.
     * @param credentialValue Credential value.
     * @param validationMode Validation mode.
     * @return List of validation errors.
     * @throws InvalidRequestException Thrown in case request is invalid.
     */
    private List<CredentialValidationFailure> validateCredential(UserIdentityEntity user, CredentialDefinitionEntity credentialDefinition,
                                                                String username, String credentialValue,
                                                                CredentialValidationMode validationMode) throws InvalidRequestException {
        // TODO - move to a separate service
        List<CredentialValidationFailure> validationErrors = new ArrayList<>();
        switch (validationMode) {
            case NO_VALIDATION:
                break;

            case VALIDATE_USERNAME:
                validationErrors.addAll(validateUsername(user, username, credentialDefinition));
                break;

            case VALIDATE_CREDENTIAL:
                validationErrors.addAll(validateCredentialValue(user, credentialValue, credentialDefinition, true));
                break;

            case VALIDATE_USERNAME_AND_CREDENTIAL:
                validationErrors.addAll(validateUsername(user, username, credentialDefinition));
                validationErrors.addAll(validateCredentialValue(user, credentialValue, credentialDefinition, true));
                break;

            default:
                throw new InvalidRequestException("Invalid validation mode: " + validationMode);

        }
        return validationErrors;
    }

    /**
     * Check whether credential change is required.
     * @param credential Credential entity.
     * @return Whether credential change is required.
     */
    public boolean isCredentialChangeRequired(CredentialEntity credential) {
        CredentialPolicyEntity credentialPolicy = credential.getCredentialDefinition().getCredentialPolicy();
        if (credentialPolicy.isRotationEnabled()) {
            Date lastChange = credential.getTimestampLastCredentialChange();
            if (lastChange == null) {
                // Only happens when data in database is manipulated
                return true;
            }
            Calendar c = GregorianCalendar.getInstance();
            c.add(Calendar.DAY_OF_YEAR, -credentialPolicy.getRotationDays());
            if (lastChange.before(c.getTime())) {
                // Last credential change occurred before time calculated by password rotation days
                return true;
            }
        }
        List<CredentialValidationFailure> validationFailures = validateCredentialValue(credential.getUser(), credential.getValue(), credential.getCredentialDefinition(), false);
        return !validationFailures.isEmpty();
    }

    /**
     * Reset a credential. Generate a new credential value, set credential status to ACTIVE, and reset failed attempt counters.
     * @param request Reset credential request.
     * @return Reset credential response.
     * @throws UserNotFoundException Thrown when user identity is not found.
     * @throws CredentialDefinitionNotFoundException Thrown when credential definition is not found.
     * @throws CredentialNotFoundException Thrown when credential is not found.
     * @throws InvalidConfigurationException Thrown in case Next Step configuration is invalid.
     */
    @Transactional
    public ResetCredentialResponse resetCredential(ResetCredentialRequest request) throws UserNotFoundException, CredentialDefinitionNotFoundException, CredentialNotFoundException, InvalidConfigurationException {
        UserIdentityEntity user = userIdentityLookupService.findUser(request.getUserId());
        CredentialDefinitionEntity credentialDefinition = credentialDefinitionService.findActiveCredentialDefinition(request.getCredentialName());
        Optional<CredentialEntity> credentialOptional = credentialRepository.findByCredentialDefinitionAndUser(credentialDefinition, user);
        if (!credentialOptional.isPresent()) {
            throw new CredentialNotFoundException("Credential not found: " + request.getCredentialName() + ", user ID: " + user.getUserId());
        }
        CredentialEntity credential = credentialOptional.get();
        if (credential.getStatus() == CredentialStatus.REMOVED) {
            throw new CredentialNotFoundException("Credential is REMOVED: " + request.getCredentialName() + ", user ID: " + user.getUserId());
        }
        // Save original credential into credential history
        credentialHistoryService.createCredentialHistory(credential);
        if (request.getCredentialType() != null) {
            credential.setType(request.getCredentialType());
        }
        credential.setValue(generateCredentialValue(credentialDefinition));
        credential.setTimestampLastUpdated(new Date());
        credential.setTimestampLastCredentialChange(new Date());
        credential.setFailedAttemptCounterSoft(0);
        credential.setFailedAttemptCounterHard(0);
        credential.setStatus(CredentialStatus.ACTIVE);
        credential.setType(request.getCredentialType());
        credential.setTimestampBlocked(null);
        credentialRepository.save(credential);
        // Save credential into credential history
        credentialHistoryService.createCredentialHistory(credential);
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
        CredentialDefinitionEntity credentialDefinition = credentialDefinitionService.findActiveCredentialDefinition(request.getCredentialName());
        Optional<CredentialEntity> credentialOptional = credentialRepository.findByCredentialDefinitionAndUser(credentialDefinition, user);
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
        CredentialDefinitionEntity credentialDefinition = credentialDefinitionService.findActiveCredentialDefinition(request.getCredentialName());
        Optional<CredentialEntity> credentialOptional = credentialRepository.findByCredentialDefinitionAndUser(credentialDefinition, user);
        if (!credentialOptional.isPresent()) {
            throw new CredentialNotFoundException("Credential not found: " + request.getCredentialName() + ", user ID: " + user.getUserId());
        }
        CredentialEntity credential = credentialOptional.get();
        if (credential.getStatus() != CredentialStatus.ACTIVE && credential.getStatus() != CredentialStatus.BLOCKED_TEMPORARY) {
            throw new CredentialNotActiveException("Credential is not ACTIVE or BLOCKED_TEMPORARY: " + request.getCredentialName() + ", user ID: " + user.getUserId());
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
        CredentialDefinitionEntity credentialDefinition = credentialDefinitionService.findActiveCredentialDefinition(request.getCredentialName());
        Optional<CredentialEntity> credentialOptional = credentialRepository.findByCredentialDefinitionAndUser(credentialDefinition, user);
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
        credential.setFailedAttemptCounterSoft(0);
        credential.setFailedAttemptCounterHard(0);
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
    public CredentialEntity findActiveCredential(CredentialDefinitionEntity credentialDefinition, UserIdentityEntity user) throws CredentialNotFoundException, CredentialNotActiveException {
        Optional<CredentialEntity> credentialOptional = credentialRepository.findByCredentialDefinitionAndUser(credentialDefinition, user);
        if (!credentialOptional.isPresent()) {
            throw new CredentialNotFoundException("Credential not found: " + credentialDefinition.getName());
        }
        CredentialEntity credential = credentialOptional.get();
        if (credential.getStatus() == CredentialStatus.REMOVED) {
            throw new CredentialNotFoundException("Credential is REMOVED: " + credentialDefinition.getName());
        }
        if (credential.getStatus() != CredentialStatus.ACTIVE) {
            throw new CredentialNotActiveException("Credential is not ACTIVE: " + credentialDefinition.getName());
        }
        return credential;
    }

    /**
     * Find a credential without status check. This method is not transactional.
     * @param credentialDefinition Credential definition.
     * @param user User identity entity.
     * @return Credential.
     * @throws CredentialNotFoundException Thrown when credential is not found.
     */
    public CredentialEntity findCredential(CredentialDefinitionEntity credentialDefinition, UserIdentityEntity user) throws CredentialNotFoundException {
        Optional<CredentialEntity> credentialOptional = credentialRepository.findByCredentialDefinitionAndUser(credentialDefinition, user);
        if (!credentialOptional.isPresent()) {
            throw new CredentialNotFoundException("Credential not found: " + credentialDefinition.getName());
        }
        return credentialOptional.get();
    }

    /**
     * Validate a username.
     * @param username Username.
     * @param credentialDefinition Credential definition.
     * @return List of validation errors.
     */
    private List<CredentialValidationFailure> validateUsername(UserIdentityEntity user, String username, CredentialDefinitionEntity credentialDefinition) {
        // TODO - move to a separate service
        List<CredentialValidationFailure> validationFailures = new ArrayList<>();
        if (username == null || username.isEmpty()) {
            validationFailures.add(CredentialValidationFailure.USERNAME_EMPTY);
            return validationFailures;
        }
        CredentialPolicyEntity credentialPolicy = credentialDefinition.getCredentialPolicy();
        Integer minLength = credentialPolicy.getUsernameLengthMin();
        Integer maxLength = credentialPolicy.getUsernameLengthMax();
        String allowedChars = credentialPolicy.getUsernameAllowedChars();
        if (minLength != null && username.length() < minLength) {
            validationFailures.add(CredentialValidationFailure.USERNAME_TOO_SHORT);
        }
        if (maxLength != null && username.length() > maxLength) {
            validationFailures.add(CredentialValidationFailure.USERNAME_TOO_LONG);
        }
        if (allowedChars != null && !username.matches(allowedChars)) {
            validationFailures.add(CredentialValidationFailure.USERNAME_PATTERN_MATCH_FAILED);
        }
        Optional<CredentialEntity> credentialOptional = credentialRepository.findByCredentialDefinitionAndUsername(credentialDefinition, username);
        if (credentialOptional.isPresent()) {
            CredentialEntity credential = credentialOptional.get();
            if (!credential.getUser().equals(user)) {
                validationFailures.add(CredentialValidationFailure.USERNAME_ALREADY_EXISTS);
            }
        }
        return validationFailures;
    }

    /**
     * Validate a credential value.
     * @param credentialValue Credential value.
     * @param credentialDefinition Credential definition.
     * @return List of validation errors.
     */
    private List<CredentialValidationFailure> validateCredentialValue(UserIdentityEntity user, String credentialValue, CredentialDefinitionEntity credentialDefinition, boolean checkHistory) {
        // TODO - move to a separate service
        // TODO - switch credential validation to Passay library with more advanced rules
        List<CredentialValidationFailure> validationErrors = new ArrayList<>();
        if (credentialValue == null || credentialValue.isEmpty()) {
            validationErrors.add(CredentialValidationFailure.CREDENTIAL_EMPTY);
            return validationErrors;
        }
        CredentialPolicyEntity credentialPolicy = credentialDefinition.getCredentialPolicy();
        Integer minLength = credentialPolicy.getCredentialLengthMin();
        Integer maxLength = credentialPolicy.getCredentialLengthMax();
        String allowedChars = credentialPolicy.getCredentialAllowedChars();
        if (minLength != null && credentialValue.length() < minLength) {
            validationErrors.add(CredentialValidationFailure.CREDENTIAL_TOO_SHORT);
        }
        if (maxLength != null && credentialValue.length() > maxLength) {
            validationErrors.add(CredentialValidationFailure.CREDENTIAL_TOO_LONG);
        }
        if (allowedChars != null && !credentialValue.matches(allowedChars)) {
            validationErrors.add(CredentialValidationFailure.CREDENTIAL_PATTERN_MATCH_FAILED);
        }
        if (checkHistory && !credentialHistoryService.checkCredentialHistory(user, credentialValue, credentialDefinition)) {
            validationErrors.add(CredentialValidationFailure.CREDENTIAL_HISTORY_CHECK_FAILED);
        }
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
     * @param validationMode Credential validation mode.
     * @throws InvalidConfigurationException Thrown when Next Step configuration is invalid.
     * @throws CredentialValidationFailedException Thrown when credential validation fails.
     * @throws InvalidRequestException Thrown when request is invalid.
     */
    public CredentialSecretDetail createCredential(UserIdentityEntity user, CredentialDefinitionEntity credentialDefinition,
                                                   CredentialType credentialType, String username, String credentialValue,
                                                   CredentialValidationMode validationMode) throws InvalidConfigurationException, CredentialValidationFailedException, InvalidRequestException {
        // Lookup credential in case it already exists
        CredentialEntity credential = null;
        Optional<CredentialEntity> credentialOptional = credentialRepository.findByCredentialDefinitionAndUser(credentialDefinition, user);
        if (credentialOptional.isPresent()) {
            // TODO - auditing
            credential = credentialOptional.get();
            credential.setTimestampLastUpdated(new Date());
            credential.setTimestampLastCredentialChange(new Date());
        }
        if (credential == null) {
            credential = new CredentialEntity();
            credential.setCredentialId(idGeneratorService.generateCredentialId());
            credential.setCredentialDefinition(credentialDefinition);
            credential.setUser(user);
            credential.setTimestampCreated(new Date());
        }
        if (username != null) {
            // Username has to be checked for duplicates even when username validation is disabled
            if (validationMode == CredentialValidationMode.NO_VALIDATION || validationMode == CredentialValidationMode.VALIDATE_CREDENTIAL) {
                Optional<CredentialEntity> existingCredentialOptional = credentialRepository.findByCredentialDefinitionAndUsername(credentialDefinition, username);
                if (existingCredentialOptional.isPresent()) {
                    CredentialEntity existingCredential = existingCredentialOptional.get();
                    if (!existingCredential.getUser().equals(user)) {
                        CredentialValidationError error = new CredentialValidationError(CredentialValidationFailedException.CODE,
                                "Username validation failed", Collections.singletonList(CredentialValidationFailure.USERNAME_ALREADY_EXISTS));
                        throw new CredentialValidationFailedException("Username validation failed for user ID: " + user.getUserId(), error);
                    }
                }
            }
        } else {
            if (credential.getUsername() != null) {
                username = credential.getUsername();
            } else {
                username = generateUsername(credentialDefinition);
            }
        }
        credential.setType(credentialType);
        credential.setUsername(username);
        String credentialValueRequest = credentialValue;
        if (credentialValue == null) {
            credentialValue = generateCredentialValue(credentialDefinition);
        }
        List<CredentialValidationFailure> validationErrors = validateCredential(user, credentialDefinition, username, credentialValue, validationMode);
        if (!validationErrors.isEmpty()) {
            CredentialValidationError error = new CredentialValidationError(CredentialValidationFailedException.CODE, "Validation failed", validationErrors);
            throw new CredentialValidationFailedException("Validation failed for user ID: " + user.getUserId(), error);
        }
        credential.setValue(credentialValue);
        credential.setTimestampLastCredentialChange(new Date());
        credential.setStatus(CredentialStatus.ACTIVE);
        credential.setTimestampBlocked(null);
        // Counters are reset even in case of an existing credential
        credential.setAttemptCounter(0);
        credential.setFailedAttemptCounterSoft(0);
        credential.setFailedAttemptCounterHard(0);
        credentialRepository.save(credential);
        // Save original credential into credential history
        credentialHistoryService.createCredentialHistory(credential);
        CredentialSecretDetail credentialDetail = new CredentialSecretDetail();
        credentialDetail.setCredentialName(credential.getCredentialDefinition().getName());
        credentialDetail.setCredentialType(credential.getType());
        credentialDetail.setCredentialStatus(CredentialStatus.ACTIVE);
        credentialDetail.setUsername(credential.getUsername());
        if (credentialValueRequest == null) {
            credentialDetail.setCredentialValue(credential.getValue());
        }
        credentialDetail.setCredentialChangeRequired(isCredentialChangeRequired(credential));
        credentialDetail.setTimestampCreated(credential.getTimestampCreated());
        credentialDetail.setTimestampLastUpdated(credential.getTimestampLastUpdated());
        credentialDetail.setTimestampExpires(credential.getTimestampExpires());
        credentialDetail.setTimestampBlocked(credential.getTimestampBlocked());
        credentialDetail.setTimestampLastCredentialChange(credential.getTimestampLastCredentialChange());
        return credentialDetail;
    }

    /**
     * Import credential history record.
     * @param user User identity entity.
     * @param credentialDefinition Credential definition.
     * @param username Username.
     * @param credentialValue Credential value.
     */
    public void importCredentialHistory(UserIdentityEntity user, CredentialDefinitionEntity credentialDefinition,
                                        String username, String credentialValue) {
        CredentialEntity credential = new CredentialEntity();
        credential.setUser(user);
        credential.setCredentialDefinition(credentialDefinition);
        credential.setUsername(username);
        credential.setValue(credentialValue);
        credentialHistoryService.createCredentialHistory(credential);
    }

    /**
     * Generate a username as defined in credential policy.
     * @param credentialDefinition Credential definition.
     * @return Generated username.
     * @throws InvalidConfigurationException Thrown in case username could not be generated.
     */
    private String generateUsername(CredentialDefinitionEntity credentialDefinition) throws InvalidConfigurationException {
        // TODO - move to a separate service
        CredentialPolicyEntity credentialPolicy = credentialDefinition.getCredentialPolicy();
        switch (credentialPolicy.getUsernameGenAlgorithm()) {
            case "DEFAULT":
            case "RANDOM_DIGITS":
                try {
                    Map<String, String> param = parameterConverter.fromString(credentialPolicy.getUsernameGenParam());
                    String paramLength = param.get("length");
                    if (paramLength == null) {
                        throw new InvalidConfigurationException("Parameter length is missing for algorithm RANDOM_DIGITS");
                    }
                    int length = Integer.parseInt(paramLength);
                    SecureRandom secureRandom = new SecureRandom();
                    for (int i = 0; i < GENERATE_USERNAME_MAX_ATTEMPTS; i++) {
                        BigInteger bound = BigInteger.valueOf(Math.round(Math.pow(10, length)));
                        BigInteger randomNumber = new BigInteger(bound.bitLength(), secureRandom).mod(bound);
                        String username = randomNumber.toString();
                        if (username.length() < length) {
                            // This can happen with leading zeros
                            continue;
                        }
                        Optional<CredentialEntity> credentialOptional = credentialRepository.findByCredentialDefinitionAndUsername(credentialDefinition, username);
                        if (credentialOptional.isPresent()) {
                            // Username is already taken
                            continue;
                        }
                        return username;
                    }
                    throw new InvalidConfigurationException("Username could not be generated, all attempts failed");
                } catch (InvalidConfigurationException ex) {
                    throw ex;
                } catch (Exception ex) {
                    throw new InvalidConfigurationException(ex);
                }

            case "RANDOM_LETTERS":
                try {
                    Map<String, String> param = parameterConverter.fromString(credentialPolicy.getUsernameGenParam());
                    String paramLength = param.get("length");
                    if (paramLength == null) {
                        throw new InvalidConfigurationException("Parameter length is missing for algorithm RANDOM_LETTERS");
                    }
                    int length = Integer.parseInt(paramLength);
                    SecureRandom secureRandom = new SecureRandom();
                    for (int i = 0; i < GENERATE_USERNAME_MAX_ATTEMPTS; i++) {
                        StringBuilder usernameBuilder = new StringBuilder();
                        for (int j = 0; j < length; j++) {
                            char c = (char) (secureRandom.nextInt(26) + 'a');
                            usernameBuilder.append(c);
                        }
                        String username = usernameBuilder.toString();
                        Optional<CredentialEntity> credentialOptional = credentialRepository.findByCredentialDefinitionAndUsername(credentialDefinition, username);
                        if (credentialOptional.isPresent()) {
                            // Username is already taken
                            continue;
                        }
                        return username;
                    }
                    throw new InvalidConfigurationException("Username could not be generated, all attempts failed");
                } catch (InvalidConfigurationException ex) {
                    throw ex;
                } catch (Exception ex) {
                    throw new InvalidConfigurationException(ex);
                }

            default:
                throw new InvalidConfigurationException("Unsupported username generation algorithm: " + credentialPolicy.getUsernameGenAlgorithm());
        }
    }

    /**
     * Generate a credential value as defined in credential policy.
     * @param credentialDefinition Credential definition.
     * @return Generated credential value.
     * @throws InvalidConfigurationException Thrown in case credential value could not be generated.
     */
    private String generateCredentialValue(CredentialDefinitionEntity credentialDefinition) throws InvalidConfigurationException {
        CredentialPolicyEntity credentialPolicy = credentialDefinition.getCredentialPolicy();
        switch (credentialPolicy.getCredentialGenAlgorithm()) {
            case "DEFAULT":
            case "RANDOM_PASSWORD":
                try {
                    // TODO - switch password generation to Passay library with more advanced rules
                    Map<String, String> param = parameterConverter.fromString(credentialPolicy.getCredentialGenParam());
                    String paramLength = param.get("length");
                    if (paramLength == null) {
                        throw new InvalidConfigurationException("Parameter length is missing for algorithm RANDOM_PASSWORD");
                    }
                    String paramSmallLetters = param.get("includeSmallLetters");
                    boolean includeSmallLetters = "true".equals(paramSmallLetters);
                    String paramCapitalLetters = param.get("includeCapitalLetters");
                    boolean includeCapitalLetters = "true".equals(paramCapitalLetters);
                    String paramDigits = param.get("includeDigits");
                    boolean includeDigits = "true".equals(paramDigits);
                    String paramSpecialChars = param.get("includeSpecialChars");
                    boolean includeSpecialChars = "true".equals(paramSpecialChars);
                    if (!includeSmallLetters && !includeCapitalLetters && !includeDigits && !includeSpecialChars) {
                        throw new InvalidConfigurationException("Invalid configuration of algorithm RANDOM_PASSWORD");
                    }
                    String paramSpecialCharLimit = param.get("specialCharLimit");
                    int length = Integer.parseInt(paramLength);
                    int specialCharLimit = length;
                    if (paramSpecialCharLimit != null) {
                        specialCharLimit = Integer.parseInt(paramSpecialCharLimit);
                    }
                    SecureRandom secureRandom = new SecureRandom();
                    StringBuilder credentialBuilder = new StringBuilder();
                    StringBuilder availableCharsBuilder = new StringBuilder();
                    int specialCharCounter = 0;
                    if (includeSmallLetters) {
                        availableCharsBuilder.append("abcdefghijklmnopqrstuvwyz");
                    }
                    if (includeCapitalLetters) {
                        availableCharsBuilder.append("ABCDEFGHIJKLMNOPQRSTUVWYZ");
                    }
                    if (includeDigits) {
                        availableCharsBuilder.append("0123456789");
                    }
                    if (includeSpecialChars) {
                        availableCharsBuilder.append("^<>{}|;:.,~!?@#$%^=&*[]()");
                    }
                    String availableChars = availableCharsBuilder.toString();
                    while (credentialBuilder.length() < length) {
                        int randomInt = secureRandom.nextInt(availableChars.length());
                        char c = availableChars.charAt(randomInt);
                        if (Character.isAlphabetic(c) || Character.isDigit(c)) {
                            credentialBuilder.append(c);
                            continue;
                        }
                        if (specialCharCounter < specialCharLimit) {
                            specialCharCounter++;
                            credentialBuilder.append(c);
                        }
                    }
                    return credentialBuilder.toString();
                } catch (InvalidConfigurationException ex) {
                    throw ex;
                } catch (Exception ex) {
                    throw new InvalidConfigurationException(ex);
                }

            case "RANDOM_PIN":
                try {
                    Map<String, String> param = parameterConverter.fromString(credentialPolicy.getCredentialGenParam());
                    String paramLength = param.get("length");
                    if (paramLength == null) {
                        throw new InvalidConfigurationException("Parameter length is missing for algorithm RANDOM_PIN");
                    }
                    int length = Integer.parseInt(paramLength);
                    SecureRandom secureRandom = new SecureRandom();
                    StringBuilder credentialBuilder = new StringBuilder();
                    while (credentialBuilder.length() < length) {
                        int randomInt = secureRandom.nextInt(10);
                        credentialBuilder.append(randomInt);
                    }
                    return credentialBuilder.toString();
                } catch (InvalidConfigurationException ex) {
                    throw ex;
                } catch (Exception ex) {
                    throw new InvalidConfigurationException(ex);
                }

            default:
                throw new InvalidConfigurationException("Unsupported credential value generation algorithm: " + credentialPolicy.getCredentialGenAlgorithm());
        }
    }

}