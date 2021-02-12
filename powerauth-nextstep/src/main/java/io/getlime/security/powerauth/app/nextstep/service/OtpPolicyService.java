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

import io.getlime.security.powerauth.app.nextstep.repository.OtpPolicyRepository;
import io.getlime.security.powerauth.app.nextstep.repository.model.entity.OtpPolicyEntity;
import io.getlime.security.powerauth.lib.nextstep.model.entity.OtpPolicyDetail;
import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.OtpPolicyStatus;
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

    private final OtpPolicyRepository otpPolicyRepository;

    private final Logger logger = LoggerFactory.getLogger(OtpPolicyService.class);

    @Autowired
    public OtpPolicyService(OtpPolicyRepository otpPolicyRepository) {
        this.otpPolicyRepository = otpPolicyRepository;
    }

    @Transactional
    public CreateOtpPolicyResponse createOtpPolicy(CreateOtpPolicyRequest request) throws OtpPolicyAlreadyExistsException {
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
        response.setExpirationTime(request.getExpirationTime());
        return response;
    }

    @Transactional
    public UpdateOtpPolicyResponse updateOtpPolicy(UpdateOtpPolicyRequest request) throws OtpPolicyNotFoundException {
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
        response.setExpirationTime(otpPolicy.getExpirationTime());
        return response;
    }

    @Transactional
    public GetOtpPolicyListResponse getOtpPolicyList(GetOtpPolicyListRequest request) {
        Iterable<OtpPolicyEntity> otpPolicies;
        if (request.isIncludeRemoved()) {
            otpPolicies = otpPolicyRepository.findAll();
        } else {
            otpPolicies = otpPolicyRepository.findOtpPolicyByStatus(OtpPolicyStatus.ACTIVE);
        }
        GetOtpPolicyListResponse response = new GetOtpPolicyListResponse();
        for (OtpPolicyEntity otpPolicy: otpPolicies) {
            // TODO - use converter
            OtpPolicyDetail otpPolicyDetail = new OtpPolicyDetail();
            otpPolicyDetail.setOtpPolicyName(otpPolicy.getName());
            otpPolicyDetail.setDescription(otpPolicy.getDescription());
            otpPolicyDetail.setOtpPolicyStatus(otpPolicy.getStatus());
            otpPolicyDetail.setLength(otpPolicy.getLength());
            otpPolicyDetail.setAttemptLimit(otpPolicy.getAttemptLimit());
            otpPolicyDetail.setGenAlgorithm(otpPolicy.getGenAlgorithm());
            otpPolicyDetail.setExpirationTime(otpPolicy.getExpirationTime());
            otpPolicyDetail.setTimestampCreated(otpPolicy.getTimestampCreated());
            otpPolicyDetail.setTimestampLastUpdated(otpPolicy.getTimestampLastUpdated());
            response.getOtpPolicies().add(otpPolicyDetail);
        }
        return response;
    }

    @Transactional
    public DeleteOtpPolicyResponse deleteOtpPolicy(DeleteOtpPolicyRequest request) throws OtpPolicyNotFoundException {
        Optional<OtpPolicyEntity> otpPolicyOptional = otpPolicyRepository.findByName(request.getOtpPolicyName());
        if (!otpPolicyOptional.isPresent()) {
            throw new OtpPolicyNotFoundException("One time password policy not found: " + request.getOtpPolicyName());
        }
        OtpPolicyEntity otpPolicy = otpPolicyOptional.get();
        otpPolicy.setStatus(OtpPolicyStatus.REMOVED);
        otpPolicyRepository.save(otpPolicy);
        DeleteOtpPolicyResponse response = new DeleteOtpPolicyResponse();
        response.setOtpPolicyName(otpPolicy.getName());
        response.setOtpPolicyStatus(otpPolicy.getStatus());
        return response;
    }

}
