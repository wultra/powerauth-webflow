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

import io.getlime.security.powerauth.app.nextstep.repository.OtpRepository;
import io.getlime.security.powerauth.app.nextstep.repository.model.entity.*;
import io.getlime.security.powerauth.lib.nextstep.model.entity.OtpDetail;
import io.getlime.security.powerauth.lib.nextstep.model.entity.OtpValueDetail;
import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.OtpStatus;
import io.getlime.security.powerauth.lib.nextstep.model.exception.*;
import io.getlime.security.powerauth.lib.nextstep.model.request.CreateOtpRequest;
import io.getlime.security.powerauth.lib.nextstep.model.request.DeleteOtpRequest;
import io.getlime.security.powerauth.lib.nextstep.model.request.GetOtpDetailRequest;
import io.getlime.security.powerauth.lib.nextstep.model.request.GetOtpListRequest;
import io.getlime.security.powerauth.lib.nextstep.model.response.CreateOtpResponse;
import io.getlime.security.powerauth.lib.nextstep.model.response.DeleteOtpResponse;
import io.getlime.security.powerauth.lib.nextstep.model.response.GetOtpDetailResponse;
import io.getlime.security.powerauth.lib.nextstep.model.response.GetOtpListResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
    private final OperationPersistenceService operationPersistenceService;
    private final OtpGenerationService otpGenerationService;
    private final StepResolutionService stepResolutionService;
    private final OtpRepository otpRepository;

    /**
     * OTP service constructor.
     * @param otpDefinitionService OTP definition service.
     * @param userIdentityLookupService User identity lookup service.
     * @param credentialDefinitionService Credential definition service.
     * @param operationPersistenceService Operation persistence service.
     * @param otpGenerationService OTP generation service.
     * @param stepResolutionService Step resolution service.
     * @param otpRepository OTP repository.
     */
    @Autowired
    public OtpService(OtpDefinitionService otpDefinitionService, UserIdentityLookupService userIdentityLookupService, CredentialDefinitionService credentialDefinitionService, OperationPersistenceService operationPersistenceService, OtpGenerationService otpGenerationService, StepResolutionService stepResolutionService, OtpRepository otpRepository) {
        this.otpDefinitionService = otpDefinitionService;
        this.userIdentityLookupService = userIdentityLookupService;
        this.credentialDefinitionService = credentialDefinitionService;
        this.operationPersistenceService = operationPersistenceService;
        this.otpGenerationService = otpGenerationService;
        this.stepResolutionService = stepResolutionService;
        this.otpRepository = otpRepository;
    }

    /**
     * Create an OTP.
     * @param request Create OTP request.
     * @return Create OTP response.
     * @throws OtpDefinitionNotFoundException Thrown when OTP definition is not found.
     * @throws UserNotFoundException Thrown when user is not found.
     * @throws CredentialDefinitionNotFoundException Thrown when credential definition is not found.
     * @throws OperationNotFoundException Thrown when operation is not found.
     * @throws InvalidRequestException Thrown when request is invalid.
     * @throws OtpGenAlgorithmNotSupportedException Thrown when OTP generation algorithm is not supported.
     * @throws InvalidConfigurationException Thrown when OTP policy is not configured properly.
     */
    @Transactional
    public CreateOtpResponse createOtp(CreateOtpRequest request) throws OtpDefinitionNotFoundException, UserNotFoundException, CredentialDefinitionNotFoundException, OperationNotFoundException, InvalidRequestException, OtpGenAlgorithmNotSupportedException, InvalidConfigurationException {
        OtpDefinitionEntity otpDefinition = otpDefinitionService.findOtpDefinition(request.getOtpName());
        UserIdentityEntity user = null;
        if (request.getUserId() != null) {
            user = userIdentityLookupService.findUser(request.getUserId());
        }
        CredentialDefinitionEntity credentialDefinition = null;
        if (request.getCredentialName() != null) {
            credentialDefinition = credentialDefinitionService.findCredentialDefinition(request.getCredentialName());
        }
        OperationEntity operation = null;
        if (request.getOperationId() != null) {
            operation = operationPersistenceService.getOperation(request.getOperationId());
            // Remove obsolete OTPs for this operation
            List<OtpEntity> existingOtps = otpRepository.findAllByOperationAndStatus(operation, OtpStatus.ACTIVE);
            for (OtpEntity otp : existingOtps) {
                otp.setStatus(OtpStatus.REMOVED);
                otpRepository.save(otp);
            }
        }
        String otpData;
        if (request.getOtpData() != null) {
            otpData = request.getOtpData();
        } else if (operation != null) {
            otpData = operation.getOperationData();
        } else {
            throw new InvalidRequestException("OTP data is not available for OTP definition: " + otpDefinition.getName());
        }
        OtpValueDetail otpValueDetail = otpGenerationService.generateOtpValue(otpData, otpDefinition.getOtpPolicy());
        OtpEntity otp = new OtpEntity();
        otp.setOtpId(UUID.randomUUID().toString());
        otp.setOtpDefinition(otpDefinition);
        otp.setUserId(user);
        otp.setCredentialDefinition(credentialDefinition);
        otp.setOperation(operation);
        otp.setValue(otpValueDetail.getOtpValue());
        otp.setSalt(otpValueDetail.getSalt());
        otp.setStatus(OtpStatus.ACTIVE);
        otp.setOtpData(otpData);
        otp.setAttemptCounter(0);
        otp.setFailedAttemptCounter(0);
        otp.setTimestampCreated(new Date());
        otpRepository.save(otp);
        CreateOtpResponse response = new CreateOtpResponse();
        response.setOtpName(otp.getOtpDefinition().getName());
        if (user != null) {
            response.setUserId(user.getUserId());
        }
        response.setOtpId(otp.getOtpId());
        response.setOtpValue(otp.getValue());
        response.setOtpStatus(otp.getStatus());
        return response;
    }

    /**
     * Get OTP list for an operation.
     * @param request Get OTP list request.
     * @return Get OTP list response.
     * @throws OperationNotFoundException Thrown when operation is not found.
     */
    @Transactional
    public GetOtpListResponse getOtpList(GetOtpListRequest request) throws OperationNotFoundException {
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
     */
    @Transactional
    public GetOtpDetailResponse getOtpDetail(GetOtpDetailRequest request) throws OperationNotFoundException, OtpNotFoundException, InvalidRequestException {
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
     * Get OTP detail for given OTP and operation.
     * @param otp OTP entity.
     * @return Operation detail.
     */
    private OtpDetail getOtpDetail(OtpEntity otp) {
        OtpDetail otpDetail = new OtpDetail();
        otpDetail.setOtpName(otp.getOtpDefinition().getName());
        if (otp.getUserId() != null) {
            otpDetail.setUserId(otp.getUserId().getUserId());
        }
        otpDetail.setOtpId(otp.getOtpId());
        if (otp.getOperation() != null) {
            otpDetail.setOperationId(otp.getOperation().getOperationId());
        }
        Long remainingAttempts = resolveRemainingAttempts(otp);
        otpDetail.setRemainingAttempts(remainingAttempts);
        otpDetail.setOtpData(otp.getOtpData());
        otpDetail.setOtpValue(otp.getValue());
        if (otp.getCredentialDefinition() != null) {
            otpDetail.setCredentialName(otp.getCredentialDefinition().getName());
        }
        otpDetail.setAttemptCounter(otp.getAttemptCounter());
        otpDetail.setFailedAttemptCounter(otp.getFailedAttemptCounter());
        otpDetail.setOtpStatus(otp.getStatus());
        otpDetail.setTimestampCreated(otp.getTimestampCreated());
        otpDetail.setTimestampExpired(otp.getTimestampExpired());
        return otpDetail;
    }

    /**
     * Find an OTP by an otp ID and/or operation ID.
     * @param otpId Otp ID.
     * @param operationId Operation ID.
     * @return OTP entity.
     * @throws OtpNotFoundException Thrown when operation is not found.
     * @throws InvalidRequestException Thrown when request is invalid.
     * @throws OperationNotFoundException Thrown when operation is not found.
     */
    private OtpEntity findOtp(String otpId, String operationId) throws OtpNotFoundException, InvalidRequestException, OperationNotFoundException {
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
     * Resolve remaining attempts for an OTP.
     * @param otp OTP entity.
     * @return Remaining attempts.
     */
    private Long resolveRemainingAttempts(OtpEntity otp) {
        OtpPolicyEntity otpPolicy = otp.getOtpDefinition().getOtpPolicy();
        Long remainingAttempts = null;
        if (otpPolicy.getAttemptLimit() != null) {
            remainingAttempts = otpPolicy.getAttemptLimit() - otp.getFailedAttemptCounter();
        }
        if (otp.getOperation() != null) {
            OperationEntity operationEntity = otp.getOperation();
            Long remainingAttemptsOperation = stepResolutionService.getNumberOfRemainingAttempts(operationEntity);
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