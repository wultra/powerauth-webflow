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
import io.getlime.security.powerauth.app.nextstep.converter.OtpPolicyConverter;
import io.getlime.security.powerauth.app.nextstep.converter.ParameterConverter;
import io.getlime.security.powerauth.app.nextstep.repository.OtpPolicyRepository;
import io.getlime.security.powerauth.app.nextstep.repository.model.entity.OtpPolicyEntity;
import io.getlime.security.powerauth.lib.nextstep.model.entity.OtpPolicyDetail;
import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.OtpPolicyStatus;
import io.getlime.security.powerauth.lib.nextstep.model.exception.InvalidConfigurationException;
import io.getlime.security.powerauth.lib.nextstep.model.exception.InvalidRequestException;
import io.getlime.security.powerauth.lib.nextstep.model.exception.OtpPolicyAlreadyExistsException;
import io.getlime.security.powerauth.lib.nextstep.model.exception.OtpPolicyNotFoundException;
import io.getlime.security.powerauth.lib.nextstep.model.request.CreateOtpPolicyRequest;
import io.getlime.security.powerauth.lib.nextstep.model.request.DeleteOtpPolicyRequest;
import io.getlime.security.powerauth.lib.nextstep.model.request.GetOtpPolicyListRequest;
import io.getlime.security.powerauth.lib.nextstep.model.request.UpdateOtpPolicyRequest;
import io.getlime.security.powerauth.lib.nextstep.model.response.CreateOtpPolicyResponse;
import io.getlime.security.powerauth.lib.nextstep.model.response.DeleteOtpPolicyResponse;
import io.getlime.security.powerauth.lib.nextstep.model.response.GetOtpPolicyListResponse;
import io.getlime.security.powerauth.lib.nextstep.model.response.UpdateOtpPolicyResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Date;
import java.util.Optional;

/**
 * This service handles persistence of one time password policies.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Service
public class OtpPolicyService {

    private final Logger logger = LoggerFactory.getLogger(OtpPolicyService.class);

    private final OtpPolicyRepository otpPolicyRepository;

    private final OtpPolicyConverter otpPolicyConverter = new OtpPolicyConverter();
    private final ParameterConverter parameterConverter = new ParameterConverter();

    /**
     * Constructor for OTP policy service.
     * @param otpPolicyRepository OTP policy repository.
     */
    @Autowired
    public OtpPolicyService(OtpPolicyRepository otpPolicyRepository) {
        this.otpPolicyRepository = otpPolicyRepository;
    }

    /**
     * Create an OTP policy.
     * @param request Create OTP policy request.
     * @return Create OTP policy response.
     * @throws OtpPolicyAlreadyExistsException Thrown when OTP policy already exists.
     * @throws InvalidRequestException Thrown when request is invalid.
     */
    @Transactional
    public CreateOtpPolicyResponse createOtpPolicy(CreateOtpPolicyRequest request) throws OtpPolicyAlreadyExistsException, InvalidRequestException {
        Optional<OtpPolicyEntity> otpPolicyOptional = otpPolicyRepository.findByName(request.getOtpPolicyName());
        if (otpPolicyOptional.isPresent()) {
            throw new OtpPolicyAlreadyExistsException("One time password policy already exists: " + request.getOtpPolicyName());
        }
        OtpPolicyEntity otpPolicy = new OtpPolicyEntity();
        otpPolicy.setName(request.getOtpPolicyName());
        otpPolicy.setDescription(request.getDescription());
        otpPolicy.setStatus(OtpPolicyStatus.ACTIVE);
        otpPolicy.setLength(request.getLength());
        otpPolicy.setAttemptLimit(request.getAttemptLimit());
        otpPolicy.setGenAlgorithm(request.getGenAlgorithm());
        try {
            otpPolicy.setGenParam(parameterConverter.fromMap(request.getGenParam()));
        } catch (JsonProcessingException ex) {
            throw new InvalidRequestException(ex);
        }
        otpPolicy.setExpirationTime(request.getExpirationTime());
        otpPolicy.setTimestampCreated(new Date());
        otpPolicyRepository.save(otpPolicy);
        CreateOtpPolicyResponse response = new CreateOtpPolicyResponse();
        response.setOtpPolicyName(otpPolicy.getName());
        response.setDescription(otpPolicy.getDescription());
        response.setOtpPolicyStatus(otpPolicy.getStatus());
        response.setLength(request.getLength());
        response.setAttemptLimit(request.getAttemptLimit());
        response.setGenAlgorithm(request.getGenAlgorithm());
        response.setGenParam(request.getGenParam());
        response.setExpirationTime(request.getExpirationTime());
        return response;
    }

    /**
     * Update an OTP policy.
     * @param request Update OTP policy request.
     * @return Update OTP policy response.
     * @throws OtpPolicyNotFoundException Thrown when OTP policy does not exist.
     * @throws InvalidRequestException Thrown when request is invalid.
     */
    @Transactional
    public UpdateOtpPolicyResponse updateOtpPolicy(UpdateOtpPolicyRequest request) throws OtpPolicyNotFoundException, InvalidRequestException {
        Optional<OtpPolicyEntity> otpPolicyOptional = otpPolicyRepository.findByName(request.getOtpPolicyName());
        if (!otpPolicyOptional.isPresent()) {
            throw new OtpPolicyNotFoundException("One time password policy not found: " + request.getOtpPolicyName());
        }
        OtpPolicyEntity otpPolicy = otpPolicyOptional.get();
        if (otpPolicy.getStatus() != OtpPolicyStatus.ACTIVE && request.getOtpPolicyStatus() != OtpPolicyStatus.ACTIVE) {
            throw new OtpPolicyNotFoundException("One time password policy is not ACTIVE: " + request.getOtpPolicyName());
        }
        otpPolicy.setName(request.getOtpPolicyName());
        otpPolicy.setDescription(request.getDescription());
        if (request.getOtpPolicyStatus() != null) {
            otpPolicy.setStatus(request.getOtpPolicyStatus());
        }
        otpPolicy.setLength(request.getLength());
        otpPolicy.setAttemptLimit(request.getAttemptLimit());
        otpPolicy.setGenAlgorithm(request.getGenAlgorithm());
        if (request.getGenParam() != null) {
            try {
                otpPolicy.setGenParam(parameterConverter.fromMap(request.getGenParam()));
            } catch (JsonProcessingException ex) {
                throw new InvalidRequestException(ex);
            }
        }
        otpPolicy.setExpirationTime(request.getExpirationTime());
        otpPolicy.setTimestampLastUpdated(new Date());
        otpPolicyRepository.save(otpPolicy);
        UpdateOtpPolicyResponse response  = new UpdateOtpPolicyResponse();
        response.setOtpPolicyName(otpPolicy.getName());
        response.setDescription(otpPolicy.getDescription());
        response.setOtpPolicyStatus(otpPolicy.getStatus());
        response.setLength(otpPolicy.getLength());
        response.setAttemptLimit(otpPolicy.getAttemptLimit());
        response.setGenAlgorithm(otpPolicy.getGenAlgorithm());
        response.setGenParam(request.getGenParam());
        response.setExpirationTime(otpPolicy.getExpirationTime());
        return response;
    }

    /**
     * Get OTP policy list.
     * @param request Get OTP policy list request.
     * @return Get OTP policy list response.
     * @throws InvalidConfigurationException Thrown when Next Step configuration is invalid.
     */
    @Transactional
    public GetOtpPolicyListResponse getOtpPolicyList(GetOtpPolicyListRequest request) throws InvalidConfigurationException {
        Iterable<OtpPolicyEntity> otpPolicies;
        if (request.isIncludeRemoved()) {
            otpPolicies = otpPolicyRepository.findAll();
        } else {
            otpPolicies = otpPolicyRepository.findOtpPolicyByStatus(OtpPolicyStatus.ACTIVE);
        }
        GetOtpPolicyListResponse response = new GetOtpPolicyListResponse();
        for (OtpPolicyEntity otpPolicy : otpPolicies) {
            OtpPolicyDetail otpPolicyDetail = otpPolicyConverter.fromEntity(otpPolicy);
            response.getOtpPolicies().add(otpPolicyDetail);
        }
        return response;
    }

    /**
     * Delete an OTP policy.
     * @param request Delete OTP policy request.
     * @return Delete OTP policy response.
     * @throws OtpPolicyNotFoundException Thrown when OTP policy is not found.
     */
    @Transactional
    public DeleteOtpPolicyResponse deleteOtpPolicy(DeleteOtpPolicyRequest request) throws OtpPolicyNotFoundException {
        Optional<OtpPolicyEntity> otpPolicyOptional = otpPolicyRepository.findByName(request.getOtpPolicyName());
        if (!otpPolicyOptional.isPresent()) {
            throw new OtpPolicyNotFoundException("One time password policy not found: " + request.getOtpPolicyName());
        }
        OtpPolicyEntity otpPolicy = otpPolicyOptional.get();
        if (otpPolicy.getStatus() == OtpPolicyStatus.REMOVED) {
            throw new OtpPolicyNotFoundException("One time password policy is already REMOVED: " + request.getOtpPolicyName());
        }
        otpPolicy.setStatus(OtpPolicyStatus.REMOVED);
        otpPolicyRepository.save(otpPolicy);
        DeleteOtpPolicyResponse response = new DeleteOtpPolicyResponse();
        response.setOtpPolicyName(otpPolicy.getName());
        response.setOtpPolicyStatus(otpPolicy.getStatus());
        return response;
    }

}
