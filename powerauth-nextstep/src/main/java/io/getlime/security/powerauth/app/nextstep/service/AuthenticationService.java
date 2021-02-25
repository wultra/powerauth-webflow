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
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.getlime.security.powerauth.app.nextstep.converter.AuthenticationConverter;
import io.getlime.security.powerauth.app.nextstep.repository.AuthenticationRepository;
import io.getlime.security.powerauth.app.nextstep.repository.model.entity.*;
import io.getlime.security.powerauth.lib.nextstep.model.entity.AuthStep;
import io.getlime.security.powerauth.lib.nextstep.model.entity.AuthenticationDetail;
import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.*;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthInstrument;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthMethod;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthStepResult;
import io.getlime.security.powerauth.lib.nextstep.model.exception.*;
import io.getlime.security.powerauth.lib.nextstep.model.request.*;
import io.getlime.security.powerauth.lib.nextstep.model.response.CombinedAuthenticationResponse;
import io.getlime.security.powerauth.lib.nextstep.model.response.CredentialAuthenticationResponse;
import io.getlime.security.powerauth.lib.nextstep.model.response.GetUserAuthenticationListResponse;
import io.getlime.security.powerauth.lib.nextstep.model.response.OtpAuthenticationResponse;
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

    private final AuthenticationConverter authenticationConverter = new AuthenticationConverter();

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Authentication service constructor.
     * @param authenticationRepository Authentication repository.
     * @param credentialDefinitionService Credential definition service.
     * @param userIdentityLookupService User identity lookup service.
     * @param otpService OTP service.
     * @param operationPersistenceService Operation persistence service.
     * @param credentialService Credential service.
     * @param credentialCounterService Credential counter service.
     */
    @Autowired
    public AuthenticationService(AuthenticationRepository authenticationRepository, CredentialDefinitionService credentialDefinitionService, UserIdentityLookupService userIdentityLookupService, OtpService otpService, OperationPersistenceService operationPersistenceService, CredentialService credentialService, CredentialCounterService credentialCounterService) {
        this.authenticationRepository = authenticationRepository;
        this.credentialDefinitionService = credentialDefinitionService;
        this.userIdentityLookupService = userIdentityLookupService;
        this.otpService = otpService;
        this.operationPersistenceService = operationPersistenceService;
        this.credentialService = credentialService;
        this.credentialCounterService = credentialCounterService;
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
     * @throws CredentialNotActiveException Thrown when credential is not active.
     * @throws CredentialNotFoundException Thrown when credential is not found.
     * @throws OperationAlreadyFinishedException Thrown when operation is already finished.
     * @throws OperationAlreadyCanceledException Thrown when operation is already canceled.
     * @throws OperationAlreadyFailedException Thrown when operation is already failed.
     * @throws OperationNotValidException Thrown when operation is not valid.
     * @throws AuthMethodNotFoundException Thrown when authentication method is not found.
     * @throws InvalidConfigurationException Thrown when Next Step configuration is invalid.
     * @throws UserNotActiveException Thrown when user identity is not active.
     */
    @Transactional
    public CredentialAuthenticationResponse authenticationWithCredential(CredentialAuthenticationRequest request) throws CredentialDefinitionNotFoundException, UserNotFoundException, OperationNotFoundException, InvalidRequestException, CredentialNotActiveException, CredentialNotFoundException, OperationAlreadyFinishedException, OperationAlreadyCanceledException, AuthMethodNotFoundException, OperationAlreadyFailedException, InvalidConfigurationException, UserNotActiveException, OperationNotValidException {
        CredentialDefinitionEntity credentialDefinition = credentialDefinitionService.findCredentialDefinition(request.getCredentialName());
        UserIdentityEntity user = userIdentityLookupService.findUser(request.getUserId());
        if (user.getStatus() != UserIdentityStatus.ACTIVE) {
            throw new UserNotActiveException("User identity is not ACTIVE, user ID: " + request.getUserId());
        }
        OperationEntity operation = null;
        if (request.getOperationId() != null) {
            operation = operationPersistenceService.getOperation(request.getOperationId());
        }
        if (request.isUpdateOperation() && operation == null) {
            throw new InvalidRequestException("Operation not found, however operation update requested for credential: " + request.getCredentialName());
        }
        CredentialEntity credential = credentialService.findCredential(credentialDefinition, user);

        // Verify credential value
        AuthenticationResult authenticationResult;
        if (credential.getStatus() == CredentialStatus.ACTIVE) {
            authenticationResult = verifyCredential(request.getAuthenticationMode(), credential, request.getCredentialValue());
        } else {
            authenticationResult = AuthenticationResult.FAILED;
        }

        // Update counters based on authentication result
        credentialCounterService.updateCredentialCounter(credential, authenticationResult);

        AuthenticationEntity authentication = new AuthenticationEntity();
        authentication.setAuthenticationId(UUID.randomUUID().toString());
        authentication.setUserId(user.getUserId());
        authentication.setAuthenticationType(AuthenticationType.CREDENTIAL);
        authentication.setCredential(credential);
        if (operation != null) {
            authentication.setOperation(operation);
        }
        authentication.setResult(authenticationResult);
        authentication.setResultCredential(authenticationResult);
        authentication.setTimestampCreated(new Date());
        authenticationRepository.save(authentication);

        if (request.isUpdateOperation() && operation != null) {
            updateOperation(user.getUserId(), operation, request.getAuthMethod(), credential,
                    authentication, Collections.singletonList(AuthInstrument.CREDENTIAL));
        }

        Long remainingAttempts = resolveRemainingAttempts(credential, null);

        CredentialAuthenticationResponse response = new CredentialAuthenticationResponse();
        response.setUserId(user.getUserId());
        response.setUserIdentityStatus(user.getStatus());
        response.setTimestampBlocked(credential.getTimestampBlocked());
        response.setCredentialStatus(credential.getStatus());
        response.setAuthenticationResult(authenticationResult);
        response.setRemainingAttempts(remainingAttempts);
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
     * @throws CredentialNotActiveException Thrown when credential is not active.
     * @throws CredentialNotFoundException Thrown when credential is not found.
     * @throws OperationAlreadyCanceledException Thrown when operation is already canceled.
     * @throws OperationAlreadyFinishedException Thrown when operation is already finished.
     * @throws OperationAlreadyFailedException Thrown when operation is already failed.
     * @throws OperationNotValidException Thrown when operation is not valid.
     * @throws InvalidConfigurationException Thrown when Next Step configuration is invalid.
     * @throws AuthMethodNotFoundException Thrown when authentication method is not found.
     * @throws UserNotActiveException Thrown when user identity is not active.
     */
    @Transactional
    public OtpAuthenticationResponse authenticationWithOtp(OtpAuthenticationRequest request) throws OtpNotFoundException, OperationNotFoundException, InvalidRequestException, CredentialNotActiveException, CredentialNotFoundException, OperationAlreadyCanceledException, OperationAlreadyFinishedException, InvalidConfigurationException, AuthMethodNotFoundException, OperationAlreadyFailedException, UserNotActiveException, OperationNotValidException {
        OtpEntity otp = otpService.findOtp(request.getOtpId(), request.getOperationId());
        // User ID uses String reference in entity to allow OTP for users not present in Next Step.
        // Empty user ID is allowed for unknown identities.
        String userId = otp.getUserId();
        UserIdentityEntity user = null;
        if (userId != null) {
            Optional<UserIdentityEntity> userOptional = userIdentityLookupService.findUserOptional(userId);
            if (userOptional.isPresent()) {
                user = userOptional.get();
                if (user.getStatus() != UserIdentityStatus.ACTIVE) {
                    throw new UserNotActiveException("User identity is not ACTIVE, user ID: " + user.getUserId());
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

        if (otp.getStatus() == OtpStatus.ACTIVE) {
            authenticationResult = verifyOtp(otp, request.getOtpValue());
        } else {
            authenticationResult = AuthenticationResult.FAILED;
        }

        if (authenticationResult == AuthenticationResult.SUCCEEDED) {
            otp.setStatus(OtpStatus.USED);
            otp.setTimestampVerified(new Date());
        }

        CredentialEntity credential = null;
        if (otp.getCredentialDefinition() != null && user != null) {
            credential = credentialService.findCredential(otp.getCredentialDefinition(), user);
            credentialCounterService.updateCredentialCounter(credential, authenticationResult);
        }

        Long remainingAttempts = resolveRemainingAttempts(credential, otp);
        if (remainingAttempts == 0L && otp.getStatus() == OtpStatus.ACTIVE) {
            otp.setStatus(OtpStatus.BLOCKED);
            otp.setTimestampBlocked(new Date());
        }

        AuthenticationEntity authentication = new AuthenticationEntity();
        authentication.setAuthenticationId(UUID.randomUUID().toString());
        authentication.setUserId(userId);
        authentication.setAuthenticationType(AuthenticationType.OTP);
        authentication.setOtp(otp);
        if (operation != null) {
            authentication.setOperation(operation);
        }
        authentication.setResult(authenticationResult);
        authentication.setResultOtp(authenticationResult);
        authentication.setTimestampCreated(new Date());
        authenticationRepository.save(authentication);

        if (request.isUpdateOperation() && operation != null) {
            updateOperation(userId, operation, request.getAuthMethod(), credential,
                    authentication, Collections.singletonList(AuthInstrument.OTP_KEY));
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
        return response;
    }

    /**
     * Combined authentication with credential and OTP.
     * @param request Combined authentication request.
     * @return Combined authentication response.
     * @throws CredentialDefinitionNotFoundException Thrown when credential definition is not found.
     * @throws UserNotFoundException Thrown when user is not found.
     * @throws OperationNotFoundException Thrown when operation is not found.
     * @throws InvalidRequestException Thrown when request is invalid.
     * @throws CredentialNotActiveException Thrown when credential is not active.
     * @throws CredentialNotFoundException Thrown when credential is not found.
     * @throws OtpNotFoundException Thrown when OTP is not found.
     * @throws OperationAlreadyCanceledException Thrown when operation is already canceled.
     * @throws OperationAlreadyFinishedException Thrown when operation is already finished.
     * @throws OperationAlreadyFailedException Thrown when operation is already failed.
     * @throws OperationNotValidException Thrown when operation is not valid.
     * @throws InvalidConfigurationException Thrown when Next Step configuration is invalid.
     * @throws AuthMethodNotFoundException Thrown when authentication method is not found.
     * @throws UserNotActiveException Thrown when user identity is not active.
     */
    @Transactional
    public CombinedAuthenticationResponse authenticationCombined(CombinedAuthenticationRequest request) throws CredentialDefinitionNotFoundException, UserNotFoundException, OperationNotFoundException, InvalidRequestException, CredentialNotActiveException, CredentialNotFoundException, OtpNotFoundException, OperationAlreadyCanceledException, OperationAlreadyFinishedException, InvalidConfigurationException, AuthMethodNotFoundException, OperationAlreadyFailedException, UserNotActiveException, OperationNotValidException {
        CredentialDefinitionEntity credentialDefinition = credentialDefinitionService.findCredentialDefinition(request.getCredentialName());
        UserIdentityEntity user = userIdentityLookupService.findUser(request.getUserId());
        if (user.getStatus() != UserIdentityStatus.ACTIVE) {
            throw new UserNotActiveException("User identity is not ACTIVE, user ID: " + user.getUserId());
        }
        OtpEntity otp = otpService.findOtp(request.getOtpId(), request.getOperationId());
        if (!user.getUserId().equals(otp.getUserId())) {
            throw new InvalidRequestException("Invalid user ID for OTP: " + otp.getOtpId());
        }
        OperationEntity operation = null;
        if (request.getOperationId() != null) {
            operation = operationPersistenceService.getOperation(request.getOperationId());
        }
        if (request.isUpdateOperation() && operation == null) {
            throw new InvalidRequestException("Operation not found, however operation update requested for credential and OTP: " + request.getCredentialName() + ", " + otp.getOtpDefinition().getName());
        } else if (!request.getOperationId().equals(otp.getOperation().getOperationId())) {
            throw new InvalidRequestException("Operation ID mismatch for credential and OTP: " + request.getCredentialName() + ", " + otp.getOtpDefinition().getName());
        }
        CredentialEntity credential = credentialService.findCredential(credentialDefinition, user);

        // Verify credential value
        AuthenticationResult credentialAuthenticationResult;
        if (credential.getStatus() == CredentialStatus.ACTIVE) {
            credentialAuthenticationResult = verifyCredential(request.getAuthenticationMode(), credential, request.getCredentialValue());
        } else {
            credentialAuthenticationResult = AuthenticationResult.FAILED;
        }

        // Verify OTP value
        AuthenticationResult otpAuthenticationResult;
        if (otp.getStatus() == OtpStatus.ACTIVE) {
            otpAuthenticationResult = verifyOtp(otp, request.getOtpValue());
        } else {
            otpAuthenticationResult = AuthenticationResult.FAILED;
        }

        // Set overall authentication result
        AuthenticationResult authenticationResult;
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

        Long remainingAttempts = resolveRemainingAttempts(credential, otp);
        if (remainingAttempts == 0L && otp.getStatus() == OtpStatus.ACTIVE) {
            otp.setStatus(OtpStatus.BLOCKED);
            otp.setTimestampBlocked(new Date());
        }

        AuthenticationEntity authentication = new AuthenticationEntity();
        authentication.setAuthenticationId(UUID.randomUUID().toString());
        authentication.setUserId(user.getUserId());
        authentication.setAuthenticationType(AuthenticationType.CREDENTIAL_OTP);
        authentication.setCredential(credential);
        if (operation != null) {
            authentication.setOperation(operation);
        }
        authentication.setResult(authenticationResult);
        authentication.setResultCredential(credentialAuthenticationResult);
        authentication.setResultOtp(otpAuthenticationResult);
        authentication.setTimestampCreated(new Date());
        authenticationRepository.save(authentication);

        if (request.isUpdateOperation() && operation != null) {
            updateOperation(user.getUserId(), operation, request.getAuthMethod(), credential,
                    authentication, Arrays.asList(AuthInstrument.CREDENTIAL, AuthInstrument.OTP_KEY));
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
            authentications = authenticationRepository.findAllByUserIdOrderByTimestampCreatedDesc(user);
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
            authentications = authenticationRepository.findAuthenticationsByUserIdAndCreatedDate(user, startDate, endDate);
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
                                                  CredentialEntity credential, String credentialValue) throws InvalidRequestException {
        switch (authenticationMode) {
            case MATCH_EXACT:
                boolean credentialMatched = credential.getValue().equals(credentialValue);
                if (credentialMatched) {
                    return AuthenticationResult.SUCCEEDED;
                } else {
                    return AuthenticationResult.FAILED;
                }

            case MATCH_ONLY_SPECIFIED_POSITIONS:
                // TODO - implement matching of specified positions
                return AuthenticationResult.FAILED;

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
     * @param credential Credential entity.
     * @param authentication Authentication entity.
     * @throws InvalidRequestException Thrown in case request is invalid.
     * @throws InvalidConfigurationException Thrown in case Next Step configuration is invalid.
     * @throws OperationNotFoundException Thrown in case operation is not found.
     * @throws OperationAlreadyFinishedException Thrown in case operation is already finished.
     * @throws OperationAlreadyCanceledException Thrown in case operation is already canceled.
     * @throws OperationAlreadyFailedException Thrown in case operation is already failed.
     * @throws AuthMethodNotFoundException Thrown in case authentication method is not found.
     * @throws OperationNotValidException Thrown in case operation is not valid.
     */
    private void updateOperation(String userId, OperationEntity operation, AuthMethod authMethod,
                                 CredentialEntity credential, AuthenticationEntity authentication,
                                 List<AuthInstrument> authInstruments) throws InvalidRequestException, InvalidConfigurationException, OperationNotFoundException, OperationAlreadyFinishedException, OperationAlreadyCanceledException, AuthMethodNotFoundException, OperationAlreadyFailedException, OperationNotValidException {
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
        AuthenticationResult authenticationResult = authentication.getResult();
        if (authenticationResult == AuthenticationResult.SUCCEEDED) {
            updateRequest.setAuthStepResult(AuthStepResult.CONFIRMED);
        } else if (credential == null || credential.getStatus() == CredentialStatus.ACTIVE) {
            updateRequest.setAuthStepResult(AuthStepResult.AUTH_FAILED);
        } else {
            updateRequest.setAuthStepResult(AuthStepResult.AUTH_METHOD_FAILED);
        }
        updateRequest.setAuthenticationId(authentication.getAuthenticationId());
        operationPersistenceService.updateOperation(updateRequest);
    }

    /**
     * Resolve remaining attempts for a credential entity and OTP entity.
     * @param credential Credential entity, use null for no credential.
     * @param otp OTP entity, use null for no OTP.
     * @return Remaining attempts.
     */
    private Long resolveRemainingAttempts(CredentialEntity credential, OtpEntity otp) {
        Long remainingAttempts = null;
        if (otp != null) {
            Long limitOtp = otp.getOtpDefinition().getOtpPolicy().getAttemptLimit();
            if (otp.getStatus() != OtpStatus.ACTIVE) {
                remainingAttempts = 0L;
            } else if (limitOtp != null) {
                remainingAttempts = limitOtp - otp.getFailedAttemptCounter();
            }
        }
        if (credential != null) {
            Long softLimit = credential.getCredentialDefinition().getCredentialPolicy().getLimitSoft();
            Long hardLimit = credential.getCredentialDefinition().getCredentialPolicy().getLimitHard();
            if (credential.getStatus() != CredentialStatus.ACTIVE) {
                remainingAttempts = 0L;
            }
            if (credential.getStatus() == CredentialStatus.ACTIVE && softLimit != null) {
                long remainingAttemptsSoft = softLimit - credential.getFailedAttemptCounterSoft();
                if (remainingAttempts == null || remainingAttemptsSoft < remainingAttempts) {
                    remainingAttempts = remainingAttemptsSoft;
                }
            }
            if (credential.getStatus() == CredentialStatus.ACTIVE && hardLimit != null) {
                long remainingAttemptsHard = hardLimit - credential.getFailedAttemptCounterHard();
                if (remainingAttempts == null || remainingAttemptsHard < remainingAttempts) {
                    remainingAttempts = remainingAttemptsHard;
                }
            }
        }
        return remainingAttempts;
    }

}
