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
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wultra.core.audit.base.Audit;
import com.wultra.core.audit.base.model.AuditDetail;
import io.getlime.security.powerauth.app.nextstep.converter.AuthenticationConverter;
import io.getlime.security.powerauth.app.nextstep.converter.OtpValueConverter;
import io.getlime.security.powerauth.app.nextstep.repository.AuthenticationRepository;
import io.getlime.security.powerauth.app.nextstep.repository.catalogue.RepositoryCatalogue;
import io.getlime.security.powerauth.app.nextstep.repository.model.entity.*;
import io.getlime.security.powerauth.app.nextstep.service.adapter.AuthenticationCustomizationService;
import io.getlime.security.powerauth.app.nextstep.service.catalogue.ServiceCatalogue;
import io.getlime.security.powerauth.lib.dataadapter.model.entity.AuthenticationContext;
import io.getlime.security.powerauth.lib.dataadapter.model.enumeration.PasswordProtectionType;
import io.getlime.security.powerauth.lib.nextstep.model.entity.AuthStep;
import io.getlime.security.powerauth.lib.nextstep.model.entity.AuthenticationDetail;
import io.getlime.security.powerauth.lib.nextstep.model.entity.OtpValue;
import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.*;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthInstrument;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthMethod;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthResult;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthStepResult;
import io.getlime.security.powerauth.lib.nextstep.model.exception.*;
import io.getlime.security.powerauth.lib.nextstep.model.request.*;
import io.getlime.security.powerauth.lib.nextstep.model.response.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.*;
import java.util.stream.Stream;

/**
 * This service handles user authentication.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Service
public class AuthenticationService {

    private final Logger logger = LoggerFactory.getLogger(AuthenticationService.class);
    private static final String AUDIT_TYPE_AUTHENTICATION = "AUTHENTICATION";

    private final AuthenticationRepository authenticationRepository;
    private final ServiceCatalogue serviceCatalogue;
    private final OtpValueConverter otpValueConverter;
    private final Audit audit;

    private final AuthenticationConverter authenticationConverter = new AuthenticationConverter();

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Authentication service constructor.
     * @param repositoryCatalogue Repository catalogue.
     * @param serviceCatalogue Service catalogue.
     * @param otpValueConverter OTP value converter.
     * @param audit Audit audit.
     */
    @Autowired
    public AuthenticationService(RepositoryCatalogue repositoryCatalogue, @Lazy ServiceCatalogue serviceCatalogue, OtpValueConverter otpValueConverter, Audit audit) {
        this.authenticationRepository = repositoryCatalogue.getAuthenticationRepository();
        this.serviceCatalogue = serviceCatalogue;
        this.otpValueConverter = otpValueConverter;
        this.audit = audit;
    }

    /**
     * Authenticate user with a credential.
     *
     * @param request Credential authentication request.
     * @return Credential authentication response.
     * @throws CredentialDefinitionNotFoundException Thrown when credential definition is not found.
     * @throws UserNotFoundException Thrown when user identity is not found.
     * @throws OperationNotFoundException Thrown when operation is not found.
     * @throws InvalidRequestException Thrown when requests is invalid.
     * @throws CredentialNotFoundException Thrown when credential is not found.
     * @throws OperationAlreadyFinishedException Thrown when operation is already finished.
     * @throws OperationAlreadyCanceledException Thrown when operation is already canceled.
     * @throws OperationAlreadyFailedException Thrown when operation is already failed.
     * @throws OperationNotValidException Thrown when operation is not valid.
     * @throws AuthMethodNotFoundException Thrown when authentication method is not found.
     * @throws InvalidConfigurationException Thrown when Next Step configuration is invalid.
     * @throws EncryptionException Thrown when decryption fails.
     */
    @Transactional
    public CredentialAuthenticationResponse authenticateWithCredential(CredentialAuthenticationRequest request) throws CredentialDefinitionNotFoundException, UserNotFoundException, OperationNotFoundException, InvalidRequestException, CredentialNotFoundException, OperationAlreadyFinishedException, OperationAlreadyCanceledException, AuthMethodNotFoundException, OperationAlreadyFailedException, InvalidConfigurationException, OperationNotValidException, EncryptionException {
        final CredentialDefinitionService credentialDefinitionService = serviceCatalogue.getCredentialDefinitionService();
        final EndToEndEncryptionService endToEndEncryptionService = serviceCatalogue.getEndToEndEncryptionService();
        final UserIdentityLookupService userIdentityLookupService = serviceCatalogue.getUserIdentityLookupService();
        final OperationPersistenceService operationPersistenceService = serviceCatalogue.getOperationPersistenceService();
        final CredentialService credentialService = serviceCatalogue.getCredentialService();
        final CredentialCounterService credentialCounterService = serviceCatalogue.getCredentialCounterService();
        final IdGeneratorService idGeneratorService = serviceCatalogue.getIdGeneratorService();

        final CredentialDefinitionEntity credentialDefinition = credentialDefinitionService.findActiveCredentialDefinition(request.getCredentialName());
        if (credentialDefinition.isDataAdapterProxyEnabled()) {
            logger.info("Credential authentication proxied through Data Adapter, credential definition name: {}", credentialDefinition.getName());
            return authenticateWithCredentialCustom(credentialDefinition, request.getCredentialValue(), request.getOperationId(), request.getUserId(), request.getAuthMethod());
        }
        String credentialValue = request.getCredentialValue();
        if (credentialDefinition.isE2eEncryptionEnabled()) {
            credentialValue = endToEndEncryptionService.decryptCredential(credentialValue, credentialDefinition);
        }
        final UserIdentityEntity user = userIdentityLookupService.findUser(request.getUserId());
        if (user.getStatus() != UserIdentityStatus.ACTIVE) {
            final CredentialAuthenticationResponse response = new CredentialAuthenticationResponse();
            response.setUserId(user.getUserId());
            response.setUserIdentityStatus(user.getStatus());
            response.setAuthenticationResult(AuthenticationResult.FAILED);
            response.setRemainingAttempts(0);
            logger.debug("User authentication failed because user identity status is: {}", user.getStatus());
            return response;
        }
        OperationEntity operation = null;
        if (request.getOperationId() != null) {
            operation = operationPersistenceService.getOperation(request.getOperationId());
        }
        if (request.isUpdateOperation() && operation == null) {
            throw new InvalidRequestException("Operation not found, however operation update requested for credential: " + request.getCredentialName());
        }
        final CredentialEntity credential = credentialService.findCredential(credentialDefinition, user);
        credential.setAttemptCounter(credential.getAttemptCounter() + 1);

        // Verify credential value
        final AuthenticationResult authenticationResult;
        if (credential.getStatus() == CredentialStatus.ACTIVE) {
            authenticationResult = verifyCredential(request.getAuthenticationMode(), credential, credentialValue, request.getCredentialPositionsToVerify());
            logger.debug("User authentication result: {}, authentication mode: {}", authenticationResult, request.getAuthenticationMode());
        } else {
            logger.debug("User authentication failed because user credential status is: {}", credential.getStatus());
            authenticationResult = AuthenticationResult.FAILED;
        }

        // Update counters based on authentication result
        credentialCounterService.updateCredentialCounter(credential, authenticationResult);

        AuthenticationEntity authentication = new AuthenticationEntity();
        authentication.setAuthenticationId(idGeneratorService.generateAuthenticationId());
        authentication.setUserId(user.getUserId());
        authentication.setAuthenticationType(AuthenticationType.CREDENTIAL);
        authentication.setCredential(credential);
        if (operation != null) {
            authentication.setOperation(operation);
        }
        authentication.setResult(authenticationResult);
        authentication.setResultCredential(authenticationResult);
        authentication.setTimestampCreated(new Date());
        // Authentication needs to be saved before calling updateOperation
        authentication = authenticationRepository.save(authentication);

        final boolean lastAttempt = user.getStatus() != UserIdentityStatus.ACTIVE ||
                credential.getStatus() != CredentialStatus.ACTIVE;

        boolean operationFailed = false;
        if (request.isUpdateOperation() && operation != null) {
            final UpdateOperationResponse operationResponse = updateOperation(user.getUserId(), operation,
                    request.getAuthMethod(), authenticationResult, authentication.getAuthenticationId(),
                    lastAttempt, Collections.singletonList(AuthInstrument.CREDENTIAL));
            if (operationResponse == null || operationResponse.getResult() == AuthResult.FAILED) {
                logger.debug("Operation update result is FAILED, operation ID: {}", operation.getOperationId());
                operationFailed = true;
                authentication.setResultCredential(AuthenticationResult.FAILED);
                authentication.setResult(AuthenticationResult.FAILED);
                // Authentication was updated, save it
                authenticationRepository.save(authentication);
            }
        }

        final Integer remainingAttempts = resolveRemainingAttempts(credential, null, operation);

        logger.info("Credential authentication result: {}, remaining attempts: {}, user ID: {}, user identity status: {}, credential status: {}, operation failed: {}",
                authenticationResult, remainingAttempts, user.getUserId(), user.getStatus(), credential.getStatus(), operationFailed);
        audit.info("Credential authentication result", AuditDetail.builder()
                .type(AUDIT_TYPE_AUTHENTICATION)
                .param("userId", user.getUserId())
                .param("operationId", operation != null ? operation.getOperationId() : null)
                .param("authenticationResult", authenticationResult)
                .build());
        audit.debug("Credential authentication result (detail)", AuditDetail.builder()
                .type(AUDIT_TYPE_AUTHENTICATION)
                .param("userId", user.getUserId())
                .param("operationId", operation != null ? operation.getOperationId() : null)
                .param("authenticationResult", authenticationResult)
                .param("remainingAttempts", remainingAttempts)
                .param("userStatus", user.getStatus())
                .param("credentialStatus", credential.getStatus())
                .param("operationFailed", operationFailed)
                .build());
        final CredentialAuthenticationResponse response = new CredentialAuthenticationResponse();
        response.setUserId(user.getUserId());
        response.setUserIdentityStatus(user.getStatus());
        response.setTimestampBlocked(credential.getTimestampBlocked());
        response.setCredentialStatus(credential.getStatus());
        response.setAuthenticationResult(authenticationResult);
        response.setRemainingAttempts(remainingAttempts);
        response.setOperationFailed(operationFailed);
        response.setCredentialChangeRequired(credentialService.isCredentialChangeRequired(credential, credentialValue));
        return response;
    }

    /**
     * Perform custom authentication with credential.
     *
     * @param credentialDefinition Credential definition.
     * @param credentialValue Credential value.
     * @param operationId Operation ID.
     * @param userId User ID.
     * @param authMethod Authentication method.
     * @return Credential authentication response.
     * @throws OperationNotFoundException Thrown when operation is not found.
     * @throws InvalidRequestException Thrown when requests is invalid.
     * @throws OperationAlreadyFinishedException Thrown when operation is already finished.
     * @throws OperationAlreadyCanceledException Thrown when operation is already canceled.
     * @throws OperationAlreadyFailedException Thrown when operation is already failed.
     * @throws OperationNotValidException Thrown when operation is not valid.
     * @throws AuthMethodNotFoundException Thrown when authentication method is not found.
     * @throws InvalidConfigurationException Thrown when Next Step configuration is invalid.
     */
    private CredentialAuthenticationResponse authenticateWithCredentialCustom(CredentialDefinitionEntity credentialDefinition, String credentialValue,
                                                                              String operationId, String userId, AuthMethod authMethod) throws InvalidRequestException, OperationNotFoundException, InvalidConfigurationException, OperationAlreadyFinishedException, OperationAlreadyFailedException, OperationNotValidException, AuthMethodNotFoundException, OperationAlreadyCanceledException {
        final OperationPersistenceService operationPersistenceService = serviceCatalogue.getOperationPersistenceService();
        final AuthenticationCustomizationService authenticationCustomizationService = serviceCatalogue.getAuthenticationCustomizationService();
        if (operationId == null) {
            throw new InvalidRequestException("Operation ID is missing in Data Adapter authentication with credential request");
        }
        final OperationEntity operation = operationPersistenceService.getOperation(operationId);
        final String organizationId = operation.getOrganization() != null ? operation.getOrganization().getOrganizationId() : null;
        final AuthenticationContext authenticationContext = new AuthenticationContext();
        if (credentialDefinition.isE2eEncryptionEnabled() && credentialDefinition.getE2eEncryptionAlgorithm() == EndToEndEncryptionAlgorithm.AES) {
            authenticationContext.setPasswordProtection(PasswordProtectionType.PASSWORD_ENCRYPTION_AES);
            authenticationContext.setCipherTransformation(credentialDefinition.getE2eEncryptionCipherTransformation());
        } else {
            authenticationContext.setPasswordProtection(PasswordProtectionType.NO_PROTECTION);
        }
        final CredentialAuthenticationResponse response = authenticationCustomizationService.authenticateWithCredential(userId, organizationId, credentialValue, operation, authenticationContext);
        final boolean lastAttempt = response.getUserIdentityStatus() != UserIdentityStatus.ACTIVE ||
                (response.getRemainingAttempts() != null && response.getRemainingAttempts() == 0);
        boolean operationFailed = false;
        final UpdateOperationResponse operationResponse = updateOperation(userId, operation, authMethod, response.getAuthenticationResult(), null, lastAttempt, Collections.singletonList(AuthInstrument.CREDENTIAL));
        if (operationResponse == null || operationResponse.getResult() == AuthResult.FAILED) {
            logger.debug("Operation update result is FAILED, operation ID: {}", operation.getOperationId());
            operationFailed = true;
        }
        response.setOperationFailed(operationFailed);
        logger.info("Credential custom authentication result: {}, remaining attempts: {}, user ID: {}, operation failed: {}",
                response.getAuthenticationResult(), response.getRemainingAttempts(), userId, operationFailed);
        audit.info("Credential custom authentication result", AuditDetail.builder()
                .type(AUDIT_TYPE_AUTHENTICATION)
                .param("userId", userId)
                .param("operationId", operation.getOperationId())
                .param("authenticationResult", response.getAuthenticationResult())
                .build());
        audit.debug("Credential custom authentication result (detail)", AuditDetail.builder()
                .type(AUDIT_TYPE_AUTHENTICATION)
                .param("userId", userId)
                .param("operationId", operation.getOperationId())
                .param("authenticationResult", response.getAuthenticationResult())
                .param("remainingAttempts", response.getRemainingAttempts())
                .param("operationFailed", operationFailed)
                .build());
        return response;
    }

    /**
     * Authenticate with OTP.
     *
     * @param request Authentication with OTP request.
     * @return Authentication with OTP response.
     * @throws OtpNotFoundException Thrown when OTP is not found.
     * @throws OperationNotFoundException Thrown when operation is not found.
     * @throws InvalidRequestException Thrown when request is invalid.
     * @throws CredentialNotFoundException Thrown when credential is not found.
     * @throws OperationAlreadyCanceledException Thrown when operation is already canceled.
     * @throws OperationAlreadyFinishedException Thrown when operation is already finished.
     * @throws OperationAlreadyFailedException Thrown when operation is already failed.
     * @throws OperationNotValidException Thrown when operation is not valid.
     * @throws InvalidConfigurationException Thrown when Next Step configuration is invalid.
     * @throws AuthMethodNotFoundException Thrown when authentication method is not found.
     * @throws EncryptionException Thrown when decryption fails.
     */
    @Transactional
    public OtpAuthenticationResponse authenticateWithOtp(OtpAuthenticationRequest request) throws OtpNotFoundException, OperationNotFoundException, InvalidRequestException, CredentialNotFoundException, OperationAlreadyCanceledException, OperationAlreadyFinishedException, InvalidConfigurationException, AuthMethodNotFoundException, OperationAlreadyFailedException, OperationNotValidException, EncryptionException {
        final OtpService otpService = serviceCatalogue.getOtpService();
        final UserIdentityLookupService userIdentityLookupService = serviceCatalogue.getUserIdentityLookupService();
        final OperationPersistenceService operationPersistenceService = serviceCatalogue.getOperationPersistenceService();
        final CredentialService credentialService = serviceCatalogue.getCredentialService();
        final CredentialCounterService credentialCounterService = serviceCatalogue.getCredentialCounterService();
        final IdGeneratorService idGeneratorService = serviceCatalogue.getIdGeneratorService();

        final OtpEntity otp = otpService.findOtp(request.getOtpId(), request.getOperationId());
        if (otp.getOtpDefinition().isDataAdapterProxyEnabled()) {
            logger.info("OTP authentication proxied through Data Adapter, OTP ID: {}", otp.getOtpId());
            return authenticateWithOtpCustom(otp.getOtpDefinition(), otp.getOtpId(), request.getOtpValue(), otp.getOperation().getOperationId(), otp.getUserId(), request.getAuthMethod());
        }
        otp.setAttemptCounter(otp.getAttemptCounter() + 1);
        // User ID uses String reference in entity to allow OTP for users not present in Next Step.
        // Empty user ID is allowed for unknown identities.
        final String userId = otp.getUserId();
        UserIdentityEntity user = null;
        if (userId != null) {
            final Optional<UserIdentityEntity> userOptional = userIdentityLookupService.findUserOptional(userId);
            if (userOptional.isPresent()) {
                user = userOptional.get();
                if (user.getStatus() != UserIdentityStatus.ACTIVE) {
                    logger.debug("OTP authentication failed because user identity status is: {}, OTP ID: {}", user.getStatus(), otp.getOtpId());
                    final OtpAuthenticationResponse response = new OtpAuthenticationResponse();
                    response.setUserId(userId);
                    response.setUserIdentityStatus(user.getStatus());
                    response.setAuthenticationResult(AuthenticationResult.FAILED);
                    response.setRemainingAttempts(0);
                    response.setOtpStatus(otp.getStatus());
                    return response;
                }
            }
        }
        OperationEntity operation = null;
        if (request.getOperationId() != null) {
            operation = operationPersistenceService.getOperation(request.getOperationId());
        }
        if (request.isUpdateOperation() && operation == null) {
            throw new InvalidRequestException("Operation not found, however operation update requested for OTP name: " + otp.getOtpDefinition().getName());
        }

        final AuthenticationResult authenticationResult;
        CredentialEntity credential = null;
        boolean credentialActive = true;

        if (otp.getCredentialDefinition() != null && user != null) {
            credential = credentialService.findCredential(otp.getCredentialDefinition(), user);
            if (credential.getStatus() != CredentialStatus.ACTIVE) {
                logger.debug("Credential is not active for OTP ID: {}, status: {}", otp.getOtpId(), credential.getStatus());
                credentialActive = false;
            }
        }

        if (!credentialActive) {
            logger.debug("OTP authentication failed because credential status is not ACTIVE, OTP ID: {}", otp.getOtpId());
            // Fail authentication and block OTP in case credential is not active
            authenticationResult = AuthenticationResult.FAILED;
            otp.setStatus(OtpStatus.BLOCKED);
            otp.setTimestampBlocked(new Date());
        } else {
            if (otp.getStatus() == OtpStatus.ACTIVE) {
                authenticationResult = verifyOtp(otp, request.getOtpValue());
            } else {
                authenticationResult = AuthenticationResult.FAILED;
            }

            if (authenticationResult == AuthenticationResult.SUCCEEDED) {
                otp.setStatus(OtpStatus.USED);
                otp.setTimestampVerified(new Date());
                logger.debug("OTP was successfully verified, OTP ID: {}", otp.getOtpId());
            }
            if (credential != null) {
                // Update counters based on authentication result in case credential related to OTP is available
                credential.setAttemptCounter(credential.getAttemptCounter() + 1);
                credentialCounterService.updateCredentialCounter(credential, authenticationResult);
            }
        }

        AuthenticationEntity authentication = new AuthenticationEntity();
        authentication.setAuthenticationId(idGeneratorService.generateAuthenticationId());
        authentication.setUserId(userId);
        authentication.setAuthenticationType(AuthenticationType.OTP);
        authentication.setOtp(otp);
        if (credential != null) {
            authentication.setCredential(credential);
        }
        if (operation != null) {
            authentication.setOperation(operation);
        }
        authentication.setResult(authenticationResult);
        authentication.setResultOtp(authenticationResult);
        authentication.setTimestampCreated(new Date());
        // Authentication needs to be saved before calling updateOperation
        authentication = authenticationRepository.save(authentication);

        final boolean lastAttempt = (user != null && user.getStatus() != UserIdentityStatus.ACTIVE)
                || (credential != null && credential.getStatus() != CredentialStatus.ACTIVE)
                || otp.getStatus() != OtpStatus.ACTIVE;

        boolean operationFailed = false;
        if (request.isUpdateOperation() && operation != null) {
            final UpdateOperationResponse operationResponse = updateOperation(userId, operation, request.getAuthMethod(),
                    authenticationResult, authentication.getAuthenticationId(), lastAttempt, Collections.singletonList(AuthInstrument.OTP_KEY));
            if (operationResponse == null || operationResponse.getResult() == AuthResult.FAILED) {
                logger.debug("Operation update result is FAILED, operation ID: {}", operation.getOperationId());
                operationFailed = true;
                authentication.setResultOtp(AuthenticationResult.FAILED);
                authentication.setResult(AuthenticationResult.FAILED);
                // Authentication was updated, save it
                authentication = authenticationRepository.save(authentication);
            }
        }

        final Integer remainingAttempts = resolveRemainingAttempts(credential, otp, operation);
        if (remainingAttempts == 0 && otp.getStatus() == OtpStatus.ACTIVE) {
            logger.debug("OTP was blocked because there are no remaining attempts left, OTP ID: {}", otp.getOtpId());
            otp.setStatus(OtpStatus.BLOCKED);
            otp.setTimestampBlocked(new Date());
            // OTP was updated, save authentication with OTP
            authenticationRepository.save(authentication);
        }

        logger.info("OTP authentication result: {}, remaining attempts: {}, user ID: {}, user identity status: {}, OTP status: {}, credential status: {}, operation failed: {}",
                authenticationResult, remainingAttempts, user == null ? null : user.getUserId(), user == null ? null : user.getStatus(), otp.getStatus(), credential == null ? null : credential.getStatus(), operationFailed);
        audit.info("OTP authentication result", AuditDetail.builder()
                .type(AUDIT_TYPE_AUTHENTICATION)
                .param("userId", user != null ? user.getUserId() : null)
                .param("otpId", otp.getOtpId())
                .param("operationId", operation != null ? operation.getOperationId() : null)
                .param("authenticationResult", authenticationResult)
                .build());
        audit.debug("OTP authentication result (detail)", AuditDetail.builder()
                .type(AUDIT_TYPE_AUTHENTICATION)
                .param("userId", user != null ? user.getUserId() : null)
                .param("otpId", otp.getOtpId())
                .param("operationId", operation != null ? operation.getOperationId() : null)
                .param("authenticationResult", authenticationResult)
                .param("remainingAttempts", remainingAttempts)
                .param("userStatus", user != null ? user.getStatus() : null)
                .param("otpStatus", otp.getStatus())
                .param("credentialStatus", credential != null ? credential.getStatus() : null)
                .param("operationFailed", operationFailed)
                .build());
        final OtpAuthenticationResponse response = new OtpAuthenticationResponse();
        if (userId != null) {
            response.setUserId(userId);
        }
        if (user != null) {
            response.setUserIdentityStatus(user.getStatus());
        }
        response.setAuthenticationResult(authenticationResult);
        response.setRemainingAttempts(remainingAttempts);
        response.setOtpStatus(otp.getStatus());
        if (credential != null) {
            response.setCredentialStatus(credential.getStatus());
            response.setTimestampBlocked(credential.getTimestampBlocked());
        }
        response.setOperationFailed(operationFailed);
        return response;
    }

    /**
     * Perform custom authentication with OTP.
     * @param otpDefinition OTP definition.
     * @param otpId OTP ID.
     * @param otpValue OTP value.
     * @param operationId Operation ID.
     * @param userId User ID.
     * @param authMethod Authentication method.
     */
    private OtpAuthenticationResponse authenticateWithOtpCustom(OtpDefinitionEntity otpDefinition, String otpId, String otpValue,
                                                                String operationId, String userId, AuthMethod authMethod) throws InvalidRequestException, OperationNotFoundException, InvalidConfigurationException, OperationAlreadyFinishedException, OperationAlreadyFailedException, OperationNotValidException, AuthMethodNotFoundException, OperationAlreadyCanceledException {
        final OperationPersistenceService operationPersistenceService = serviceCatalogue.getOperationPersistenceService();
        final AuthenticationCustomizationService authenticationCustomizationService = serviceCatalogue.getAuthenticationCustomizationService();
        if (operationId == null) {
            throw new InvalidRequestException("Operation ID is missing in Data Adapter authentication with credential request");
        }
        final OperationEntity operation = operationPersistenceService.getOperation(operationId);
        final String organizationId = operation.getOrganization() != null ? operation.getOrganization().getOrganizationId() : null;
        final OtpAuthenticationResponse response = authenticationCustomizationService.authenticateWithOtp(otpId, otpValue, userId, organizationId, operation);
        final boolean lastAttempt = response.getUserIdentityStatus() != UserIdentityStatus.ACTIVE ||
                (response.getRemainingAttempts() != null && response.getRemainingAttempts() == 0);
        boolean operationFailed = false;
        final UpdateOperationResponse operationResponse = updateOperation(userId, operation, authMethod, response.getAuthenticationResult(),
                null, lastAttempt, Collections.singletonList(AuthInstrument.OTP_KEY));
        if (operationResponse == null || operationResponse.getResult() == AuthResult.FAILED) {
            logger.debug("Operation update result is FAILED, operation ID: {}", operation.getOperationId());
            operationFailed = true;
        }
        response.setOperationFailed(operationFailed);
        logger.info("OTP custom authentication result: {}, OTP ID: {}, remaining attempts: {}, user ID: {}, operation failed: {}",
                response.getAuthenticationResult(), otpId, response.getRemainingAttempts(), userId, operationFailed);
        audit.info("OTP custom authentication result", AuditDetail.builder()
                .type(AUDIT_TYPE_AUTHENTICATION)
                .param("otpId", otpId)
                .param("userId", userId)
                .param("operationId", operation.getOperationId())
                .param("authenticationResult", response.getAuthenticationResult())
                .build());
        audit.debug("OTP custom authentication result (detail)", AuditDetail.builder()
                .type(AUDIT_TYPE_AUTHENTICATION)
                .param("otpId", otpId)
                .param("userId", userId)
                .param("operationId", operation.getOperationId())
                .param("authenticationResult", response.getAuthenticationResult())
                .param("remainingAttempts", response.getRemainingAttempts())
                .param("operationFailed", operationFailed)
                .build());
        return response;
    }

    /**
     * Combined authentication with credential and OTP.
     * @param request Combined authentication request.
     * @return Combined authentication response.
     * @throws UserNotFoundException Thrown when user is not found.
     * @throws OperationNotFoundException Thrown when operation is not found.
     * @throws InvalidRequestException Thrown when request is invalid.
     * @throws CredentialNotFoundException Thrown when credential is not found.
     * @throws OtpNotFoundException Thrown when OTP is not found.
     * @throws OperationAlreadyCanceledException Thrown when operation is already canceled.
     * @throws OperationAlreadyFinishedException Thrown when operation is already finished.
     * @throws OperationAlreadyFailedException Thrown when operation is already failed.
     * @throws OperationNotValidException Thrown when operation is not valid.
     * @throws InvalidConfigurationException Thrown when Next Step configuration is invalid.
     * @throws AuthMethodNotFoundException Thrown when authentication method is not found.
     * @throws EncryptionException Thrown when decryption fails.
     */
    @Transactional
    public CombinedAuthenticationResponse authenticateCombined(CombinedAuthenticationRequest request) throws UserNotFoundException, OperationNotFoundException, InvalidRequestException, CredentialNotFoundException, OtpNotFoundException, OperationAlreadyCanceledException, OperationAlreadyFinishedException, InvalidConfigurationException, AuthMethodNotFoundException, OperationAlreadyFailedException, OperationNotValidException, EncryptionException {
        final OtpService otpService = serviceCatalogue.getOtpService();
        final EndToEndEncryptionService endToEndEncryptionService = serviceCatalogue.getEndToEndEncryptionService();
        final UserIdentityLookupService userIdentityLookupService = serviceCatalogue.getUserIdentityLookupService();
        final OperationPersistenceService operationPersistenceService = serviceCatalogue.getOperationPersistenceService();
        final CredentialService credentialService = serviceCatalogue.getCredentialService();
        final CredentialCounterService credentialCounterService = serviceCatalogue.getCredentialCounterService();
        final IdGeneratorService idGeneratorService = serviceCatalogue.getIdGeneratorService();

        final OtpEntity otp = otpService.findOtp(request.getOtpId(), request.getOperationId());
        if (otp.getOtpDefinition().isDataAdapterProxyEnabled()) {
            logger.info("Combined authentication proxied through Data Adapter, OTP ID: {}", request.getOtpId());
            return authenticateCombinedCustom(otp.getCredentialDefinition(), otp.getOtpId(), request.getOtpValue(), request.getCredentialValue(), otp.getOperation().getOperationId(), otp.getUserId(), request.getAuthMethod());
        }
        otp.setAttemptCounter(otp.getAttemptCounter() + 1);

        final UserIdentityEntity user = userIdentityLookupService.findUser(request.getUserId());
        if (user.getStatus() != UserIdentityStatus.ACTIVE) {
            final CombinedAuthenticationResponse response = new CombinedAuthenticationResponse();
            response.setUserId(user.getUserId());
            response.setUserIdentityStatus(user.getStatus());
            response.setAuthenticationResult(AuthenticationResult.FAILED);
            response.setCredentialAuthenticationResult(AuthenticationResult.FAILED);
            response.setOtpAuthenticationResult(AuthenticationResult.FAILED);
            response.setRemainingAttempts(0);
            logger.debug("Combined authentication failed because user identity status is: {}", user.getStatus());
            return response;
        }
        if (!user.getUserId().equals(otp.getUserId())) {
            throw new InvalidRequestException("Invalid user ID for OTP: " + otp.getOtpId());
        }
        OperationEntity operation = null;
        if (request.getOperationId() != null) {
            operation = operationPersistenceService.getOperation(request.getOperationId());
        }
        if (request.isUpdateOperation() && operation == null) {
            throw new InvalidRequestException("Operation not found, however operation update requested for credential and OTP: " + request.getCredentialName() + ", " + otp.getOtpDefinition().getName());
        }
        if (request.getOperationId() != null && !request.getOperationId().equals(otp.getOperation().getOperationId())) {
            throw new InvalidRequestException("Operation ID mismatch for credential and OTP: " + request.getCredentialName() + ", " + otp.getOtpDefinition().getName());
        }
        final CredentialEntity credential = credentialService.findCredential(otp.getCredentialDefinition(), user);
        final CredentialDefinitionEntity credentialDefinition = credential.getCredentialDefinition();
        String credentialValue = request.getCredentialValue();
        if (credentialDefinition.isE2eEncryptionEnabled()) {
            credentialValue = endToEndEncryptionService.decryptCredential(credentialValue, credentialDefinition);
        }
        credential.setAttemptCounter(credential.getAttemptCounter() + 1);
        final AuthenticationResult credentialAuthenticationResult;
        final AuthenticationResult otpAuthenticationResult;
        final AuthenticationResult authenticationResult;
        if (credential.getStatus() != CredentialStatus.ACTIVE) {
            // Fail authentication and block OTP in case credential is not active
            authenticationResult = AuthenticationResult.FAILED;
            credentialAuthenticationResult = AuthenticationResult.FAILED;
            otpAuthenticationResult = AuthenticationResult.FAILED;
            otp.setStatus(OtpStatus.BLOCKED);
            otp.setTimestampBlocked(new Date());
            logger.debug("Combined authentication failed because user credential status is: {}", credential.getStatus());
        } else {
            // Verify OTP value
            if (otp.getStatus() == OtpStatus.ACTIVE) {
                credentialAuthenticationResult = verifyCredential(request.getAuthenticationMode(), credential, credentialValue, request.getCredentialPositionsToVerify());
                logger.debug("User authentication result: {}, authentication mode: {}", credentialAuthenticationResult, request.getAuthenticationMode());
                otpAuthenticationResult = verifyOtp(otp, request.getOtpValue());
                logger.debug("OTP verification result: {}, OTP ID: {}", otpAuthenticationResult, otp.getOtpId());
            } else {
                credentialAuthenticationResult = AuthenticationResult.FAILED;
                otpAuthenticationResult = AuthenticationResult.FAILED;
                logger.debug("Combined authentication failed because OTP status is: {}", otp.getStatus());
            }

            // Set overall authentication result
            if (credentialAuthenticationResult == AuthenticationResult.SUCCEEDED && otpAuthenticationResult == AuthenticationResult.SUCCEEDED) {
                authenticationResult = AuthenticationResult.SUCCEEDED;
            } else {
                authenticationResult = AuthenticationResult.FAILED;
            }

            if (authenticationResult == AuthenticationResult.SUCCEEDED) {
                logger.debug("OTP and credential were successfully verified, OTP ID: {}", otp.getOtpId());
                otp.setStatus(OtpStatus.USED);
                otp.setTimestampVerified(new Date());
            }

            // Update counters based on authentication result
            credentialCounterService.updateCredentialCounter(credential, authenticationResult);
        }

        AuthenticationEntity authentication = new AuthenticationEntity();
        authentication.setAuthenticationId(idGeneratorService.generateAuthenticationId());
        authentication.setUserId(user.getUserId());
        authentication.setAuthenticationType(AuthenticationType.CREDENTIAL_OTP);
        authentication.setCredential(credential);
        authentication.setOtp(otp);
        if (operation != null) {
            authentication.setOperation(operation);
        }
        authentication.setResult(authenticationResult);
        authentication.setResultCredential(credentialAuthenticationResult);
        authentication.setResultOtp(otpAuthenticationResult);
        authentication.setTimestampCreated(new Date());
        // Authentication needs to be saved before calling updateOperation
        authentication = authenticationRepository.save(authentication);

        final boolean lastAttempt = user.getStatus() != UserIdentityStatus.ACTIVE
                || credential.getStatus() != CredentialStatus.ACTIVE
                || otp.getStatus() != OtpStatus.ACTIVE;

        boolean operationFailed = false;
        if (request.isUpdateOperation() && operation != null) {
            final UpdateOperationResponse operationResponse = updateOperation(user.getUserId(), operation, request.getAuthMethod(),
                    authenticationResult, authentication.getAuthenticationId(), lastAttempt,
                    Arrays.asList(AuthInstrument.CREDENTIAL, AuthInstrument.OTP_KEY));
            if (operationResponse == null || operationResponse.getResult() == AuthResult.FAILED) {
                logger.debug("Operation update result is FAILED, operation ID: {}", operation.getOperationId());
                operationFailed = true;
                authentication.setResult(AuthenticationResult.FAILED);
                authentication.setResultOtp(AuthenticationResult.FAILED);
                authentication.setResult(AuthenticationResult.FAILED);
                // Authentication was updated, save it
                authentication = authenticationRepository.save(authentication);
            }
        }

        final Integer remainingAttempts = resolveRemainingAttempts(credential, otp, operation);
        if (remainingAttempts == 0 && otp.getStatus() == OtpStatus.ACTIVE) {
            logger.debug("OTP was blocked because there are no remaining attempts left, OTP ID: {}", otp.getOtpId());
            otp.setStatus(OtpStatus.BLOCKED);
            otp.setTimestampBlocked(new Date());
            // OTP was updated, save authentication with OTP
            authenticationRepository.save(authentication);
        }

        logger.info("Combined authentication result: {}, credential authentication result: {}, OTP authentication result: {}, remaining attempts: {}, user ID: {}, user identity status: {}, OTP status: {}, credential status: {}, operation failed: {}",
                authenticationResult, credentialAuthenticationResult, otpAuthenticationResult, remainingAttempts, user.getUserId(), user.getStatus(), otp.getStatus(), credential.getStatus(), operationFailed);
        audit.info("Combined authentication result", AuditDetail.builder()
                .type(AUDIT_TYPE_AUTHENTICATION)
                .param("userId", user.getUserId())
                .param("otpId", otp.getOtpId())
                .param("operationId", operation != null ? operation.getOperationId() : null)
                .param("authenticationResult", authenticationResult)
                .build());
        audit.debug("Combined authentication result (detail)", AuditDetail.builder()
                .type(AUDIT_TYPE_AUTHENTICATION)
                .param("userId", user.getUserId())
                .param("otpId", otp.getOtpId())
                .param("operationId", operation != null ? operation.getOperationId() : null)
                .param("authenticationResult", authenticationResult)
                .param("credentialAuthenticationResult", credentialAuthenticationResult)
                .param("otpAuthenticationResult", otpAuthenticationResult)
                .param("remainingAttempts", remainingAttempts)
                .param("userStatus", user.getStatus())
                .param("otpStatus", otp.getStatus())
                .param("credentialStatus", credential.getStatus())
                .param("operationFailed", operationFailed)
                .build());
        final CombinedAuthenticationResponse response = new CombinedAuthenticationResponse();
        response.setUserId(user.getUserId());
        response.setUserIdentityStatus(user.getStatus());
        response.setTimestampBlocked(credential.getTimestampBlocked());
        response.setCredentialStatus(credential.getStatus());
        response.setOtpStatus(otp.getStatus());
        response.setAuthenticationResult(authenticationResult);
        response.setCredentialAuthenticationResult(credentialAuthenticationResult);
        response.setOtpAuthenticationResult(otpAuthenticationResult);
        response.setRemainingAttempts(remainingAttempts);
        response.setOperationFailed(operationFailed);
        response.setCredentialChangeRequired(credentialService.isCredentialChangeRequired(credential, credentialValue));
        return response;
    }

    /**
     * Perform custom authentication with OTP and credential.
     * @param otpId OTP ID.
     * @param otpValue OTP value.
     * @param operationId Operation ID.
     * @param userId User ID.
     * @param authMethod Authentication method.
     */
    private CombinedAuthenticationResponse authenticateCombinedCustom(CredentialDefinitionEntity credentialDefinition, String otpId, String otpValue, String credentialValue,
                                                                    String operationId, String userId, AuthMethod authMethod) throws InvalidRequestException, OperationNotFoundException, InvalidConfigurationException, OperationAlreadyFinishedException, OperationAlreadyFailedException, OperationNotValidException, AuthMethodNotFoundException, OperationAlreadyCanceledException {
        final OperationPersistenceService operationPersistenceService = serviceCatalogue.getOperationPersistenceService();
        final AuthenticationCustomizationService authenticationCustomizationService = serviceCatalogue.getAuthenticationCustomizationService();
        if (operationId == null) {
            throw new InvalidRequestException("Operation ID is missing in Data Adapter authentication with credential request");
        }
        final OperationEntity operation = operationPersistenceService.getOperation(operationId);
        final String organizationId = operation.getOrganization() != null ? operation.getOrganization().getOrganizationId() : null;
        final AuthenticationContext authenticationContext = new AuthenticationContext();
        if (credentialDefinition.isE2eEncryptionEnabled() && credentialDefinition.getE2eEncryptionAlgorithm() == EndToEndEncryptionAlgorithm.AES) {
            authenticationContext.setPasswordProtection(PasswordProtectionType.PASSWORD_ENCRYPTION_AES);
            authenticationContext.setCipherTransformation(credentialDefinition.getE2eEncryptionCipherTransformation());
        } else {
            authenticationContext.setPasswordProtection(PasswordProtectionType.NO_PROTECTION);
        }
        final CombinedAuthenticationResponse response = authenticationCustomizationService.authenticateCombined(otpId, otpValue, userId, organizationId, credentialValue, operation, authenticationContext);
        final boolean lastAttempt = response.getUserIdentityStatus() != UserIdentityStatus.ACTIVE ||
                (response.getRemainingAttempts() != null && response.getRemainingAttempts() == 0);
        boolean operationFailed = false;
        final UpdateOperationResponse operationResponse = updateOperation(userId, operation, authMethod, response.getAuthenticationResult(),
                null, lastAttempt, Arrays.asList(AuthInstrument.CREDENTIAL, AuthInstrument.OTP_KEY));
        if (operationResponse == null || operationResponse.getResult() == AuthResult.FAILED) {
            logger.debug("Operation update result is FAILED, operation ID: {}", operation.getOperationId());
            operationFailed = true;
        }
        response.setOperationFailed(operationFailed);
        logger.info("Combined custom authentication result: {}, OTP ID: {}, remaining attempts: {}, user ID: {}, operation failed: {}",
                response.getAuthenticationResult(), otpId, response.getRemainingAttempts(), userId, operationFailed);
        audit.info("Combined custom authentication result", AuditDetail.builder()
                .type(AUDIT_TYPE_AUTHENTICATION)
                .param("otpId", otpId)
                .param("userId", userId)
                .param("operationId", operation.getOperationId())
                .param("authenticationResult", response.getAuthenticationResult())
                .build());
        audit.debug("Combined custom authentication result (detail)", AuditDetail.builder()
                .type(AUDIT_TYPE_AUTHENTICATION)
                .param("otpId", otpId)
                .param("userId", userId)
                .param("operationId", operation.getOperationId())
                .param("authenticationResult", response.getAuthenticationResult())
                .param("remainingAttempts", response.getRemainingAttempts())
                .param("operationFailed", operationFailed)
                .build());
        return response;
    }

    /**
     * Get list of authentications for user.
     * @param request Get user authentication list request.
     * @return Get user authentication list response.
     * @throws UserNotFoundException Thrown when user is not found.
     */
    @Transactional
    public GetUserAuthenticationListResponse getUserAuthenticationList(GetUserAuthenticationListRequest request) throws UserNotFoundException {
        final UserIdentityLookupService userIdentityLookupService = serviceCatalogue.getUserIdentityLookupService();
        final UserIdentityEntity user = userIdentityLookupService.findUser(request.getUserId());
        final Stream<AuthenticationEntity> authentications;
        if (request.getCreatedStartDate() == null && request.getCreatedEndDate() == null) {
            authentications = authenticationRepository.findAllByUserIdOrderByTimestampCreatedDesc(user.getUserId());
        }  else {
            final Date startDate;
            final Date endDate;
            if (request.getCreatedStartDate() == null) {
                startDate = new Date(0L);
            } else {
                startDate = request.getCreatedStartDate();
            }
            if (request.getCreatedEndDate() == null) {
                endDate = new Date();
            } else {
                endDate = request.getCreatedEndDate();
            }
            authentications = authenticationRepository.findAuthenticationsByUserIdAndCreatedDate(user.getUserId(), startDate, endDate);
        }
        final GetUserAuthenticationListResponse response = new GetUserAuthenticationListResponse();
        response.setUserId(user.getUserId());
        authentications.forEach(authentication -> {
            final AuthenticationDetail authenticationDetail = authenticationConverter.fromEntity(authentication);
            response.getAuthentications().add(authenticationDetail);
        });
        authentications.close();
        return response;
    }

    /**
     * Verify a credential value.
     * @param authenticationMode Credential authentication mode.
     * @param credential Credential entity.
     * @param credentialValue Credential value to verify.
     * @param credentialPositionsToVerify Credential positions to verify for algorithm MATCH_ONLY_SPECIFIED_POSITIONS.
     * @return Authentication result.
     * @throws InvalidRequestException Thrown when request is invalid.
     * @throws InvalidConfigurationException Thrown when Next Step configuration is invalid.
     * @throws EncryptionException Thrown when decryption fails.
     */
    private AuthenticationResult verifyCredential(CredentialAuthenticationMode authenticationMode,
                                                  CredentialEntity credential, String credentialValue,
                                                  List<Integer> credentialPositionsToVerify) throws InvalidRequestException, InvalidConfigurationException, EncryptionException {
        final CredentialProtectionService credentialProtectionService = serviceCatalogue.getCredentialProtectionService();
        if (credential.getStatus() != CredentialStatus.ACTIVE) {
            logger.info("Credential verification failed, user ID: {}, credential definition name: {}, status: {}", credential.getUser().getUserId(), credential.getCredentialDefinition().getName(), credential.getStatus());
            return AuthenticationResult.FAILED;
        }
        // Temporary credentials cannot be used for authentication after their expiration
        if (credential.getType() == CredentialType.TEMPORARY
            && credential.getTimestampExpires() != null
            && new Date().after(credential.getTimestampExpires())) {
            logger.info("Credential verification failed because temporary credential is expired, user ID: {}, credential definition name: {}", credential.getUser().getUserId(), credential.getCredentialDefinition().getName());
            return AuthenticationResult.FAILED;
        }
        final CredentialAuthenticationMode authModeResolved;
        if (authenticationMode == null) {
            authModeResolved = CredentialAuthenticationMode.MATCH_EXACT;
        } else {
            authModeResolved = authenticationMode;
        }
        switch (authModeResolved) {
            case MATCH_EXACT:
                final boolean credentialMatched = credentialProtectionService.verifyCredential(credentialValue, credential);
                if (credentialMatched) {
                    logger.info("Credential verification succeeded, user ID: {}, credential definition name: {}", credential.getUser().getUserId(), credential.getCredentialDefinition().getName());
                    return AuthenticationResult.SUCCEEDED;
                } else {
                    logger.info("Credential verification failed, user ID: {}, credential definition name: {}, attempt counter: {}, soft counter: {}, hard counter: {}", credential.getUser().getUserId(), credential.getCredentialDefinition().getName(), credential.getAttemptCounter(), credential.getFailedAttemptCounterSoft(), credential.getFailedAttemptCounterHard());
                    return AuthenticationResult.FAILED;
                }

            case MATCH_ONLY_SPECIFIED_POSITIONS:
                if (credentialPositionsToVerify.isEmpty()) {
                    throw new InvalidRequestException("No positions specified for authentication mode MATCH_ONLY_SPECIFIED_POSITIONS");
                }
                if (credential.getHashingConfig() != null) {
                    throw new InvalidConfigurationException("Credential verification is not possible in MATCH_ONLY_SPECIFIED_POSITIONS mode when credential hashing is enabled");
                }

                final String expectedCredentialValue = credentialProtectionService.extractCredentialValue(credential);
                int counter = 0;
                for (Integer position : credentialPositionsToVerify) {
                    try {
                        final char c1 = credentialValue.charAt(counter);
                        final char c2 = expectedCredentialValue.charAt(position);
                        if (c1 != c2) {
                            logger.info("Credential verification failed for position match, user ID: {}, credential definition name: {}", credential.getUser().getUserId(), credential.getCredentialDefinition().getName());
                            return AuthenticationResult.FAILED;
                        }
                        counter++;
                    } catch (StringIndexOutOfBoundsException ex) {
                        // Index is out of bounds
                        logger.info("Credential verification failed because position is out of bounds, user ID: {}, credential definition name: {}", credential.getUser().getUserId(), credential.getCredentialDefinition().getName());
                        return AuthenticationResult.FAILED;
                    }
                }
                logger.info("Credential verification succeeded for position match, user ID: {}, credential definition name: {}", credential.getUser().getUserId(), credential.getCredentialDefinition().getName());
                return AuthenticationResult.SUCCEEDED;

            default:
                throw new InvalidRequestException("Invalid authentication mode: " + authenticationMode);
        }
    }

    /**
     * Verify an OTP value.
     * @param otp OTP entity.
     * @param otpValue OTP value to verify.
     * @return Authentication result.
     * @throws InvalidConfigurationException Thrown when Next Step configuration is invalid.
     * @throws EncryptionException Thrown when decryption fails.
     */
    private AuthenticationResult verifyOtp(OtpEntity otp, String otpValue) throws InvalidConfigurationException, EncryptionException {
        if (otp.getStatus() != OtpStatus.ACTIVE) {
            logger.info("OTP verification failed for OTP ID: {}, status: {}", otp.getOtpId(), otp.getStatus());
            return AuthenticationResult.FAILED;
        }
        if (otp.getTimestampExpires() != null && otp.getTimestampExpires().before(new Date())) {
            otp.setStatus(OtpStatus.BLOCKED);
            otp.setTimestampBlocked(new Date());
            otp.setFailedAttemptCounter(otp.getFailedAttemptCounter() + 1);
            logger.info("OTP verification failed because OTP is expired: {}", otp.getOtpId());
            return AuthenticationResult.FAILED;
        }
        final OtpValue otpValueDb = new OtpValue(otp.getEncryptionAlgorithm(), otp.getValue());
        final String value = otpValueConverter.fromDBValue(otpValueDb, otp.getOtpId(), otp.getOtpDefinition());
        if (value != null && value.equals(otpValue)) {
            logger.info("OTP verification succeeded, OTP ID: {}", otp.getOtpId());
            return AuthenticationResult.SUCCEEDED;
        } else {
            otp.setFailedAttemptCounter(otp.getFailedAttemptCounter() + 1);
            logger.info("OTP verification failed, OTP ID: {}, failed counter: {}", otp.getOtpId(), otp.getFailedAttemptCounter());
            return AuthenticationResult.FAILED;
        }
    }

    /**
     * Update operation with authentication result.
     * @param userId User ID.
     * @param operation Operation entity.
     * @param authMethod Authentication method performing authentication.
     * @param authenticationResult Authentication result.
     * @param authenticationId Authentication ID, use null for external authentication.
     * @param lastAttempt Whether this attempt is the last one.
     * @param authInstruments Authentication instruments.
     * @throws InvalidRequestException Thrown in case request is invalid.
     * @throws InvalidConfigurationException Thrown in case Next Step configuration is invalid.
     * @throws OperationNotFoundException Thrown in case operation is not found.
     * @throws OperationAlreadyFinishedException Thrown in case operation is already finished.
     * @throws OperationAlreadyCanceledException Thrown in case operation is already canceled.
     * @throws OperationAlreadyFailedException Thrown in case operation is already failed.
     * @throws AuthMethodNotFoundException Thrown in case authentication method is not found.
     * @throws OperationNotValidException Thrown in case operation is not valid.
     */
    private UpdateOperationResponse updateOperation(String userId, OperationEntity operation, AuthMethod authMethod,
                                                    AuthenticationResult authenticationResult, String authenticationId,
                                                    boolean lastAttempt, List<AuthInstrument> authInstruments) throws InvalidRequestException, InvalidConfigurationException, OperationNotFoundException, OperationAlreadyFinishedException, OperationAlreadyCanceledException, AuthMethodNotFoundException, OperationAlreadyFailedException, OperationNotValidException {
        final OperationPersistenceService operationPersistenceService = serviceCatalogue.getOperationPersistenceService();
        final UpdateOperationRequest updateRequest = new UpdateOperationRequest();
        if (userId != null) {
            // User ID can be null during OTP authentication with unknown user identity. The referred user ID may not be present in Next Step.
            updateRequest.setUserId(userId);
        }
        updateRequest.setOperationId(operation.getOperationId());
        updateRequest.getAuthInstruments().addAll(authInstruments);
        if (authMethod != null) {
            updateRequest.setAuthMethod(authMethod);
        } else {
            // Determine authentication method, this only works when there is a single next method available
            final OperationHistoryEntity currentHistory = operation.getCurrentOperationHistoryEntity();
            if (currentHistory == null) {
                // Cannot occur unless data in database is manually manipulated
                return null;
            }
            try {
                final List<AuthStep> authSteps = objectMapper.readValue(currentHistory.getResponseSteps(), new TypeReference<List<AuthStep>>() {});
                if (authSteps.size() != 1) {
                    throw new InvalidRequestException("Authentication method could not be determined " +
                            "during credential authentication, operation ID: " + operation.getOperationId());
                }
                updateRequest.setAuthMethod(authSteps.get(0).getAuthMethod());
            } catch (JsonProcessingException ex) {
                throw new InvalidConfigurationException("Next steps could not be determined " +
                        "during credential authentication, operation ID: " + operation.getOperationId());
            }
        }
        if (authenticationResult == AuthenticationResult.SUCCEEDED) {
            logger.debug("Operation will be updated with authentication step result CONFIRMED, operation ID: {}", operation.getOperationId());
            updateRequest.setAuthStepResult(AuthStepResult.CONFIRMED);
        } else if (!lastAttempt) {
            logger.debug("Operation will be updated with authentication step result AUTH_FAILED, operation ID: {}", operation.getOperationId());
            updateRequest.setAuthStepResult(AuthStepResult.AUTH_FAILED);
        } else {
            logger.debug("Operation will be updated with authentication step result AUTH_METHOD_FAILED, operation ID: {}", operation.getOperationId());
            updateRequest.setAuthStepResult(AuthStepResult.AUTH_METHOD_FAILED);
        }
        updateRequest.setAuthenticationId(authenticationId);
        try {
            return operationPersistenceService.updateOperation(updateRequest);
        } catch (OrganizationNotFoundException ex) {
            // Cannot occur, organization is not changed by the request
            return null;
        }
    }

    /**
     * Resolve remaining attempts for a credential entity, OTP entity and operation.
     * @param credential Credential entity or null for no credential.
     * @param otp OTP entity or null for no OTP.
     * @param operation Operation entity or null for no operation.
     * @return Remaining attempts.
     */
    private Integer resolveRemainingAttempts(CredentialEntity credential, OtpEntity otp, OperationEntity operation) {
        final StepResolutionService stepResolutionService = serviceCatalogue.getStepResolutionService();
        Integer remainingAttempts = null;
        if (otp != null) {
            final Integer limitOtp = otp.getOtpDefinition().getOtpPolicy().getAttemptLimit();
            if (otp.getStatus() != OtpStatus.ACTIVE) {
                remainingAttempts = 0;
            } else if (limitOtp != null) {
                remainingAttempts = limitOtp - otp.getFailedAttemptCounter();
            }
        }
        if (credential != null) {
            final Integer softLimit = credential.getCredentialDefinition().getCredentialPolicy().getLimitSoft();
            final Integer hardLimit = credential.getCredentialDefinition().getCredentialPolicy().getLimitHard();
            if (credential.getStatus() != CredentialStatus.ACTIVE) {
                remainingAttempts = 0;
            }
            if (credential.getStatus() == CredentialStatus.ACTIVE && softLimit != null) {
                final int remainingAttemptsSoft = softLimit - credential.getFailedAttemptCounterSoft();
                if (remainingAttempts == null || remainingAttemptsSoft < remainingAttempts) {
                    remainingAttempts = remainingAttemptsSoft;
                }
            }
            if (credential.getStatus() == CredentialStatus.ACTIVE && hardLimit != null) {
                final int remainingAttemptsHard = hardLimit - credential.getFailedAttemptCounterHard();
                if (remainingAttempts == null || remainingAttemptsHard < remainingAttempts) {
                    remainingAttempts = remainingAttemptsHard;
                }
            }
        }
        if (operation != null) {
            final Integer remainingAttemptsOperation = stepResolutionService.getNumberOfRemainingAttempts(operation);
            if (remainingAttemptsOperation != null) {
                if (remainingAttempts == null || remainingAttemptsOperation < remainingAttempts) {
                    remainingAttempts = remainingAttemptsOperation;
                }
            }
        }
        logger.debug("Remaining attempts: {}", remainingAttempts);
        return remainingAttempts;
    }

}
