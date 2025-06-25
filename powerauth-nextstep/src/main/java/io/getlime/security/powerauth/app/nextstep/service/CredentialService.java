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
import io.getlime.security.powerauth.app.nextstep.configuration.NextStepServerConfiguration;
import io.getlime.security.powerauth.app.nextstep.converter.CredentialConverter;
import io.getlime.security.powerauth.app.nextstep.repository.CredentialRepository;
import io.getlime.security.powerauth.app.nextstep.repository.UserIdentityRepository;
import io.getlime.security.powerauth.app.nextstep.repository.catalogue.RepositoryCatalogue;
import io.getlime.security.powerauth.app.nextstep.repository.model.entity.CredentialDefinitionEntity;
import io.getlime.security.powerauth.app.nextstep.repository.model.entity.CredentialEntity;
import io.getlime.security.powerauth.app.nextstep.repository.model.entity.CredentialPolicyEntity;
import io.getlime.security.powerauth.app.nextstep.repository.model.entity.UserIdentityEntity;
import io.getlime.security.powerauth.app.nextstep.service.catalogue.ServiceCatalogue;
import io.getlime.security.powerauth.lib.nextstep.model.entity.CredentialDetail;
import io.getlime.security.powerauth.lib.nextstep.model.entity.CredentialSecretDetail;
import io.getlime.security.powerauth.lib.nextstep.model.entity.CredentialValue;
import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.*;
import io.getlime.security.powerauth.lib.nextstep.model.entity.error.CredentialValidationError;
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

/**
 * This service handles persistence of credentials.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Service
public class CredentialService {

    private final Logger logger = LoggerFactory.getLogger(CredentialService.class);
    private static final String AUDIT_TYPE_USER_IDENTITY = "USER_IDENTITY";

    private final RepositoryCatalogue repositoryCatalogue;
    private final ServiceCatalogue serviceCatalogue;
    private final NextStepServerConfiguration nextStepServerConfiguration;
    private final Audit audit;

    private final CredentialConverter credentialConverter = new CredentialConverter();

    /**
     * Credential service constructor.
     * @param repositoryCatalogue Repository catalogue.
     * @param serviceCatalogue Service catalogue.
     * @param nextStepServerConfiguration Next Step server configuration.
     * @param audit Audit interface.
     */
    @Autowired
    public CredentialService(RepositoryCatalogue repositoryCatalogue, @Lazy ServiceCatalogue serviceCatalogue, NextStepServerConfiguration nextStepServerConfiguration, Audit audit) {
        this.repositoryCatalogue = repositoryCatalogue;
        this.serviceCatalogue = serviceCatalogue;
        this.nextStepServerConfiguration = nextStepServerConfiguration;
        this.audit = audit;
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
     * @throws EncryptionException Thrown when encryption or decryption fails.
     */
    @Transactional
    public CreateCredentialResponse createCredential(CreateCredentialRequest request) throws UserNotFoundException, CredentialDefinitionNotFoundException, InvalidConfigurationException, InvalidRequestException, CredentialValidationFailedException, EncryptionException {
        final UserIdentityLookupService userIdentityLookupService = serviceCatalogue.getUserIdentityLookupService();
        final CredentialDefinitionService credentialDefinitionService = serviceCatalogue.getCredentialDefinitionService();
        final EndToEndEncryptionService endToEndEncryptionService = serviceCatalogue.getEndToEndEncryptionService();
        final UserIdentityRepository userIdentityRepository = repositoryCatalogue.getUserIdentityRepository();

        UserIdentityEntity user = userIdentityLookupService.findUser(request.getUserId());
        final CredentialDefinitionEntity credentialDefinition = credentialDefinitionService.findActiveCredentialDefinition(request.getCredentialName());
        final CredentialType credentialType = request.getCredentialType();
        final String username = request.getUsername();
        String credentialValue = request.getCredentialValue();
        if (credentialValue != null && credentialDefinition.isE2eEncryptionEnabled()) {
            credentialValue = endToEndEncryptionService.decryptCredential(credentialValue, credentialDefinition);
        }
        final Date timestampExpires = request.getTimestampExpires();
        CredentialValidationMode validationMode = request.getValidationMode();
        final List<CreateCredentialRequest.CredentialHistory> credentialHistory = request.getCredentialHistory();
        if (validationMode == null) {
            validationMode = CredentialValidationMode.VALIDATE_USERNAME_AND_CREDENTIAL;
        }
        final CredentialSecretDetail credentialDetail = createCredential(user, credentialDefinition, credentialType, username, credentialValue, timestampExpires, validationMode);
        if (credentialHistory != null && !credentialHistory.isEmpty()) {
            final int dateCount = credentialHistory.size();
            // Use unique timestamps in seconds to keep order of credential history
            long createdTimestamp = new Date().getTime() - (dateCount * 1000L);
            for (CreateCredentialRequest.CredentialHistory h : credentialHistory) {
                final Date createdDate = new Date(createdTimestamp);
                String credentialValueHistory = h.getCredentialValue();
                if (credentialDefinition.isE2eEncryptionEnabled()) {
                    credentialValueHistory = endToEndEncryptionService.decryptCredential(credentialValueHistory, credentialDefinition);
                }
                importCredentialHistory(user, credentialDefinition, h.getUsername(), credentialValueHistory, createdDate);
                createdTimestamp += 1000;
            }
        }
        user = userIdentityRepository.save(user);
        logger.debug("Credential was created for user ID: {}, credential definition name: {}", user.getUserId(), credentialDefinition.getName());
        audit.info("Credential was created", AuditDetail.builder()
                .type(AUDIT_TYPE_USER_IDENTITY)
                .param("userId", user.getUserId())
                .param("credentialDefinitionName", credentialDefinition.getName())
                .build());
        final CreateCredentialResponse response = new CreateCredentialResponse();
        response.setCredentialName(credentialDetail.getCredentialName());
        response.setCredentialType(credentialDetail.getCredentialType());
        response.setUserId(user.getUserId());
        response.setCredentialStatus(credentialDetail.getCredentialStatus());
        response.setUsername(credentialDetail.getUsername());
        if (request.getCredentialValue() == null) {
            // Return generated credential value, with possible end2end encryption
            String credentialValueResponse = credentialDetail.getCredentialValue();
            if (credentialDefinition.isE2eEncryptionEnabled() &&
                    (credentialDetail.getCredentialType() == CredentialType.PERMANENT || credentialDefinition.isE2eEncryptionForTemporaryCredentialEnabled())) {
                credentialValueResponse = endToEndEncryptionService.encryptCredential(credentialValueResponse, credentialDefinition);
            }
            response.setCredentialValue(credentialValueResponse);
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
     * @throws InvalidConfigurationException Thrown when Next Step configuration is invalid.
     * @throws EncryptionException Thrown when encryption or decryption fails.
     */
    @Transactional
    public UpdateCredentialResponse updateCredential(UpdateCredentialRequest request) throws UserNotFoundException, CredentialDefinitionNotFoundException, CredentialNotFoundException, CredentialValidationFailedException, InvalidRequestException, InvalidConfigurationException, EncryptionException {
        final UserIdentityLookupService userIdentityLookupService = serviceCatalogue.getUserIdentityLookupService();
        final CredentialDefinitionService credentialDefinitionService = serviceCatalogue.getCredentialDefinitionService();
        final EndToEndEncryptionService endToEndEncryptionService = serviceCatalogue.getEndToEndEncryptionService();
        final CredentialValidationService credentialValidationService = serviceCatalogue.getCredentialValidationService();
        final CredentialProtectionService credentialProtectionService = serviceCatalogue.getCredentialProtectionService();
        final CredentialHistoryService credentialHistoryService = serviceCatalogue.getCredentialHistoryService();
        final UserIdentityRepository userIdentityRepository = repositoryCatalogue.getUserIdentityRepository();

        UserIdentityEntity user = userIdentityLookupService.findUser(request.getUserId());
        final CredentialDefinitionEntity credentialDefinition = credentialDefinitionService.findActiveCredentialDefinition(request.getCredentialName());
        final Optional<CredentialEntity> credentialOptional = user.getCredentials().stream().filter(c -> c.getCredentialDefinition().equals(credentialDefinition)).findFirst();
        if (credentialOptional.isEmpty()) {
            throw new CredentialNotFoundException("Credential not found: " + request.getCredentialName() + ", user ID: " + user.getUserId());
        }
        boolean updateCredentialExpiration = false;
        final CredentialEntity credential = credentialOptional.get();
        if (credential.getStatus() == CredentialStatus.REMOVED && request.getCredentialStatus() == null) {
            throw new CredentialNotFoundException("Credential is REMOVED: " + request.getCredentialName() + ", user ID: " + user.getUserId());
        }
        if (credential.getStatus() == CredentialStatus.REMOVED && request.getCredentialStatus() == CredentialStatus.REMOVED) {
            throw new CredentialNotFoundException("Credential is already REMOVED: " + request.getCredentialName() + ", user ID: " + user.getUserId());
        }
        if (request.getCredentialType() != null) {
            credential.setType(request.getCredentialType());
            updateCredentialExpiration = true;
        }
        String username = null;
        String credentialValue = request.getCredentialValue();
        if (credentialValue != null && credentialDefinition.isE2eEncryptionEnabled()) {
            credentialValue = endToEndEncryptionService.decryptCredential(credentialValue, credentialDefinition);
        }
        CredentialValidationMode validationMode = CredentialValidationMode.NO_VALIDATION;
        if (request.getUsername() != null && request.getCredentialValue() != null) {
            username = request.getUsername().toLowerCase();
            validationMode = CredentialValidationMode.VALIDATE_USERNAME_AND_CREDENTIAL;
        } else if (request.getCredentialValue() != null) {
            username = credential.getUsername().toLowerCase();
            validationMode = CredentialValidationMode.VALIDATE_CREDENTIAL;
        } else if (request.getUsername() != null) {
            username = request.getUsername().toLowerCase();
            validationMode = CredentialValidationMode.VALIDATE_USERNAME;
        }
        if (request.getUsername() != null || request.getCredentialValue() != null) {
            final List<CredentialValidationFailure> validationErrors = credentialValidationService.validateCredential(user,
                    credentialDefinition, username, credentialValue, validationMode);
            if (!validationErrors.isEmpty()) {
                CredentialValidationError error = new CredentialValidationError(CredentialValidationFailedException.CODE, "Validation failed", validationErrors);
                throw new CredentialValidationFailedException("Validation failed for user ID: " + user.getUserId(), error);
            }
        }
        Date changeTimestamp = new Date();
        if (request.getUsername() != null && !request.getUsername().equals(credential.getUsername())) {
            credential.setUsername(username);
            credential.setTimestampLastUsernameChange(changeTimestamp);
        }
        if (credentialValue != null) {
            final CredentialValue protectedValue = credentialProtectionService.protectCredential(credentialValue, credential);
            credential.setValue(protectedValue.getValue());
            credential.setEncryptionAlgorithm(protectedValue.getEncryptionAlgorithm());
            credential.setHashingConfig(credentialDefinition.getHashingConfig());
            credential.setTimestampLastCredentialChange(changeTimestamp);
            updateCredentialExpiration = true;
        }
        if (request.getTimestampExpires() != null) {
            // Credential expiration is set in the request
            credential.setTimestampExpires(request.getTimestampExpires());
        } else if (updateCredentialExpiration) {
            // Credential expiration needs to be derived
            updateCredentialExpirationTime(credential, credentialDefinition.getCredentialPolicy());
        }
        if (request.getCredentialStatus() != null) {
            credential.setStatus(request.getCredentialStatus());
                if (credential.getStatus() == CredentialStatus.BLOCKED_TEMPORARY || credential.getStatus() == CredentialStatus.BLOCKED_PERMANENT){
                // For blocked credentials set timestamp when credential was blocked
                credential.setTimestampBlocked(changeTimestamp);
            } else if (credential.getStatus() == CredentialStatus.ACTIVE) {
                // Reset counters for active credentials
                credential.setFailedAttemptCounterSoft(0);
                credential.setFailedAttemptCounterHard(0);
                credential.setTimestampBlocked(null);
            }
        }
        credential.setTimestampLastUpdated(changeTimestamp);
        if (request.getCredentialValue() != null) {
            // Save credential into credential history
            credentialHistoryService.createCredentialHistory(user, credential, changeTimestamp);
        }
        user = userIdentityRepository.save(user);
        logger.debug("Credential was updated for user ID: {}, credential definition name: {}", user.getUserId(), credentialDefinition.getName());
        audit.info("Credential was updated", AuditDetail.builder()
                .type(AUDIT_TYPE_USER_IDENTITY)
                .param("userId", user.getUserId())
                .param("credentialDefinitionName", credentialDefinition.getName())
                .build());
        final UpdateCredentialResponse response = new UpdateCredentialResponse();
        response.setUserId(user.getUserId());
        response.setCredentialName(credential.getCredentialDefinition().getName());
        response.setCredentialType(credential.getType());
        response.setCredentialStatus(credential.getStatus());
        response.setUsername(credential.getUsername());
        final boolean credentialChangeRequired;
        if (request.getCredentialValue() != null) {
            credentialChangeRequired = isCredentialChangeRequired(credential, credentialValue);
        } else {
            if (credentialDefinition.getHashingConfig() == null) {
                credentialChangeRequired = isCredentialChangeRequired(credential, credential.getValue());
            } else {
                credentialChangeRequired = isCredentialChangeRequired(credential, null);
            }
        }
        response.setCredentialChangeRequired(credentialChangeRequired);
        return response;
    }

    /**
     * Get credential list for a user identity.
     * @param request Get credential list request.
     * @return Get credential list response.
     * @throws UserNotFoundException Thrown when user identity is not found.
     * @throws InvalidConfigurationException Thrown when Next Step configuration is invalid.
     * @throws EncryptionException Thrown when decryption fails.
     */
    @Transactional
    public GetUserCredentialListResponse getCredentialList(GetUserCredentialListRequest request) throws UserNotFoundException, InvalidConfigurationException, EncryptionException {
        final UserIdentityLookupService userIdentityLookupService = serviceCatalogue.getUserIdentityLookupService();
        final UserIdentityEntity user = userIdentityLookupService.findUser(request.getUserId());
        final GetUserCredentialListResponse response = new GetUserCredentialListResponse();
        response.setUserId(user.getUserId());
        final Set<CredentialEntity> credentials = user.getCredentials();
        for (CredentialEntity credential: credentials) {
            if (credential.getStatus() == CredentialStatus.REMOVED && !request.isIncludeRemoved()) {
                continue;
            }
            final CredentialDetail credentialDetail = credentialConverter.fromEntity(credential);
            final boolean credentialChangeRequired;
            if (credential.getCredentialDefinition().getHashingConfig() == null) {
                credentialChangeRequired = isCredentialChangeRequired(credential, credential.getValue());
            } else {
                credentialChangeRequired = isCredentialChangeRequired(credential, null);
            }
            credentialDetail.setCredentialChangeRequired(credentialChangeRequired);
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
     * @throws InvalidConfigurationException Thrown when Next Step configuration is invalid.
     * @throws EncryptionException Thrown when decryption fails.
     */
    @Transactional
    public ValidateCredentialResponse validateCredential(ValidateCredentialRequest request) throws CredentialDefinitionNotFoundException, InvalidRequestException, UserNotFoundException, InvalidConfigurationException, EncryptionException {
        final UserIdentityLookupService userIdentityLookupService = serviceCatalogue.getUserIdentityLookupService();
        final CredentialDefinitionService credentialDefinitionService = serviceCatalogue.getCredentialDefinitionService();
        final EndToEndEncryptionService endToEndEncryptionService = serviceCatalogue.getEndToEndEncryptionService();
        final CredentialValidationService credentialValidationService = serviceCatalogue.getCredentialValidationService();

        final UserIdentityEntity user = userIdentityLookupService.findUser(request.getUserId());
        final CredentialDefinitionEntity credentialDefinition = credentialDefinitionService.findActiveCredentialDefinition(request.getCredentialName());
        final String username = request.getUsername();
        String credentialValue = request.getCredentialValue();
        final CredentialValidationMode validationMode = request.getValidationMode();
        if (credentialValue != null && credentialDefinition.isE2eEncryptionEnabled()) {
            credentialValue = endToEndEncryptionService.decryptCredential(credentialValue, credentialDefinition);
        }
        final List<CredentialValidationFailure> validationErrors = credentialValidationService.validateCredential(user,
                credentialDefinition, username, credentialValue, validationMode);
        final ValidateCredentialResponse response = new ValidateCredentialResponse();
        if (validationErrors.isEmpty()) {
            response.setValidationResult(CredentialValidationResult.SUCCEEDED);
        } else {
            response.setValidationResult(CredentialValidationResult.FAILED);
        }
        logger.debug("Credential validation result: {}, validation errors: {}", response.getValidationResult(), response.getValidationErrors());
        response.getValidationErrors().addAll(validationErrors);
        return response;
    }

    /**
     * Check whether credential change is required.
     * @param credential Credential entity.
     * @param unprotectedCredentialValue Unprotected credential value.
     * @return Whether credential change is required.
     * @throws InvalidConfigurationException Thrown when Next Step configuration is invalid.
     * @throws EncryptionException Thrown when decryption fails.
     */
    public boolean isCredentialChangeRequired(CredentialEntity credential, String unprotectedCredentialValue) throws InvalidConfigurationException, EncryptionException {
        final CredentialValidationService credentialValidationService = serviceCatalogue.getCredentialValidationService();
        // Check expiration time
        final Date expirationTime = credential.getTimestampExpires();
        if (expirationTime != null && new Date().after(expirationTime)) {
            return true;
        }
        // Perform an actual check of credential expiration for case that credential policy was updated after last credential change
        final CredentialPolicyEntity credentialPolicy = credential.getCredentialDefinition().getCredentialPolicy();
        if (credentialPolicy.isRotationEnabled()) {
            final Date lastChange = credential.getTimestampLastCredentialChange();
            if (lastChange == null) {
                // Only happens when data in database is manipulated
                credential.setTimestampExpires(new Date());
                return true;
            }
            final Calendar c = GregorianCalendar.getInstance();
            c.add(Calendar.DAY_OF_YEAR, -credentialPolicy.getRotationDays());
            if (lastChange.before(c.getTime())) {
                // Last credential change occurred before time calculated by password rotation days
                credential.setTimestampExpires(new Date());
                return true;
            }
        }
        if (unprotectedCredentialValue == null) {
            return false;
        }
        final List<CredentialValidationFailure> validationFailures = credentialValidationService.validateCredentialValue(credential.getUser(),
                credential.getUsername(), unprotectedCredentialValue, credential.getCredentialDefinition(), false);
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
     * @throws EncryptionException Thrown when encryption fails.
     */
    @Transactional
    public ResetCredentialResponse resetCredential(ResetCredentialRequest request) throws UserNotFoundException, CredentialDefinitionNotFoundException, CredentialNotFoundException, InvalidConfigurationException, EncryptionException {
        final UserIdentityLookupService userIdentityLookupService = serviceCatalogue.getUserIdentityLookupService();
        final CredentialDefinitionService credentialDefinitionService = serviceCatalogue.getCredentialDefinitionService();
        final CredentialGenerationService credentialGenerationService = serviceCatalogue.getCredentialGenerationService();
        final CredentialProtectionService credentialProtectionService = serviceCatalogue.getCredentialProtectionService();
        final CredentialHistoryService credentialHistoryService = serviceCatalogue.getCredentialHistoryService();
        final UserIdentityRepository userIdentityRepository = repositoryCatalogue.getUserIdentityRepository();
        final EndToEndEncryptionService endToEndEncryptionService = serviceCatalogue.getEndToEndEncryptionService();

        UserIdentityEntity user = userIdentityLookupService.findUser(request.getUserId());
        final CredentialDefinitionEntity credentialDefinition = credentialDefinitionService.findActiveCredentialDefinition(request.getCredentialName());
        final Optional<CredentialEntity> credentialOptional = user.getCredentials().stream().filter(c -> c.getCredentialDefinition().equals(credentialDefinition)).findFirst();
        if (credentialOptional.isEmpty()) {
            throw new CredentialNotFoundException("Credential not found: " + request.getCredentialName() + ", user ID: " + user.getUserId());
        }
        final CredentialEntity credential = credentialOptional.get();
        if (credential.getStatus() == CredentialStatus.REMOVED) {
            throw new CredentialNotFoundException("Credential is REMOVED: " + request.getCredentialName() + ", user ID: " + user.getUserId());
        }
        if (request.getCredentialType() != null) {
            credential.setType(request.getCredentialType());
        }
        if (request.getTimestampExpires() != null) {
            // Credential expiration is set in the request
            credential.setTimestampExpires(request.getTimestampExpires());
        } else {
            // Credential expiration needs to be derived
            updateCredentialExpirationTime(credential, credentialDefinition.getCredentialPolicy());
        }
        final String unprotectedCredentialValue = credentialGenerationService.generateCredentialValue(credentialDefinition);
        final CredentialValue protectedCredentialValue = credentialProtectionService.protectCredential(unprotectedCredentialValue, credential);
        credential.setValue(protectedCredentialValue.getValue());
        credential.setEncryptionAlgorithm(protectedCredentialValue.getEncryptionAlgorithm());
        credential.setHashingConfig(credentialDefinition.getHashingConfig());
        Date changeTimestamp = new Date();
        credential.setTimestampLastUpdated(changeTimestamp);
        credential.setTimestampLastCredentialChange(changeTimestamp);
        credential.setFailedAttemptCounterSoft(0);
        credential.setFailedAttemptCounterHard(0);
        credential.setStatus(CredentialStatus.ACTIVE);
        credential.setTimestampBlocked(null);
        // Save credential into credential history
        credentialHistoryService.createCredentialHistory(user, credential, changeTimestamp);
        user = userIdentityRepository.save(user);
        logger.debug("Credential was reset for user ID: {}, credential definition name: {}", user.getUserId(), credentialDefinition.getName());
        audit.info("Credential was reset", AuditDetail.builder()
                .type(AUDIT_TYPE_USER_IDENTITY)
                .param("userId", user.getUserId())
                .param("credentialDefinitionName", credentialDefinition.getName())
                .build());
        final ResetCredentialResponse response = new ResetCredentialResponse();
        response.setUserId(user.getUserId());
        response.setCredentialName(credential.getCredentialDefinition().getName());
        response.setUsername(credential.getUsername());
        // Generated password must be returned in unprotected form
        String credentialValueResponse = unprotectedCredentialValue;
        if (credentialDefinition.isE2eEncryptionEnabled() &&
                (credential.getType() == CredentialType.PERMANENT || credentialDefinition.isE2eEncryptionForTemporaryCredentialEnabled())) {
            credentialValueResponse = endToEndEncryptionService.encryptCredential(credentialValueResponse, credentialDefinition);
        }
        response.setCredentialValue(credentialValueResponse);
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
        final UserIdentityLookupService userIdentityLookupService = serviceCatalogue.getUserIdentityLookupService();
        final CredentialDefinitionService credentialDefinitionService = serviceCatalogue.getCredentialDefinitionService();
        final UserIdentityRepository userIdentityRepository = repositoryCatalogue.getUserIdentityRepository();

        UserIdentityEntity user = userIdentityLookupService.findUser(request.getUserId());
        final CredentialDefinitionEntity credentialDefinition = credentialDefinitionService.findActiveCredentialDefinition(request.getCredentialName());
        final Optional<CredentialEntity> credentialOptional = user.getCredentials().stream().filter(c -> c.getCredentialDefinition().equals(credentialDefinition)).findFirst();
        if (credentialOptional.isEmpty()) {
            throw new CredentialNotFoundException("Credential not found: " + request.getCredentialName() + ", user ID: " + user.getUserId());
        }
        final CredentialEntity credential = credentialOptional.get();
        if (credential.getStatus() == CredentialStatus.REMOVED) {
            throw new CredentialNotFoundException("Credential is already REMOVED: " + request.getCredentialName() + ", user ID: " + user.getUserId());
        }
        credential.setStatus(CredentialStatus.REMOVED);
        credential.setUsername(null);
        user = userIdentityRepository.save(user);
        logger.debug("Credential was removed for user ID: {}, credential definition name: {}", user.getUserId(), credentialDefinition.getName());
        audit.info("Credential was removed", AuditDetail.builder()
                .type(AUDIT_TYPE_USER_IDENTITY)
                .param("userId", user.getUserId())
                .param("credentialDefinitionName", credentialDefinition.getName())
                .build());
        final DeleteCredentialResponse response = new DeleteCredentialResponse();
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
        final UserIdentityLookupService userIdentityLookupService = serviceCatalogue.getUserIdentityLookupService();
        final CredentialDefinitionService credentialDefinitionService = serviceCatalogue.getCredentialDefinitionService();
        final UserIdentityRepository userIdentityRepository = repositoryCatalogue.getUserIdentityRepository();

        UserIdentityEntity user = userIdentityLookupService.findUser(request.getUserId());
        final CredentialDefinitionEntity credentialDefinition = credentialDefinitionService.findActiveCredentialDefinition(request.getCredentialName());
        final Optional<CredentialEntity> credentialOptional = user.getCredentials().stream().filter(c -> c.getCredentialDefinition().equals(credentialDefinition)).findFirst();
        if (credentialOptional.isEmpty()) {
            throw new CredentialNotFoundException("Credential not found: " + request.getCredentialName() + ", user ID: " + user.getUserId());
        }
        final CredentialEntity credential = credentialOptional.get();
        if (credential.getStatus() != CredentialStatus.ACTIVE && credential.getStatus() != CredentialStatus.BLOCKED_TEMPORARY) {
            throw new CredentialNotActiveException("Credential is not ACTIVE or BLOCKED_TEMPORARY: " + request.getCredentialName() + ", user ID: " + user.getUserId());
        }
        credential.setStatus(CredentialStatus.BLOCKED_PERMANENT);
        credential.setTimestampBlocked(new Date());
        user = userIdentityRepository.save(user);
        logger.debug("Credential was blocked for user ID: {}, credential definition name: {}", user.getUserId(), credentialDefinition.getName());
        audit.info("Credential was blocked", AuditDetail.builder()
                .type(AUDIT_TYPE_USER_IDENTITY)
                .param("userId", user.getUserId())
                .param("credentialDefinitionName", credentialDefinition.getName())
                .build());
        final BlockCredentialResponse response = new BlockCredentialResponse();
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
        final UserIdentityLookupService userIdentityLookupService = serviceCatalogue.getUserIdentityLookupService();
        final CredentialDefinitionService credentialDefinitionService = serviceCatalogue.getCredentialDefinitionService();
        final UserIdentityRepository userIdentityRepository = repositoryCatalogue.getUserIdentityRepository();

        UserIdentityEntity user = userIdentityLookupService.findUser(request.getUserId());
        final CredentialDefinitionEntity credentialDefinition = credentialDefinitionService.findActiveCredentialDefinition(request.getCredentialName());
        final Optional<CredentialEntity> credentialOptional = user.getCredentials().stream().filter(c -> c.getCredentialDefinition().equals(credentialDefinition)).findFirst();
        if (credentialOptional.isEmpty()) {
            throw new CredentialNotFoundException("Credential not found: " + request.getCredentialName() + ", user ID: " + user.getUserId());
        }
        final CredentialEntity credential = credentialOptional.get();
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
        user = userIdentityRepository.save(user);
        logger.debug("Credential was unblocked for user ID: {}, credential definition name: {}", user.getUserId(), credentialDefinition.getName());
        audit.info("Credential was unblocked", AuditDetail.builder()
                .type(AUDIT_TYPE_USER_IDENTITY)
                .param("userId", user.getUserId())
                .param("credentialDefinitionName", credentialDefinition.getName())
                .build());
        final UnblockCredentialResponse response = new UnblockCredentialResponse();
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
        final CredentialEntity credential = findCredential(credentialDefinition, user);
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
        return user.getCredentials().stream()
                .filter(c -> c.getCredentialDefinition().equals(credentialDefinition))
                .findFirst().orElseThrow(() ->
                        new CredentialNotFoundException("Credential not found: " + credentialDefinition.getName()));
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
     * @param timestampExpires Expiration timestamp for case when expiration timestamp is overridden.
     * @param validationMode Credential validation mode.
     * @throws InvalidConfigurationException Thrown when Next Step configuration is invalid.
     * @throws CredentialValidationFailedException Thrown when credential validation fails.
     * @throws InvalidRequestException Thrown when request is invalid.
     * @throws EncryptionException Thrown when encryption or decryption fails.
     */
    public CredentialSecretDetail createCredential(UserIdentityEntity user, CredentialDefinitionEntity credentialDefinition,
                                                   CredentialType credentialType, String username, String credentialValue,
                                                   Date timestampExpires, CredentialValidationMode validationMode) throws InvalidConfigurationException, CredentialValidationFailedException, InvalidRequestException, EncryptionException {
        final IdGeneratorService idGeneratorService = serviceCatalogue.getIdGeneratorService();
        final CredentialRepository credentialRepository = repositoryCatalogue.getCredentialRepository();
        final CredentialGenerationService credentialGenerationService = serviceCatalogue.getCredentialGenerationService();
        final CredentialValidationService credentialValidationService = serviceCatalogue.getCredentialValidationService();
        final CredentialProtectionService credentialProtectionService = serviceCatalogue.getCredentialProtectionService();
        final CredentialHistoryService credentialHistoryService = serviceCatalogue.getCredentialHistoryService();
        final UserIdentityRepository userIdentityRepository = repositoryCatalogue.getUserIdentityRepository();
        final EndToEndEncryptionService endToEndEncryptionService = serviceCatalogue.getEndToEndEncryptionService();
        // Lookup credential in case it already exists
        final CredentialEntity credential;
        final Optional<CredentialEntity> credentialOptional = user.getCredentials().stream().filter(c -> c.getCredentialDefinition().equals(credentialDefinition)).findFirst();
        final boolean newCredential;
        Date changeTimestamp = new Date();
        if (credentialOptional.isPresent()) {
            audit.info("Credential was reactivated", AuditDetail.builder()
                    .type(AUDIT_TYPE_USER_IDENTITY)
                    .param("userId", user.getUserId())
                    .param("credentialDefinitionName", credentialDefinition.getName())
                    .build());
            credential = credentialOptional.get();
            credential.setTimestampLastUpdated(changeTimestamp);
            credential.setTimestampLastCredentialChange(changeTimestamp);
            newCredential = false;
        } else {
            credential = new CredentialEntity();
            credential.setCredentialId(idGeneratorService.generateCredentialId());
            credential.setCredentialDefinition(credentialDefinition);
            credential.setTimestampCreated(changeTimestamp);
            credential.setUser(user);
            newCredential = true;
        }
        if (username != null) {
            // Username has to be checked for duplicates even when username validation is disabled
            if (validationMode == CredentialValidationMode.NO_VALIDATION || validationMode == CredentialValidationMode.VALIDATE_CREDENTIAL) {
                final Optional<CredentialEntity> existingCredentialOptional = credentialRepository.findByCredentialDefinitionAndUsernameIgnoreCase(credentialDefinition, username);
                if (existingCredentialOptional.isPresent()) {
                    final CredentialEntity existingCredential = existingCredentialOptional.get();
                    if (!existingCredential.getUser().equals(user)) {
                        final CredentialValidationError error = new CredentialValidationError(CredentialValidationFailedException.CODE,
                                "Username validation failed", Collections.singletonList(CredentialValidationFailure.USERNAME_ALREADY_EXISTS));
                        throw new CredentialValidationFailedException("Username validation failed for user ID: " + user.getUserId(), error);
                    }
                }
            }
        } else {
            final boolean useOriginalUsername = nextStepServerConfiguration.isUseOriginalUsername();
            if (useOriginalUsername && credential.getUsername() != null) {
                username = credential.getUsername();
            } else {
                username = credentialGenerationService.generateUsername(credentialDefinition);
            }
        }
        if (username != null) {
            username = username.toLowerCase();
        }
        credential.setType(credentialType);
        if (timestampExpires != null) {
            // Credential expiration is set in the request
            credential.setTimestampExpires(timestampExpires);
        } else {
            // Credential expiration needs to be derived
            updateCredentialExpirationTime(credential, credentialDefinition.getCredentialPolicy());
        }
        credential.setUsername(username);
        final String credentialValueRequest = credentialValue;
        if (credentialValue == null) {
            credentialValue = credentialGenerationService.generateCredentialValue(credentialDefinition);
        }
        final List<CredentialValidationFailure> validationErrors = credentialValidationService.validateCredential(user,
                credentialDefinition, username, credentialValue, validationMode);
        if (!validationErrors.isEmpty()) {
            final CredentialValidationError error = new CredentialValidationError(CredentialValidationFailedException.CODE, "Validation failed", validationErrors);
            throw new CredentialValidationFailedException("Validation failed for user ID: " + user.getUserId(), error);
        }
        final String unprotectedCredentialValue = credentialValue;
        final CredentialValue protectedCredentialValue = credentialProtectionService.protectCredential(credentialValue, credential);
        credential.setValue(protectedCredentialValue.getValue());
        credential.setEncryptionAlgorithm(protectedCredentialValue.getEncryptionAlgorithm());
        credential.setHashingConfig(credentialDefinition.getHashingConfig());
        credential.setTimestampLastCredentialChange(changeTimestamp);
        credential.setTimestampLastUsernameChange(changeTimestamp);
        credential.setStatus(CredentialStatus.ACTIVE);
        credential.setTimestampBlocked(null);
        // Counters are reset even in case of an existing credential
        credential.setAttemptCounter(0);
        credential.setFailedAttemptCounterSoft(0);
        credential.setFailedAttemptCounterHard(0);
        if (newCredential) {
            // Credential needs to be added after all validations, otherwise JPA may save the credential prematurely
            user.getCredentials().add(credential);
        }

        // Save credential into credential history
        credentialHistoryService.createCredentialHistory(user, credential, changeTimestamp);
        user = userIdentityRepository.save(user);
        logger.debug("Credential was created for user ID: {}, credential definition name: {}", user.getUserId(), credentialDefinition.getName());
        audit.info("Credential was created", AuditDetail.builder()
                .type(AUDIT_TYPE_USER_IDENTITY)
                .param("userId", user.getUserId())
                .param("credentialDefinitionName", credentialDefinition.getName())
                .build());
        final CredentialSecretDetail credentialDetail = new CredentialSecretDetail();
        credentialDetail.setCredentialName(credential.getCredentialDefinition().getName());
        credentialDetail.setCredentialType(credential.getType());
        credentialDetail.setCredentialStatus(CredentialStatus.ACTIVE);
        credentialDetail.setUsername(credential.getUsername());
        final boolean credentialChangeRequired;
        if (credentialValueRequest == null) {
            // Generated credential value is returned in unprotected form, with possible e2e-encryption
            credentialChangeRequired = isCredentialChangeRequired(credential, unprotectedCredentialValue);
            String credentialValueResponse = unprotectedCredentialValue;
            if (credentialDefinition.isE2eEncryptionEnabled() &&
                    (credential.getType() == CredentialType.PERMANENT || credentialDefinition.isE2eEncryptionForTemporaryCredentialEnabled())) {
                credentialValueResponse = endToEndEncryptionService.encryptCredential(credentialValueResponse, credentialDefinition);
            }
            credentialDetail.setCredentialValue(credentialValueResponse);
        } else {
            credentialChangeRequired = isCredentialChangeRequired(credential, credentialValueRequest);
        }
        credentialDetail.setCredentialChangeRequired(credentialChangeRequired);
        credentialDetail.setTimestampCreated(credential.getTimestampCreated());
        credentialDetail.setTimestampLastUpdated(credential.getTimestampLastUpdated());
        credentialDetail.setTimestampExpires(credential.getTimestampExpires());
        credentialDetail.setTimestampBlocked(credential.getTimestampBlocked());
        credentialDetail.setTimestampLastCredentialChange(credential.getTimestampLastCredentialChange());
        credentialDetail.setTimestampLastUsernameChange(credential.getTimestampLastUsernameChange());
        return credentialDetail;
    }

    /**
     * Import credential history record.
     * @param user User identity entity.
     * @param credentialDefinition Credential definition.
     * @param username Username.
     * @param credentialValue Credential value.
     * @throws InvalidConfigurationException Thrown when Next Step configuration is invalid.
     * @throws EncryptionException Thrown when encryption fails.
     */
    public void importCredentialHistory(UserIdentityEntity user, CredentialDefinitionEntity credentialDefinition,
                                        String username, String credentialValue, Date createdDate) throws InvalidConfigurationException, EncryptionException {
        final CredentialProtectionService credentialProtectionService = serviceCatalogue.getCredentialProtectionService();
        final CredentialHistoryService credentialHistoryService = serviceCatalogue.getCredentialHistoryService();

        final CredentialEntity credential = new CredentialEntity();
        credential.setUser(user);
        credential.setCredentialDefinition(credentialDefinition);
        credential.setUsername(username);
        final CredentialValue protectedValue = credentialProtectionService.protectCredential(credentialValue, credential);
        credential.setValue(protectedValue.getValue());
        credential.setEncryptionAlgorithm(protectedValue.getEncryptionAlgorithm());
        credential.setHashingConfig(credentialDefinition.getHashingConfig());
        credentialHistoryService.createCredentialHistory(user, credential, createdDate);
        logger.debug("Credential history record was imported for user ID: {}, credential definition name: {}", user.getUserId(), credentialDefinition.getName());
    }

    /**
     * Update credential expiration time in case credential type is TEMPORARY or rotation is enabled.
     * @param credential Credential entity.
     * @param credentialPolicy Credential policy entity.
     */
    private void updateCredentialExpirationTime(CredentialEntity credential, CredentialPolicyEntity credentialPolicy) {
        if (credential.getType() == CredentialType.TEMPORARY) {
            final Integer expirationTime = credentialPolicy.getTemporaryCredentialExpirationTime();
            if (expirationTime != null) {
                final Calendar c = Calendar.getInstance();
                c.add(Calendar.SECOND, expirationTime);
                credential.setTimestampExpires(c.getTime());
                logger.debug("Credential expiration time was updated for user ID: {}, credential definition name: {}, expiration: {}", credential.getUser().getUserId(), credential.getCredentialDefinition().getName(), credential.getTimestampExpires());
                return;
            }
        }
        if (credentialPolicy.isRotationEnabled()) {
            final Calendar c = GregorianCalendar.getInstance();
            c.add(Calendar.DAY_OF_YEAR, credentialPolicy.getRotationDays());
            credential.setTimestampExpires(c.getTime());
            logger.debug("Credential expiration time was updated for user ID: {}, credential definition name: {}, expiration: {}", credential.getUser().getUserId(), credential.getCredentialDefinition().getName(), credential.getTimestampExpires());
            return;
        }
        credential.setTimestampExpires(null);
    }

}