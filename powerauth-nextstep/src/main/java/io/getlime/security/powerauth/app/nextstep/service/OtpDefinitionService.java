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

import io.getlime.security.powerauth.app.nextstep.converter.OtpDefinitionConverter;
import io.getlime.security.powerauth.app.nextstep.repository.ApplicationRepository;
import io.getlime.security.powerauth.app.nextstep.repository.OtpDefinitionRepository;
import io.getlime.security.powerauth.app.nextstep.repository.OtpPolicyRepository;
import io.getlime.security.powerauth.app.nextstep.repository.model.entity.ApplicationEntity;
import io.getlime.security.powerauth.app.nextstep.repository.model.entity.OtpDefinitionEntity;
import io.getlime.security.powerauth.app.nextstep.repository.model.entity.OtpPolicyEntity;
import io.getlime.security.powerauth.lib.nextstep.model.entity.OtpDefinitionDetail;
import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.ApplicationStatus;
import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.OtpDefinitionStatus;
import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.OtpPolicyStatus;
import io.getlime.security.powerauth.lib.nextstep.model.exception.ApplicationNotFoundException;
import io.getlime.security.powerauth.lib.nextstep.model.exception.OtpDefinitionAlreadyExistsException;
import io.getlime.security.powerauth.lib.nextstep.model.exception.OtpDefinitionNotFoundException;
import io.getlime.security.powerauth.lib.nextstep.model.exception.OtpPolicyNotFoundException;
import io.getlime.security.powerauth.lib.nextstep.model.request.CreateOtpDefinitionRequest;
import io.getlime.security.powerauth.lib.nextstep.model.request.DeleteOtpDefinitionRequest;
import io.getlime.security.powerauth.lib.nextstep.model.request.GetOtpDefinitionListRequest;
import io.getlime.security.powerauth.lib.nextstep.model.request.UpdateOtpDefinitionRequest;
import io.getlime.security.powerauth.lib.nextstep.model.response.CreateOtpDefinitionResponse;
import io.getlime.security.powerauth.lib.nextstep.model.response.DeleteOtpDefinitionResponse;
import io.getlime.security.powerauth.lib.nextstep.model.response.GetOtpDefinitionListResponse;
import io.getlime.security.powerauth.lib.nextstep.model.response.UpdateOtpDefinitionResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Date;
import java.util.Optional;

/**
 * This service handles persistence of one time password definitions.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Service
public class OtpDefinitionService {

    private final Logger logger = LoggerFactory.getLogger(OtpDefinitionService.class);

    private final OtpDefinitionRepository otpDefinitionRepository;
    private final OtpPolicyRepository otpPolicyRepository;
    private final ApplicationRepository applicationRepository;

    private final OtpDefinitionConverter otpDefinitionConverter = new OtpDefinitionConverter();

    /**
     * OTP definition service constructor.
     * @param otpDefinitionRepository OTP definition repository.
     * @param otpPolicyRepository OTP policy repository.
     * @param applicationRepository Application repository.
     */
    @Autowired
    public OtpDefinitionService(OtpDefinitionRepository otpDefinitionRepository, OtpPolicyRepository otpPolicyRepository, ApplicationRepository applicationRepository) {
        this.otpDefinitionRepository = otpDefinitionRepository;
        this.otpPolicyRepository = otpPolicyRepository;
        this.applicationRepository = applicationRepository;
    }

    /**
     * Create an OTP definition.
     * @param request Create OTP definition request.
     * @return Create OTP definition response.
     * @throws OtpDefinitionAlreadyExistsException Thrown when OTP definition already exists.
     * @throws ApplicationNotFoundException Thrown when application is not found.
     * @throws OtpPolicyNotFoundException Thrown when OTP policy is not found.
     */
    @Transactional
    public CreateOtpDefinitionResponse createOtpDefinition(CreateOtpDefinitionRequest request) throws OtpDefinitionAlreadyExistsException, ApplicationNotFoundException, OtpPolicyNotFoundException {
        Optional<OtpDefinitionEntity> otpDefinitionOptional = otpDefinitionRepository.findByName(request.getOtpDefinitionName());
        if (otpDefinitionOptional.isPresent()) {
            throw new OtpDefinitionAlreadyExistsException("One time password already exists: " + request.getOtpDefinitionName());
        }
        Optional<ApplicationEntity> applicationOptional = applicationRepository.findByName(request.getApplicationName());
        if (!applicationOptional.isPresent()) {
            throw new ApplicationNotFoundException("Application does not exist: " + request.getApplicationName());
        }
        ApplicationEntity application = applicationOptional.get();
        if (application.getStatus() != ApplicationStatus.ACTIVE) {
            throw new ApplicationNotFoundException("Application is not ACTIVE: " + request.getApplicationName());
        }
        Optional<OtpPolicyEntity> otpPolicyOptional = otpPolicyRepository.findByName(request.getOtpPolicyName());
        if (!otpPolicyOptional.isPresent()) {
            throw new OtpPolicyNotFoundException("Otp policy does not exist: " + request.getOtpPolicyName());
        }
        OtpPolicyEntity otpPolicy = otpPolicyOptional.get();
        if (otpPolicy.getStatus() != OtpPolicyStatus.ACTIVE) {
            throw new OtpPolicyNotFoundException("Otp policy is not ACTIVE: " + request.getOtpPolicyName());
        }
        OtpDefinitionEntity otpDefinition = new OtpDefinitionEntity();
        otpDefinition.setName(request.getOtpDefinitionName());
        otpDefinition.setDescription(request.getDescription());
        otpDefinition.setApplication(application);
        otpDefinition.setOtpPolicy(otpPolicy);
        otpDefinition.setEncryptionEnabled(request.isEncryptionEnabled());
        otpDefinition.setEncryptionAlgorithm(request.getEncryptionAlgorithm());
        otpDefinition.setStatus(OtpDefinitionStatus.ACTIVE);
        otpDefinition.setTimestampCreated(new Date());
        otpDefinitionRepository.save(otpDefinition);
        CreateOtpDefinitionResponse response = new CreateOtpDefinitionResponse();
        response.setOtpDefinitionName(otpDefinition.getName());
        response.setOtpDefinitionStatus(otpDefinition.getStatus());
        response.setApplicationName(otpDefinition.getApplication().getName());
        response.setOtpPolicyName(otpDefinition.getOtpPolicy().getName());
        response.setEncryptionEnabled(otpDefinition.isEncryptionEnabled());
        response.setEncryptionAlgorithm(otpDefinition.getEncryptionAlgorithm());
        return response;
    }

    /**
     * Update an OTP definition.
     * @param request Update OTP definition request.
     * @return Update OTP definition response.
     * @throws OtpDefinitionNotFoundException Thrown when OTP definition is not found.
     * @throws ApplicationNotFoundException Thrown when application is not found.
     * @throws OtpPolicyNotFoundException Thrown when OTP policy is not found.
     */
    @Transactional
    public UpdateOtpDefinitionResponse updateOtpDefinition(UpdateOtpDefinitionRequest request) throws OtpDefinitionNotFoundException, ApplicationNotFoundException, OtpPolicyNotFoundException {
        Optional<OtpDefinitionEntity> otpDefinitionOptional = otpDefinitionRepository.findByName(request.getOtpDefinitionName());
        if (!otpDefinitionOptional.isPresent()) {
            throw new OtpDefinitionNotFoundException("One time password definition not found: " + request.getOtpDefinitionName());
        }
        OtpDefinitionEntity otpDefinition = otpDefinitionOptional.get();
        if (otpDefinition.getStatus() != OtpDefinitionStatus.ACTIVE && request.getOtpDefinitionStatus() != OtpDefinitionStatus.ACTIVE) {
            throw new OtpDefinitionNotFoundException("One time password definition is not ACTIVE: " + request.getOtpDefinitionName());
        }
        Optional<ApplicationEntity> applicationOptional = applicationRepository.findByName(request.getApplicationName());
        if (!applicationOptional.isPresent()) {
            throw new ApplicationNotFoundException("Application does not exist: " + request.getApplicationName());
        }
        ApplicationEntity application = applicationOptional.get();
        if (application.getStatus() != ApplicationStatus.ACTIVE) {
            throw new ApplicationNotFoundException("Application is not ACTIVE: " + request.getApplicationName());
        }
        Optional<OtpPolicyEntity> otpPolicyOptional = otpPolicyRepository.findByName(request.getOtpPolicyName());
        if (!otpPolicyOptional.isPresent()) {
            throw new OtpPolicyNotFoundException("Otp policy does not exist: " + request.getOtpPolicyName());
        }
        OtpPolicyEntity otpPolicy = otpPolicyOptional.get();
        if (otpPolicy.getStatus() != OtpPolicyStatus.ACTIVE) {
            throw new OtpPolicyNotFoundException("Otp policy is not ACTIVE: " + request.getOtpPolicyName());
        }
        otpDefinition.setName(request.getOtpDefinitionName());
        otpDefinition.setDescription(request.getDescription());
        if (request.getOtpDefinitionStatus() != null) {
            otpDefinition.setStatus(request.getOtpDefinitionStatus());
        }
        otpDefinition.setApplication(application);
        otpDefinition.setOtpPolicy(otpPolicy);
        otpDefinition.setEncryptionEnabled(request.isEncryptionEnabled());
        otpDefinition.setEncryptionAlgorithm(request.getEncryptionAlgorithm());
        otpDefinition.setTimestampLastUpdated(new Date());
        otpDefinitionRepository.save(otpDefinition);
        UpdateOtpDefinitionResponse response  = new UpdateOtpDefinitionResponse();
        response.setOtpDefinitionName(otpDefinition.getName());
        response.setDescription(otpDefinition.getDescription());
        response.setOtpDefinitionStatus(otpDefinition.getStatus());
        response.setApplicationName(otpDefinition.getApplication().getName());
        response.setOtpPolicyName(otpDefinition.getOtpPolicy().getName());
        response.setEncryptionEnabled(otpDefinition.isEncryptionEnabled());
        response.setEncryptionAlgorithm(otpDefinition.getEncryptionAlgorithm());
        return response;
    }

    /**
     * Get OTP definition list.
     * @param request Get OTP definition list request.
     * @return Get OTP definition list response.
     */
    @Transactional
    public GetOtpDefinitionListResponse getOtpDefinitionList(GetOtpDefinitionListRequest request) {
        Iterable<OtpDefinitionEntity> otpDefinitions;
        if (request.isIncludeRemoved()) {
            otpDefinitions = otpDefinitionRepository.findAll();
        } else {
            otpDefinitions = otpDefinitionRepository.findOtpDefinitionByStatus(OtpDefinitionStatus.ACTIVE);
        }
        GetOtpDefinitionListResponse response = new GetOtpDefinitionListResponse();
        for (OtpDefinitionEntity otpDefinition : otpDefinitions) {
            OtpDefinitionDetail otpDefinitionDetail = otpDefinitionConverter.fromEntity(otpDefinition);
            response.getOtpDefinitions().add(otpDefinitionDetail);
        }
        return response;
    }

    /**
     * Delete an OTP definition.
     * @param request Delete OTP definition request.
     * @return Delete OTP definition response.
     * @throws OtpDefinitionNotFoundException Thrown when OTP definition is not found.
     */
    @Transactional
    public DeleteOtpDefinitionResponse deleteOtpDefinition(DeleteOtpDefinitionRequest request) throws OtpDefinitionNotFoundException {
        Optional<OtpDefinitionEntity> otpDefinitionOptional = otpDefinitionRepository.findByName(request.getOtpDefinitionName());
        if (!otpDefinitionOptional.isPresent()) {
            throw new OtpDefinitionNotFoundException("One time password definition not found: " + request.getOtpDefinitionName());
        }
        OtpDefinitionEntity otpDefinition = otpDefinitionOptional.get();
        if (otpDefinition.getStatus() == OtpDefinitionStatus.REMOVED) {
            throw new OtpDefinitionNotFoundException("One time password definition is already REMOVED: " + request.getOtpDefinitionName());
        }
        otpDefinition.setStatus(OtpDefinitionStatus.REMOVED);
        otpDefinitionRepository.save(otpDefinition);
        DeleteOtpDefinitionResponse response = new DeleteOtpDefinitionResponse();
        response.setOtpDefinitionName(otpDefinition.getName());
        response.setOtpDefinitionStatus(otpDefinition.getStatus());
        return response;
    }

    /**
     * Find an OTP definition. This method is not transactional.
     * @param otpDefinitionName OTP definition name.
     * @return OTP definition.
     * @throws OtpDefinitionNotFoundException Thrown when OTP definition is not found.
     */
    public OtpDefinitionEntity findActiveOtpDefinition(String otpDefinitionName) throws OtpDefinitionNotFoundException {
        Optional<OtpDefinitionEntity> otpDefinitionOptional = otpDefinitionRepository.findByName(otpDefinitionName);
        if (!otpDefinitionOptional.isPresent()) {
            throw new OtpDefinitionNotFoundException("OTP definition not found: " + otpDefinitionName);
        }
        OtpDefinitionEntity otpDefinition = otpDefinitionOptional.get();
        if (otpDefinition.getStatus() != OtpDefinitionStatus.ACTIVE) {
            throw new OtpDefinitionNotFoundException("OTP definition is not ACTIVE: " + otpDefinitionName);
        }
        return otpDefinition;
    }
}
