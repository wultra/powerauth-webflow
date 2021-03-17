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
import io.getlime.security.powerauth.app.nextstep.converter.AuthenticationConverter;
import io.getlime.security.powerauth.app.nextstep.repository.AuthenticationRepository;
import io.getlime.security.powerauth.app.nextstep.repository.model.entity.*;
import io.getlime.security.powerauth.app.nextstep.service.adapter.AuthenticationCustomizationService;
import io.getlime.security.powerauth.lib.nextstep.model.entity.AuthStep;
import io.getlime.security.powerauth.lib.nextstep.model.entity.AuthenticationDetail;
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
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.*;

/**
 * This service handles user authentication.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Service
public class AuthenticationService {

    private final Logger logger = LoggerFactory.getLogger(AuthenticationService.class);

    private final AuthenticationRepository authenticationRepository;
    private final CredentialDefinitionService credentialDefinitionService;
    private final UserIdentityLookupService userIdentityLookupService;
    private final OtpService otpService;
    private final OperationPersistenceService operationPersistenceService;
    private final CredentialService credentialService;
    private final CredentialCounterService credentialCounterService;
    private final StepResolutionService stepResolutionService;
    private final IdGeneratorService idGeneratorService;
    private final AuthenticationCustomizationService authenticationCustomizationService;
    private final CredentialProtectionService credentialProtectionService;

    private final AuthenticationConverter authenticationConverter = new AuthenticationConverter();

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Authentication service constructor.
     *
     * @param authenticationRepository Authentication repository.
     * @param credentialDefinitionService Credential definition service.
     * @param userIdentityLookupService User identity lookup service.
     * @param otpService OTP service.
     * @param operationPersistenceService Operation persistence service.
     * @param credentialService Credential service.
     * @param credentialCounterService Credential counter service.
     * @param stepResolutionService Step resolution service.
     * @param idGeneratorService ID generator service.
     * @param authenticationCustomizationService Authentication customization service.
     * @param credentialProtectionService Credential protection service.
     */
    @Autowired
    public AuthenticationService(AuthenticationRepository authenticationRepository, CredentialDefinitionService credentialDefinitionService, UserIdentityLookupService userIdentityLookupService, OtpService otpService, OperationPersistenceService operationPersistenceService, CredentialService credentialService, CredentialCounterService credentialCounterService, StepResolutionService stepResolutionService, IdGeneratorService idGeneratorService, AuthenticationCustomizationService authenticationCustomizationService, CredentialProtectionService credentialProtectionService) {
        this.authenticationRepository = authenticationRepository;
        this.credentialDefinitionService = credentialDefinitionService;
        this.userIdentityLookupService = userIdentityLookupService;
        this.otpService = otpService;
        this.operationPersistenceService = operationPersistenceService;
        this.credentialService = credentialService;
        this.credentialCounterService = credentialCounterService;
        this.stepResolutionService = stepResolutionService;
        this.idGeneratorService = idGeneratorService;
        this.authenticationCustomizationService = authenticationCustomizationService;
        this.credentialProtectionService = credentialProtectionService;
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
     */
    @Transactional
    public CredentialAuthenticationResponse authenticateWithCredential(CredentialAuthenticationRequest request) throws CredentialDefinitionNotFoundException, UserNotFoundException, OperationNotFoundException, InvalidRequestException, CredentialNotFoundException, OperationAlreadyFinishedException, OperationAlreadyCanceledException, AuthMethodNotFoundException, OperationAlreadyFailedException, InvalidConfigurationException, OperationNotValidException {
        CredentialDefinitionEntity credentialDefinition = credentialDefinitionService.findActiveCredentialDefinition(request.getCredentialName());
        if (credentialDefinition.isDataAdapterProxyEnabled()) {
            return authenticateWithCredentialCustom(credentialDefinition, request.getCredentialValue(), request.getOperationId(), request.getUserId(), request.getAuthMethod());
        }
        UserIdentityEntity user = userIdentityLookupService.findUser(request.getUserId());
        if (user.getStatus() != UserIdentityStatus.ACTIVE) {
            CredentialAuthenticationResponse response = new CredentialAuthenticationResponse();
            response.setUserId(user.getUserId());
            response.setUserIdentityStatus(user.getStatus());
            response.setAuthenticationResult(AuthenticationResult.FAILED);
            response.setRemainingAttempts(0);
            return response;
        }
        OperationEntity operation = null;
        if (request.getOperationId() != null) {
            operation = operationPersistenceService.getOperation(request.getOperationId());
        }
        if (request.isUpdateOperation() && operation == null) {
            throw new InvalidRequestException("Operation not found, however operation update requested for credential: " + request.getCredentialName());
        }
        CredentialEntity credential = credentialService.findCredential(credentialDefinition, user);
        credential.setAttemptCounter(credential.getAttemptCounter() + 1);

        // Verify credential value
        AuthenticationResult authenticationResult;
        if (credential.getStatus() == CredentialStatus.ACTIVE) {
            authenticationResult = verifyCredential(request.getAuthenticationMode(), credential, request.getCredentialValue(), request.getCredentialPositionsToVerify());
        } else {
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
        authenticationRepository.save(authentication);

        boolean lastAttempt = false;
        if (user.getStatus() != UserIdentityStatus.ACTIVE || credential.getStatus() != CredentialStatus.ACTIVE) {
            lastAttempt = true;
        }

        boolean operationFailed = false;
        if (request.isUpdateOperation() && operation != null) {
            UpdateOperationResponse operationResponse = updateOperation(user.getUserId(), operation,
                    request.getAuthMethod(), authenticationResult, authentication.getAuthenticationId(),
                    lastAttempt, Collections.singletonList(AuthInstrument.CREDENTIAL));
            if (operationResponse == null || operationResponse.getResult() == AuthResult.FAILED) {
                operationFailed = true;
                authentication.setResultCredential(AuthenticationResult.FAILED);
                authentication.setResult(AuthenticationResult.FAILED);
                // Authentication was updated, save it
                authenticationRepository.save(authentication);
            }
        }

        Integer remainingAttempts = resolveRemainingAttempts(credential, null, operation);

        CredentialAuthenticationResponse response = new CredentialAuthenticationResponse();
        response.setUserId(user.getUserId());
        response.setUserIdentityStatus(user.getStatus());
        response.setTimestampBlocked(credential.getTimestampBlocked());
        response.setCredentialStatus(credential.getStatus());
        response.setAuthenticationResult(authenticationResult);
        response.setRemainingAttempts(remainingAttempts);
        response.setOperationFailed(operationFailed);
        response.setCredentialChangeRequired(credentialService.isCredentialChangeRequired(credential, request.getCredentialValue()));
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
        OrganizationEntity organization = credentialDefinition.getApplication().getOrganization();
        String organizationId = null;
        if (organization != null) {
            organizationId = organization.getOrganizationId();
        }
        if (operationId == null) {
            throw new InvalidRequestException("Operation ID is missing in Data Adapter authentication with credential request");
        }
        OperationEntity operation = operationPersistenceService.getOperation(operationId);
        CredentialAuthenticationResponse response = authenticationCustomizationService.authenticateWithCredential(userId, organizationId, credentialValue, operation);
        boolean lastAttempt = false;
        if (response.getUserIdentityStatus() != UserIdentityStatus.ACTIVE ||
                response.getRemainingAttempts() != null && response.getRemainingAttempts() == 0) {
            lastAttempt = true;
        }
        boolean operationFailed = false;
        UpdateOperationResponse operationResponse = updateOperation(userId, operation, authMethod, response.getAuthenticationResult(), null, lastAttempt, Collections.singletonList(AuthInstrument.CREDENTIAL));
        if (operationResponse == null || operationResponse.getResult() == AuthResult.FAILED) {
            operationFailed = true;
        }
        response.setOperationFailed(operationFailed);
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
     */
    @Transactional
    public OtpAuthenticationResponse authenticateWithOtp(OtpAuthenticationRequest request) throws OtpNotFoundException, OperationNotFoundException, InvalidRequestException, CredentialNotFoundException, OperationAlreadyCanceledException, OperationAlreadyFinishedException, InvalidConfigurationException, AuthMethodNotFoundException, OperationAlreadyFailedException, OperationNotValidException {
        OtpEntity otp = otpService.findOtp(request.getOtpId(), request.getOperationId());
        if (otp.getOtpDefinition().isDataAdapterProxyEnabled()) {
            return authenticateWithOtpCustom(otp.getOtpDefinition(), otp.getOtpId(), request.getOtpValue(), otp.getOperation().getOperationId(), otp.getUserId(), request.getAuthMethod());
        }
        otp.setAttemptCounter(otp.getAttemptCounter() + 1);
        // User ID uses String reference in entity to allow OTP for users not present in Next Step.
        // Empty user ID is allowed for unknown identities.
        String userId = otp.getUserId();
        UserIdentityEntity user = null;
        if (userId != null) {
            Optional<UserIdentityEntity> userOptional = userIdentityLookupService.findUserOptional(userId);
            if (userOptional.isPresent()) {
                user = userOptional.get();
                if (user.getStatus() != UserIdentityStatus.ACTIVE) {
                    OtpAuthenticationResponse response = new OtpAuthenticationResponse();
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
            throw new InvalidRequestException("Operation not found, however operation update requested for OTP: " + otp.getOtpDefinition().getName());
        }

        AuthenticationResult authenticationResult;
        CredentialEntity credential = null;
        boolean credentialActive = true;

        if (otp.getCredentialDefinition() != null && user != null) {
            credential = credentialService.findCredential(otp.getCredentialDefinition(), user);
            if (credential.getStatus() != CredentialStatus.ACTIVE) {
                credentialActive = false;
            }
        }

        if (!credentialActive) {
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
            }
            if (credential != null) {
                credentialCounterService.updateCredentialCounter(credential, authenticationResult);
            }
        }

        AuthenticationEntity authentication = new AuthenticationEntity();
        authentication.setAuthenticationId(UUID.randomUUID().toString());
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
        authenticationRepository.save(authentication);

        boolean lastAttempt = false;
        if ((user != null && user.getStatus() != UserIdentityStatus.ACTIVE)
                || (credential != null && credential.getStatus() != CredentialStatus.ACTIVE)
                || otp.getStatus() != OtpStatus.ACTIVE) {
            lastAttempt = true;
        }

        boolean operationFailed = false;
        if (request.isUpdateOperation() && operation != null) {
            UpdateOperationResponse operationResponse = updateOperation(userId, operation, request.getAuthMethod(),
                    authenticationResult, authentication.getAuthenticationId(), lastAttempt, Collections.singletonList(AuthInstrument.OTP_KEY));
            if (operationResponse == null || operationResponse.getResult() == AuthResult.FAILED) {
                operationFailed = true;
                authentication.setResultOtp(AuthenticationResult.FAILED);
                authentication.setResult(AuthenticationResult.FAILED);
                // Authentication was updated, save it
                authenticationRepository.save(authentication);
            }
        }

        Integer remainingAttempts = resolveRemainingAttempts(credential, otp, operation);
        if (remainingAttempts == 0 && otp.getStatus() == OtpStatus.ACTIVE) {
            otp.setStatus(OtpStatus.BLOCKED);
            otp.setTimestampBlocked(new Date());
            // OTP was updated, save authentication with OTP
            authenticationRepository.save(authentication);
        }

        OtpAuthenticationResponse response = new OtpAuthenticationResponse();
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
        OrganizationEntity organization = otpDefinition.getApplication().getOrganization();
        String organizationId = null;
        if (organization != null) {
            organizationId = organization.getOrganizationId();
        }
        if (operationId == null) {
            throw new InvalidRequestException("Operation ID is missing in Data Adapter authentication with credential request");
        }
        OperationEntity operation = operationPersistenceService.getOperation(operationId);
        OtpAuthenticationResponse response = authenticationCustomizationService.authenticateWithOtp(otpId, otpValue, userId, organizationId, operation);
        boolean lastAttempt = false;
        if (response.getUserIdentityStatus() != UserIdentityStatus.ACTIVE ||
                response.getRemainingAttempts() != null && response.getRemainingAttempts() == 0) {
            lastAttempt = true;
        }
        boolean operationFailed = false;
        UpdateOperationResponse operationResponse = updateOperation(userId, operation, authMethod, response.getAuthenticationResult(),
                null, lastAttempt, Collections.singletonList(AuthInstrument.OTP_KEY));
        if (operationResponse == null || operationResponse.getResult() == AuthResult.FAILED) {
            operationFailed = true;
        }
        response.setOperationFailed(operationFailed);
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
     */
    @Transactional
    public CombinedAuthenticationResponse authenticateCombined(CombinedAuthenticationRequest request) throws UserNotFoundException, OperationNotFoundException, InvalidRequestException, CredentialNotFoundException, OtpNotFoundException, OperationAlreadyCanceledException, OperationAlreadyFinishedException, InvalidConfigurationException, AuthMethodNotFoundException, OperationAlreadyFailedException, OperationNotValidException {
        OtpEntity otp = otpService.findOtp(request.getOtpId(), request.getOperationId());
        if (otp.getOtpDefinition().isDataAdapterProxyEnabled()) {
            return authenticateCombinedCustom(otp.getOtpDefinition(), otp.getOtpId(), request.getOtpValue(), request.getCredentialValue(), otp.getOperation().getOperationId(), otp.getUserId(), request.getAuthMethod());
        }
        otp.setAttemptCounter(otp.getAttemptCounter() + 1);

        UserIdentityEntity user = userIdentityLookupService.findUser(request.getUserId());
        if (user.getStatus() != UserIdentityStatus.ACTIVE) {
            CombinedAuthenticationResponse response = new CombinedAuthenticationResponse();
            response.setUserId(user.getUserId());
            response.setUserIdentityStatus(user.getStatus());
            response.setAuthenticationResult(AuthenticationResult.FAILED);
            response.setCredentialAuthenticationResult(AuthenticationResult.FAILED);
            response.setOtpAuthenticationResult(AuthenticationResult.FAILED);
            response.setRemainingAttempts(0);
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
        CredentialEntity credential = credentialService.findCredential(otp.getCredentialDefinition(), user);
        credential.setAttemptCounter(credential.getAttemptCounter() + 1);
        AuthenticationResult credentialAuthenticationResult;
        AuthenticationResult otpAuthenticationResult;
        AuthenticationResult authenticationResult;
        if (credential.getStatus() != CredentialStatus.ACTIVE) {
            // Fail authentication and block OTP in case credential is not active
            authenticationResult = AuthenticationResult.FAILED;
            credentialAuthenticationResult = AuthenticationResult.FAILED;
            otpAuthenticationResult = AuthenticationResult.FAILED;
            otp.setStatus(OtpStatus.BLOCKED);
            otp.setTimestampBlocked(new Date());
        } else {
            credentialAuthenticationResult = verifyCredential(request.getAuthenticationMode(), credential, request.getCredentialValue(), request.getCredentialPositionsToVerify());

            // Verify OTP value
            if (otp.getStatus() == OtpStatus.ACTIVE) {
                otpAuthenticationResult = verifyOtp(otp, request.getOtpValue());
            } else {
                otpAuthenticationResult = AuthenticationResult.FAILED;
            }

            // Set overall authentication result
            if (credentialAuthenticationResult == AuthenticationResult.SUCCEEDED && otpAuthenticationResult == AuthenticationResult.SUCCEEDED) {
                authenticationResult = AuthenticationResult.SUCCEEDED;
            } else {
                authenticationResult = AuthenticationResult.FAILED;
            }

            if (authenticationResult == AuthenticationResult.SUCCEEDED) {
                otp.setStatus(OtpStatus.USED);
                otp.setTimestampVerified(new Date());
            }

            // Update counters based on authentication result
            credentialCounterService.updateCredentialCounter(credential, authenticationResult);
        }


        AuthenticationEntity authentication = new AuthenticationEntity();
        authentication.setAuthenticationId(UUID.randomUUID().toString());
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
        authenticationRepository.save(authentication);

        boolean lastAttempt = false;
        if ( user.getStatus() != UserIdentityStatus.ACTIVE
                || credential.getStatus() != CredentialStatus.ACTIVE
                || otp.getStatus() != OtpStatus.ACTIVE) {
            lastAttempt = true;
        }

        boolean operationFailed = false;
        if (request.isUpdateOperation() && operation != null) {
            UpdateOperationResponse operationResponse = updateOperation(user.getUserId(), operation, request.getAuthMethod(),
                    authenticationResult, authentication.getAuthenticationId(), lastAttempt,
                    Arrays.asList(AuthInstrument.CREDENTIAL, AuthInstrument.OTP_KEY));
            if (operationResponse == null || operationResponse.getResult() == AuthResult.FAILED) {
                operationFailed = true;
                authentication.setResult(AuthenticationResult.FAILED);
                authentication.setResultOtp(AuthenticationResult.FAILED);
                authentication.setResult(AuthenticationResult.FAILED);
                // Authentication was updated, save it
                authenticationRepository.save(authentication);
            }
        }

        Integer remainingAttempts = resolveRemainingAttempts(credential, otp, operation);
        if (remainingAttempts == 0 && otp.getStatus() == OtpStatus.ACTIVE) {
            otp.setStatus(OtpStatus.BLOCKED);
            otp.setTimestampBlocked(new Date());
            // OTP was updated, save authentication with OTP
            authenticationRepository.save(authentication);
        }

        CombinedAuthenticationResponse response = new CombinedAuthenticationResponse();
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
        response.setCredentialChangeRequired(credentialService.isCredentialChangeRequired(credential, request.getCredentialValue()));
        return response;
    }

    /**
     * Perform custom authentication with OTP and credential.
     * @param otpDefinition OTP definition.
     * @param otpId OTP ID.
     * @param otpValue OTP value.
     * @param operationId Operation ID.
     * @param userId User ID.
     * @param authMethod Authentication method.
     */
    private CombinedAuthenticationResponse authenticateCombinedCustom(OtpDefinitionEntity otpDefinition, String otpId, String otpValue, String credentialValue,
                                                                    String operationId, String userId, AuthMethod authMethod) throws InvalidRequestException, OperationNotFoundException, InvalidConfigurationException, OperationAlreadyFinishedException, OperationAlreadyFailedException, OperationNotValidException, AuthMethodNotFoundException, OperationAlreadyCanceledException {
        OrganizationEntity organization = otpDefinition.getApplication().getOrganization();
        String organizationId = null;
        if (organization != null) {
            organizationId = organization.getOrganizationId();
        }
        if (operationId == null) {
            throw new InvalidRequestException("Operation ID is missing in Data Adapter authentication with credential request");
        }
        OperationEntity operation = operationPersistenceService.getOperation(operationId);
        CombinedAuthenticationResponse response = authenticationCustomizationService.authenticateCombined(otpId, otpValue, userId, organizationId, credentialValue, operation);
        boolean lastAttempt = false;
        if (response.getUserIdentityStatus() != UserIdentityStatus.ACTIVE ||
                response.getRemainingAttempts() != null && response.getRemainingAttempts() == 0) {
            lastAttempt = true;
        }
        boolean operationFailed = false;
        UpdateOperationResponse operationResponse = updateOperation(userId, operation, authMethod, response.getAuthenticationResult(),
                null, lastAttempt, Arrays.asList(AuthInstrument.CREDENTIAL, AuthInstrument.OTP_KEY));
        if (operationResponse == null || operationResponse.getResult() == AuthResult.FAILED) {
            operationFailed = true;
        }
        response.setOperationFailed(operationFailed);
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
        UserIdentityEntity user = userIdentityLookupService.findUser(request.getUserId());
        List<AuthenticationEntity> authentications;
        if (request.getCreatedStartDate() == null && request.getCreatedEndDate() == null) {
            authentications = authenticationRepository.findAllByUserIdOrderByTimestampCreatedDesc(user.getUserId());
        }  else {
            Date startDate;
            Date endDate;
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
        GetUserAuthenticationListResponse response = new GetUserAuthenticationListResponse();
        response.setUserId(user.getUserId());
        for (AuthenticationEntity authentication: authentications) {
            AuthenticationDetail authenticationDetail = authenticationConverter.fromEntity(authentication);
            response.getAuthentications().add(authenticationDetail);
        }
        return response;
    }

    /**
     * Verify a credential value.
     * @param authenticationMode Credential authentication mode.
     * @param credential Credential entity.
     * @param credentialValue Credential value to verify.
     * @return Authentication result.
     * @throws InvalidRequestException Thrown when request is invalid.
     */
    private AuthenticationResult verifyCredential(CredentialAuthenticationMode authenticationMode,
                                                  CredentialEntity credential, String credentialValue,
                                                  List<Integer> credentialPositionsToVerify) throws InvalidRequestException, InvalidConfigurationException {
        if (credential.getStatus() != CredentialStatus.ACTIVE) {
            return AuthenticationResult.FAILED;
        }
        CredentialAuthenticationMode authModeResolved;
        if (authenticationMode == null) {
            authModeResolved = CredentialAuthenticationMode.MATCH_EXACT;
        } else {
            authModeResolved = authenticationMode;
        }
        switch (authModeResolved) {
            case MATCH_EXACT:
                boolean credentialMatched = credentialProtectionService.verifyCredential(credentialValue, credential.getValue(), credential.getCredentialDefinition());
                if (credentialMatched) {
                    return AuthenticationResult.SUCCEEDED;
                } else {
                    return AuthenticationResult.FAILED;
                }

            case MATCH_ONLY_SPECIFIED_POSITIONS:
                if (credentialPositionsToVerify.isEmpty()) {
                    throw new InvalidRequestException("No positions specified for authentication mode MATCH_ONLY_SPECIFIED_POSITIONS");
                }
                int counter = 0;
                for (Integer position : credentialPositionsToVerify) {
                    try {
                        char c1 = credentialValue.charAt(counter);
                        char c2 = credential.getValue().charAt(position);
                        if (c1 != c2) {
                            return AuthenticationResult.FAILED;
                        }
                        counter++;
                    } catch (StringIndexOutOfBoundsException ex) {
                        // Index is out of bounds
                        return AuthenticationResult.FAILED;
                    }
                }
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
     */
    private AuthenticationResult verifyOtp(OtpEntity otp, String otpValue) {
        if (otp.getStatus() != OtpStatus.ACTIVE) {
            return AuthenticationResult.FAILED;
        }
        if (otp.getTimestampExpires() != null && otp.getTimestampExpires().before(new Date())) {
            otp.setStatus(OtpStatus.BLOCKED);
            otp.setTimestampBlocked(new Date());
            otp.setFailedAttemptCounter(otp.getFailedAttemptCounter() + 1);
            return AuthenticationResult.FAILED;
        }
        if (otp.getValue().matches(otpValue)) {
            return AuthenticationResult.SUCCEEDED;
        } else {
            otp.setFailedAttemptCounter(otp.getFailedAttemptCounter() + 1);
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
        UpdateOperationRequest updateRequest = new UpdateOperationRequest();
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
            OperationHistoryEntity currentHistory = operation.getCurrentOperationHistoryEntity();
            try {
                List<AuthStep> authSteps = objectMapper.readValue(currentHistory.getResponseSteps(), new TypeReference<List<AuthStep>>() {});
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
            updateRequest.setAuthStepResult(AuthStepResult.CONFIRMED);
        } else if (!lastAttempt) {
            updateRequest.setAuthStepResult(AuthStepResult.AUTH_FAILED);
        } else {
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
        Integer remainingAttempts = null;
        if (otp != null) {
            Integer limitOtp = otp.getOtpDefinition().getOtpPolicy().getAttemptLimit();
            if (otp.getStatus() != OtpStatus.ACTIVE) {
                remainingAttempts = 0;
            } else if (limitOtp != null) {
                remainingAttempts = limitOtp - otp.getFailedAttemptCounter();
            }
        }
        if (credential != null) {
            Integer softLimit = credential.getCredentialDefinition().getCredentialPolicy().getLimitSoft();
            Integer hardLimit = credential.getCredentialDefinition().getCredentialPolicy().getLimitHard();
            if (credential.getStatus() != CredentialStatus.ACTIVE) {
                remainingAttempts = 0;
            }
            if (credential.getStatus() == CredentialStatus.ACTIVE && softLimit != null) {
                int remainingAttemptsSoft = softLimit - credential.getFailedAttemptCounterSoft();
                if (remainingAttempts == null || remainingAttemptsSoft < remainingAttempts) {
                    remainingAttempts = remainingAttemptsSoft;
                }
            }
            if (credential.getStatus() == CredentialStatus.ACTIVE && hardLimit != null) {
                int remainingAttemptsHard = hardLimit - credential.getFailedAttemptCounterHard();
                if (remainingAttempts == null || remainingAttemptsHard < remainingAttempts) {
                    remainingAttempts = remainingAttemptsHard;
                }
            }
        }
        if (operation != null) {
            Integer remainingAttemptsOperation = stepResolutionService.getNumberOfRemainingAttempts(operation);
            if (remainingAttemptsOperation != null) {
                if (remainingAttempts == null || remainingAttemptsOperation < remainingAttempts) {
                    remainingAttempts = remainingAttemptsOperation;
                }
            }
        }
        return remainingAttempts;
    }

}
