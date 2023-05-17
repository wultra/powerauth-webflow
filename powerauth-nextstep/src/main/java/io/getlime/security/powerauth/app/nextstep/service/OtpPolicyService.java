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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.wultra.core.audit.base.Audit;
import com.wultra.core.audit.base.model.AuditDetail;
import io.getlime.security.powerauth.app.nextstep.converter.OtpPolicyConverter;
import io.getlime.security.powerauth.app.nextstep.converter.ParameterConverter;
import io.getlime.security.powerauth.app.nextstep.repository.OtpPolicyRepository;
import io.getlime.security.powerauth.app.nextstep.repository.catalogue.RepositoryCatalogue;
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
import org.springframework.transaction.annotation.Transactional;

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
    private static final String AUDIT_TYPE_CONFIGURATION = "CONFIGURATION";

    private final OtpPolicyRepository otpPolicyRepository;
    private final Audit audit;

    private final OtpPolicyConverter otpPolicyConverter = new OtpPolicyConverter();
    private final ParameterConverter parameterConverter = new ParameterConverter();

    /**
     * Constructor for OTP policy service.
     * @param repositoryCatalogue Repository catalogue.
     * @param audit Audit interface.
     */
    @Autowired
    public OtpPolicyService(RepositoryCatalogue repositoryCatalogue, Audit audit) {
        this.otpPolicyRepository = repositoryCatalogue.getOtpPolicyRepository();
        this.audit = audit;
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
        final Optional<OtpPolicyEntity> otpPolicyOptional = otpPolicyRepository.findByName(request.getOtpPolicyName());
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
            otpPolicy.setGenParam(parameterConverter.fromObject(request.getGenParam()));
        } catch (JsonProcessingException ex) {
            throw new InvalidRequestException(ex);
        }
        otpPolicy.setExpirationTime(request.getExpirationTime());
        otpPolicy.setTimestampCreated(new Date());
        otpPolicy = otpPolicyRepository.save(otpPolicy);
        logger.debug("OTP policy was created, OTP policy ID: {}, OTP policy name: {}", otpPolicy.getOtpPolicyId(), otpPolicy.getName());
        audit.info("OTP policy was created", AuditDetail.builder()
                .type(AUDIT_TYPE_CONFIGURATION)
                .param("otpPolicy", otpPolicy)
                .build());
        final CreateOtpPolicyResponse response = new CreateOtpPolicyResponse();
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
        OtpPolicyEntity otpPolicy = otpPolicyRepository.findByName(request.getOtpPolicyName()).orElseThrow(() ->
                new OtpPolicyNotFoundException("One time password policy not found: " + request.getOtpPolicyName()));
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
        if (request.getGenAlgorithm() != null) {
            otpPolicy.setGenAlgorithm(request.getGenAlgorithm());
        }
        if (request.getGenParam() != null) {
            try {
                otpPolicy.setGenParam(parameterConverter.fromObject(request.getGenParam()));
            } catch (JsonProcessingException ex) {
                throw new InvalidRequestException(ex);
            }
        }
        otpPolicy.setExpirationTime(request.getExpirationTime());
        otpPolicy.setTimestampLastUpdated(new Date());
        otpPolicy = otpPolicyRepository.save(otpPolicy);
        logger.debug("OTP policy was updated, OTP policy ID: {}, OTP policy name: {}", otpPolicy.getOtpPolicyId(), otpPolicy.getName());
        audit.info("OTP policy was updated", AuditDetail.builder()
                .type(AUDIT_TYPE_CONFIGURATION)
                .param("otpPolicy", otpPolicy)
                .build());
        final UpdateOtpPolicyResponse response  = new UpdateOtpPolicyResponse();
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
        final Iterable<OtpPolicyEntity> otpPolicies;
        if (request.isIncludeRemoved()) {
            otpPolicies = otpPolicyRepository.findAll();
        } else {
            otpPolicies = otpPolicyRepository.findOtpPolicyByStatus(OtpPolicyStatus.ACTIVE);
        }
        final GetOtpPolicyListResponse response = new GetOtpPolicyListResponse();
        for (OtpPolicyEntity otpPolicy : otpPolicies) {
            final OtpPolicyDetail otpPolicyDetail = otpPolicyConverter.fromEntity(otpPolicy);
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
        OtpPolicyEntity otpPolicy = otpPolicyRepository.findByName(request.getOtpPolicyName()).orElseThrow(() ->
                new OtpPolicyNotFoundException("One time password policy not found: " + request.getOtpPolicyName()));
        if (otpPolicy.getStatus() == OtpPolicyStatus.REMOVED) {
            throw new OtpPolicyNotFoundException("One time password policy is already REMOVED: " + request.getOtpPolicyName());
        }
        otpPolicy.setStatus(OtpPolicyStatus.REMOVED);
        otpPolicy.setTimestampLastUpdated(new Date());
        otpPolicy = otpPolicyRepository.save(otpPolicy);
        logger.debug("OTP policy was removed, OTP policy ID: {}, OTP policy name: {}", otpPolicy.getOtpPolicyId(), otpPolicy.getName());
        audit.info("OTP policy was removed", AuditDetail.builder()
                .type(AUDIT_TYPE_CONFIGURATION)
                .param("otpPolicy", otpPolicy)
                .build());
        final DeleteOtpPolicyResponse response = new DeleteOtpPolicyResponse();
        response.setOtpPolicyName(otpPolicy.getName());
        response.setOtpPolicyStatus(otpPolicy.getStatus());
        return response;
    }

}
