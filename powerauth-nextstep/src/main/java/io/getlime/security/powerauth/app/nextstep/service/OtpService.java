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

import io.getlime.security.powerauth.app.nextstep.OtpValueConverter;
import io.getlime.security.powerauth.app.nextstep.repository.OtpRepository;
import io.getlime.security.powerauth.app.nextstep.repository.model.entity.*;
import io.getlime.security.powerauth.app.nextstep.service.adapter.OtpCustomizationService;
import io.getlime.security.powerauth.lib.nextstep.model.entity.OtpDeliveryResult;
import io.getlime.security.powerauth.lib.nextstep.model.entity.OtpDetail;
import io.getlime.security.powerauth.lib.nextstep.model.entity.OtpValue;
import io.getlime.security.powerauth.lib.nextstep.model.entity.OtpValueDetail;
import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.OtpStatus;
import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.UserIdentityStatus;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthResult;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.OtpGenerationAlgorithm;
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
 * This service handles persistence of one time passwords.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Service
public class OtpService {

    private final Logger logger = LoggerFactory.getLogger(OtpService.class);

    private final OtpDefinitionService otpDefinitionService;
    private final UserIdentityLookupService userIdentityLookupService;
    private final CredentialDefinitionService credentialDefinitionService;
    private final CredentialService credentialService;
    private final OperationPersistenceService operationPersistenceService;
    private final OtpGenerationService otpGenerationService;
    private final StepResolutionService stepResolutionService;
    private final OtpRepository otpRepository;
    private final OtpCustomizationService otpCustomizationService;
    private final IdGeneratorService idGeneratorService;
    private final OtpValueConverter otpValueConverter;

    /**
     * OTP service constructor.
     * @param otpDefinitionService OTP definition service.
     * @param userIdentityLookupService User identity lookup service.
     * @param credentialDefinitionService Credential definition service.
     * @param credentialService Credential service.
     * @param operationPersistenceService Operation persistence service.
     * @param otpGenerationService OTP generation service.
     * @param stepResolutionService Step resolution service.
     * @param otpRepository OTP repository.
     * @param otpCustomizationService OTP customization service.
     * @param idGeneratorService ID generator service.
     * @param otpValueConverter OTP value converter.
     */
    @Autowired
    public OtpService(OtpDefinitionService otpDefinitionService, UserIdentityLookupService userIdentityLookupService, CredentialDefinitionService credentialDefinitionService, CredentialService credentialService, OperationPersistenceService operationPersistenceService, OtpGenerationService otpGenerationService, StepResolutionService stepResolutionService, OtpRepository otpRepository, OtpCustomizationService otpCustomizationService, IdGeneratorService idGeneratorService, OtpValueConverter otpValueConverter) {
        this.otpDefinitionService = otpDefinitionService;
        this.userIdentityLookupService = userIdentityLookupService;
        this.credentialDefinitionService = credentialDefinitionService;
        this.credentialService = credentialService;
        this.operationPersistenceService = operationPersistenceService;
        this.otpGenerationService = otpGenerationService;
        this.stepResolutionService = stepResolutionService;
        this.otpRepository = otpRepository;
        this.otpCustomizationService = otpCustomizationService;
        this.idGeneratorService = idGeneratorService;
        this.otpValueConverter = otpValueConverter;
    }

    /**
     * Create an OTP.
     * @param request Create OTP request.
     * @return Create OTP response.
     * @throws OtpDefinitionNotFoundException Thrown when OTP definition is not found.
     * @throws UserNotActiveException Thrown when user is not active.
     * @throws CredentialDefinitionNotFoundException Thrown when credential definition is not found.
     * @throws CredentialNotFoundException Thrown when credential is not found.
     * @throws CredentialNotActiveException Thrown when credential is not active.
     * @throws OperationNotFoundException Thrown when operation is not found.
     * @throws InvalidRequestException Thrown when request is invalid.
     * @throws OtpGenAlgorithmNotSupportedException Thrown when OTP generation algorithm is not supported.
     * @throws InvalidConfigurationException Thrown when OTP policy is not configured properly.
     * @throws OperationAlreadyFinishedException Thrown when operation is already finished.
     * @throws OperationAlreadyFinishedException Thrown when operation is already failed.
     * @throws EncryptionException Thrown when encryption fails.
     */
    @Transactional
    public CreateOtpResponse createOtp(CreateOtpRequest request) throws OtpDefinitionNotFoundException, UserNotActiveException, CredentialDefinitionNotFoundException, OperationNotFoundException, InvalidRequestException, OtpGenAlgorithmNotSupportedException, InvalidConfigurationException, OperationAlreadyFinishedException, OperationAlreadyFailedException, CredentialNotActiveException, CredentialNotFoundException, EncryptionException {
        OtpDefinitionEntity otpDefinition = otpDefinitionService.findActiveOtpDefinition(request.getOtpName());
        String userId = request.getUserId();
        String credentialName = request.getCredentialName();
        String otpData = request.getOtpData();
        String operationId = request.getOperationId();
        return createOtpInternal(otpDefinition, userId, credentialName, otpData, operationId);
    }

    /**
     * Create and send and OTP. Depending on configuration the OTP is created in Next Step or in Data Adapter.
     * OTP delivery is always done via Data Adapter.
     * @param request Create and send an OTP.
     * @return Create and send OTP response.
     * @throws OtpDefinitionNotFoundException Thrown when OTP definition is not found.
     * @throws UserNotActiveException Thrown when user is not active.
     * @throws CredentialDefinitionNotFoundException Thrown when credential definition is not found.
     * @throws CredentialNotFoundException Thrown when credential is not found.
     * @throws CredentialNotActiveException Thrown when credential is not active.
     * @throws OperationNotFoundException Thrown when operation is not found.
     * @throws InvalidRequestException Thrown when request is invalid.
     * @throws OtpGenAlgorithmNotSupportedException Thrown when OTP generation algorithm is not supported.
     * @throws InvalidConfigurationException Thrown when OTP policy is not configured properly.
     * @throws OperationAlreadyFinishedException Thrown when operation is already finished.
     * @throws OperationAlreadyFinishedException Thrown when operation is already failed.
     * @throws EncryptionException Thrown when encryption fails.
     */
    @Transactional
    public CreateAndSendOtpResponse createAndSendOtp(CreateAndSendOtpRequest request) throws OtpDefinitionNotFoundException, CredentialNotFoundException, CredentialNotActiveException, InvalidRequestException, InvalidConfigurationException, OtpGenAlgorithmNotSupportedException, CredentialDefinitionNotFoundException, OperationAlreadyFinishedException, OperationAlreadyFailedException, OperationNotFoundException, UserNotActiveException, EncryptionException {
        OtpDefinitionEntity otpDefinition = otpDefinitionService.findActiveOtpDefinition(request.getOtpName());
        String userId = request.getUserId();
        String credentialName = request.getCredentialName();
        String otpData = request.getOtpData();
        String operationId = request.getOperationId();
        String language = request.getLanguage();
        boolean dataAdapterProxyEnabled = otpDefinition.isDataAdapterProxyEnabled();
        boolean resend = false;
        // Operation is required, otherwise Data Adapter would not have enough context to generate SMS message
        OperationEntity operation = operationPersistenceService.getOperation(operationId);
        List<OtpEntity> existingOtps = otpRepository.findAllByOperationOrderByTimestampCreatedDesc(operation);
        if (!existingOtps.isEmpty()) {
            resend = true;
        }
        if (dataAdapterProxyEnabled) {
            // Create and send OTP code via Data Adapter
            OtpDeliveryResult result = otpCustomizationService.createAndSendOtp(userId, operation, language, resend);
            // Store a local OTP record so that OTP can be found during authentication
            OtpEntity otp = new OtpEntity();
            otp.setOtpId(result.getOtpId());
            otp.setOtpDefinition(otpDefinition);
            otp.setUserId(userId);
            otp.setOperation(operation);
            otp.setStatus(OtpStatus.EXTERNAL);
            otp.setTimestampCreated(new Date());
            otpRepository.save(otp);
            CreateAndSendOtpResponse response = new CreateAndSendOtpResponse();
            response.setOtpName(otpDefinition.getName());
            response.setUserId(userId);
            response.setOtpId(result.getOtpId());
            // Derive status based on whether OTP code was generated and delivered
            if (result.getOtpId() != null && result.isDelivered()) {
                response.setOtpStatus(OtpStatus.ACTIVE);
            } else {
                response.setOtpStatus(OtpStatus.BLOCKED);
            }
            response.setDelivered(result.isDelivered());
            response.setErrorMessage(result.getErrorMessage());
            return response;
        } else {
            // Create OTP in Next Step and send it via Data Adapter
            CreateOtpResponse otpResponse = createOtpInternal(otpDefinition, userId, credentialName, otpData, operationId);
            OtpDeliveryResult result = otpCustomizationService.sendOtp(userId, operation, otpResponse.getOtpId(), otpResponse.getOtpValue(), language, resend);
            CreateAndSendOtpResponse response = new CreateAndSendOtpResponse();
            response.setOtpName(otpDefinition.getName());
            response.setUserId(userId);
            response.setOtpId(otpResponse.getOtpId());
            response.setOtpStatus(otpResponse.getOtpStatus());
            response.setDelivered(result.isDelivered());
            response.setErrorMessage(result.getErrorMessage());
            return response;
        }
    }

    /**
     * Create an OTP.
     * @param otpDefinition OTP definition.
     * @param userId User ID.
     * @param credentialName Credential definition name.
     * @param otpData OTP data.
     * @param operationId Operation ID.
     * @return Create OTP response.
     * @throws UserNotActiveException Thrown when user is not active.
     * @throws CredentialDefinitionNotFoundException Thrown when credential definition is not found.
     * @throws CredentialNotFoundException Thrown when credential is not found.
     * @throws CredentialNotActiveException Thrown when credential is not active.
     * @throws OperationNotFoundException Thrown when operation is not found.
     * @throws InvalidRequestException Thrown when request is invalid.
     * @throws OtpGenAlgorithmNotSupportedException Thrown when OTP generation algorithm is not supported.
     * @throws InvalidConfigurationException Thrown when OTP policy is not configured properly.
     * @throws OperationAlreadyFinishedException Thrown when operation is already finished.
     * @throws OperationAlreadyFinishedException Thrown when operation is already failed.
     * @throws EncryptionException Thrown when encryption fails.
     */
    private CreateOtpResponse createOtpInternal(OtpDefinitionEntity otpDefinition, String userId, String credentialName, String otpData, String operationId) throws UserNotActiveException, CredentialDefinitionNotFoundException, OperationNotFoundException, InvalidRequestException, OtpGenAlgorithmNotSupportedException, InvalidConfigurationException, OperationAlreadyFinishedException, OperationAlreadyFailedException, CredentialNotActiveException, CredentialNotFoundException, EncryptionException {
        UserIdentityEntity user = null;
        if (userId != null) {
            Optional<UserIdentityEntity> userOptional = userIdentityLookupService.findUserOptional(userId);
            if (userOptional.isPresent()) {
                user = userOptional.get();
                if(user.getStatus() != UserIdentityStatus.ACTIVE) {
                    throw new UserNotActiveException("User identity is not ACTIVE: " + user.getUserId());
                }
            }
        }
        CredentialDefinitionEntity credentialDefinition = null;
        if (user != null && credentialName != null) {
            credentialDefinition = credentialDefinitionService.findActiveCredentialDefinition(credentialName);
            // Make sure the credential exists and it is active
            credentialService.findActiveCredential(credentialDefinition, user);
        }
        OperationEntity operation = null;
        if (operationId != null) {
            operation = operationPersistenceService.getOperation(operationId);
            // Remove obsolete OTPs for this operation
            List<OtpEntity> existingOtps = otpRepository.findAllByOperationAndStatus(operation, OtpStatus.ACTIVE);
            for (OtpEntity otp : existingOtps) {
                otp.setStatus(OtpStatus.REMOVED);
                otpRepository.save(otp);
            }
            if (operation.getResult() == AuthResult.DONE) {
                throw new OperationAlreadyFinishedException("Cannot create OTP, because operation is already finished: " + operation.getOperationId());
            }
            if (operation.getResult() == AuthResult.FAILED) {
                throw new OperationAlreadyFailedException("Cannot create OTP, because operation is already failed: " + operation.getOperationId());
            }
        }
        String otpDataToUse = null;
        if (otpData != null) {
            otpDataToUse = otpData;
        } else if (operation != null) {
            otpDataToUse = operation.getOperationData();
        } else if (otpDefinition.getOtpPolicy().getGenAlgorithm() == OtpGenerationAlgorithm.OTP_DATA_DIGEST) {
            throw new InvalidRequestException("OTP data is not available for OTP definition: " + otpDefinition.getName());
        }
        OtpValueDetail otpValueDetail = otpGenerationService.generateOtpValue(otpData, otpDefinition.getOtpPolicy());
        String otpId = idGeneratorService.generateOtpId();
        OtpEntity otp = new OtpEntity();
        otp.setOtpId(otpId);
        otp.setOtpDefinition(otpDefinition);
        otp.setUserId(userId);
        otp.setCredentialDefinition(credentialDefinition);
        otp.setOperation(operation);
        OtpValue otpValueDb = otpValueConverter.toDBValue(otpValueDetail.getOtpValue(), otpId, otpDefinition);
        otp.setValue(otpValueDb.getValue());
        otp.setEncryptionAlgorithm(otpValueDb.getEncryptionAlgorithm());
        otp.setSalt(otpValueDetail.getSalt());
        otp.setStatus(OtpStatus.ACTIVE);
        otp.setOtpData(otpDataToUse);
        otp.setAttemptCounter(0);
        otp.setFailedAttemptCounter(0);
        otp.setTimestampCreated(new Date());
        Long expirationTime = otpDefinition.getOtpPolicy().getExpirationTime();
        if (expirationTime != null) {
            Calendar cal = GregorianCalendar.getInstance();
            cal.add(Calendar.SECOND, expirationTime.intValue());
            otp.setTimestampExpires(cal.getTime());
        }
        otpRepository.save(otp);
        CreateOtpResponse response = new CreateOtpResponse();
        response.setOtpName(otp.getOtpDefinition().getName());
        response.setUserId(userId);
        response.setOtpId(otp.getOtpId());
        response.setOtpValue(otpValueDetail.getOtpValue());
        response.setOtpStatus(otp.getStatus());
        return response;
    }

    /**
     * Get OTP list for an operation.
     * @param request Get OTP list request.
     * @return Get OTP list response.
     * @throws OperationNotFoundException Thrown when operation is not found.
     * @throws InvalidConfigurationException Thrown when Next Step configuration is invalid.
     * @throws EncryptionException Thrown when decryption fails.
     */
    @Transactional
    public GetOtpListResponse getOtpList(GetOtpListRequest request) throws OperationNotFoundException, InvalidConfigurationException, EncryptionException {
        OperationEntity operation = operationPersistenceService.getOperation(request.getOperationId());
        List<OtpEntity> otpList = otpRepository.findAllByOperationOrderByTimestampCreatedDesc(operation);
        GetOtpListResponse response = new GetOtpListResponse();
        response.setOperationId(operation.getOperationId());
        for (OtpEntity otp : otpList) {
            if (!request.isIncludeRemoved() && otp.getStatus() == OtpStatus.REMOVED) {
                continue;
            }
            OtpDetail otpDetail = getOtpDetail(otp);
            response.getOtpDetails().add(otpDetail);
        }
        return response;
    }

    /**
     * Get OTP detail.
     * @param request Get OTP detail request.
     * @return Get OTP detail response.
     * @throws OperationNotFoundException Thrown when operation is not found.
     * @throws OtpNotFoundException Thrown when OTP is not found.
     * @throws InvalidRequestException Thrown when request is invalid.
     * @throws InvalidConfigurationException Thrown when Next Step configuration is invalid.
     * @throws EncryptionException Thrown when decryption fails.
     */
    @Transactional
    public GetOtpDetailResponse getOtpDetail(GetOtpDetailRequest request) throws OperationNotFoundException, OtpNotFoundException, InvalidRequestException, InvalidConfigurationException, EncryptionException {
        OtpEntity otp = findOtp(request.getOtpId(), request.getOperationId());
        OtpDetail otpDetail = getOtpDetail(otp);
        GetOtpDetailResponse response = new GetOtpDetailResponse();
        response.setOperationId(request.getOperationId());
        response.setOtpDetail(otpDetail);
        return response;
    }

    /**
     * Delete an OTP.
     * @param request Delete OTP request.
     * @return Delete OTP response.
     * @throws OtpNotFoundException Thrown when OTP is not found.
     * @throws OperationNotFoundException Thrown when operation is not found.
     * @throws InvalidRequestException Thrown when request is invalid.
     */
    @Transactional
    public DeleteOtpResponse deleteOtp(DeleteOtpRequest request) throws OtpNotFoundException, OperationNotFoundException, InvalidRequestException {
        OtpEntity otp = findOtp(request.getOtpId(), request.getOperationId());
        if (otp.getStatus() == OtpStatus.REMOVED) {
            throw new OtpNotFoundException("OTP is already removed: " + request.getOtpId() + ", operation ID: " + request.getOperationId());
        }
        otp.setStatus(OtpStatus.REMOVED);
        otpRepository.save(otp);
        DeleteOtpResponse response = new DeleteOtpResponse();
        response.setOtpId(otp.getOtpId());
        if (otp.getOperation() != null) {
            response.setOperationId(otp.getOperation().getOperationId());
        }
        response.setOtpStatus(otp.getStatus());
        return response;
    }

    /**
     * Find an OTP by an otp ID and/or operation ID. This method is not transactional.
     * @param otpId Otp ID.
     * @param operationId Operation ID.
     * @return OTP entity.
     * @throws OtpNotFoundException Thrown when operation is not found.
     * @throws InvalidRequestException Thrown when request is invalid.
     * @throws OperationNotFoundException Thrown when operation is not found.
     */
    public OtpEntity findOtp(String otpId, String operationId) throws OtpNotFoundException, InvalidRequestException, OperationNotFoundException {
        OtpEntity otp;
        if (otpId != null) {
            Optional<OtpEntity> otpOptional = otpRepository.findById(otpId);
            if (!otpOptional.isPresent()) {
                throw new OtpNotFoundException("OTP not found: " + otpId);
            }
            otp = otpOptional.get();
            if (operationId != null) {
                if (otp.getOperation() == null) {
                    throw new InvalidRequestException("OTP was not created within an operation: " + otpId);
                }
                if (!operationId.equals(otp.getOperation().getOperationId())) {
                    throw new InvalidRequestException("Operation ID mismatch for an OTP: " + otpId + ", operation ID: " + operationId);
                }
            }
        } else if (operationId != null) {
            OperationEntity operation = operationPersistenceService.getOperation(operationId);
            List<OtpEntity> otpList = otpRepository.findAllByOperationOrderByTimestampCreatedDesc(operation);
            if (otpList.isEmpty()) {
                throw new OtpNotFoundException("No OTP found for operation: " + operation.getOperationId());
            }
            otp = otpList.get(0);
        } else {
            throw new InvalidRequestException("Missing otp ID or operation ID");
        }
        return otp;
    }

    /**
     * Get OTP detail for given OTP and operation.
     * @param otp OTP entity.
     * @return Operation detail.
     * @throws InvalidConfigurationException Thrown when Next Step configuration is invalid.
     * @throws EncryptionException Thrown when decryption fails.
     */
    private OtpDetail getOtpDetail(OtpEntity otp) throws InvalidConfigurationException, EncryptionException {
        OtpDetail otpDetail = new OtpDetail();
        otpDetail.setOtpName(otp.getOtpDefinition().getName());
        if (otp.getUserId() != null) {
            otpDetail.setUserId(otp.getUserId());
        }
        otpDetail.setOtpId(otp.getOtpId());
        if (otp.getOperation() != null) {
            otpDetail.setOperationId(otp.getOperation().getOperationId());
        }
        Integer remainingAttempts = resolveRemainingAttempts(otp);
        otpDetail.setRemainingAttempts(remainingAttempts);
        otpDetail.setOtpData(otp.getOtpData());
        OtpValue otpValueDb = new OtpValue(otp.getEncryptionAlgorithm(), otp.getValue());
        String value = otpValueConverter.fromDBValue(otpValueDb, otp.getOtpId(), otp.getOtpDefinition());
        otpDetail.setOtpValue(value);
        if (otp.getCredentialDefinition() != null) {
            otpDetail.setCredentialName(otp.getCredentialDefinition().getName());
        }
        otpDetail.setAttemptCounter(otp.getAttemptCounter());
        otpDetail.setFailedAttemptCounter(otp.getFailedAttemptCounter());
        otpDetail.setOtpStatus(otp.getStatus());
        otpDetail.setTimestampCreated(otp.getTimestampCreated());
        otpDetail.setTimestampVerified(otp.getTimestampVerified());
        otpDetail.setTimestampBlocked(otp.getTimestampBlocked());
        otpDetail.setTimestampExpires(otp.getTimestampExpires());
        return otpDetail;
    }

    /**
     * Resolve remaining attempts for an OTP.
     * @param otp OTP entity.
     * @return Remaining attempts.
     */
    private Integer resolveRemainingAttempts(OtpEntity otp) {
        OtpPolicyEntity otpPolicy = otp.getOtpDefinition().getOtpPolicy();
        Integer remainingAttempts = null;
        if (otpPolicy.getAttemptLimit() != null) {
            remainingAttempts = otpPolicy.getAttemptLimit() - otp.getFailedAttemptCounter();
        }
        if (otp.getOperation() != null) {
            OperationEntity operationEntity = otp.getOperation();
            Integer remainingAttemptsOperation = stepResolutionService.getNumberOfRemainingAttempts(operationEntity);
            if (remainingAttemptsOperation != null && (remainingAttempts == null || remainingAttemptsOperation < remainingAttempts)) {
                return remainingAttemptsOperation;
            } else {
                return remainingAttempts;
            }
        } else {
            return remainingAttempts;
        }
    }

}