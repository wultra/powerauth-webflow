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

    private final OtpDefinitionRepository otpDefinitionRepository;
    private final OtpPolicyRepository otpPolicyRepository;
    private final ApplicationRepository applicationRepository;

    private final Logger logger = LoggerFactory.getLogger(OtpDefinitionService.class);

    @Autowired
    public OtpDefinitionService(OtpDefinitionRepository otpDefinitionRepository, OtpPolicyRepository otpPolicyRepository, ApplicationRepository applicationRepository) {
        this.otpDefinitionRepository = otpDefinitionRepository;
        this.otpPolicyRepository = otpPolicyRepository;
        this.applicationRepository = applicationRepository;
    }

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

    @Transactional
    public UpdateOtpDefinitionResponse updateOtpDefinition(UpdateOtpDefinitionRequest request) throws OtpDefinitionNotFoundException, ApplicationNotFoundException, OtpPolicyNotFoundException {
        Optional<OtpDefinitionEntity> otpDefinitionOptional = otpDefinitionRepository.findByName(request.getOtpDefinitionName());
        if (!otpDefinitionOptional.isPresent()) {
            throw new OtpDefinitionNotFoundException("One time password not found: " + request.getOtpDefinitionName());
        }
        OtpDefinitionEntity otpDefinition = otpDefinitionOptional.get();
        if (otpDefinition.getStatus() != OtpDefinitionStatus.ACTIVE) {
            throw new OtpDefinitionNotFoundException("One time password is not ACTIVE: " + request.getOtpDefinitionName());
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
        otpDefinition.setStatus(OtpDefinitionStatus.ACTIVE);
        otpDefinition.setApplication(application);
        otpDefinition.setOtpPolicy(otpPolicy);
        otpDefinition.setEncryptionEnabled(request.isEncryptionEnabled());
        otpDefinition.setEncryptionAlgorithm(request.getEncryptionAlgorithm());
        otpDefinition.setStatus(OtpDefinitionStatus.ACTIVE);
        otpDefinition.setTimestampLastUpdated(new Date());
        otpDefinitionRepository.save(otpDefinition);
        UpdateOtpDefinitionResponse response  = new UpdateOtpDefinitionResponse();
        response.setOtpDefinitionName(otpDefinition.getName());
        response.setOtpDefinitionStatus(otpDefinition.getStatus());
        response.setApplicationName(otpDefinition.getApplication().getName());
        response.setOtpPolicyName(otpDefinition.getOtpPolicy().getName());
        response.setEncryptionEnabled(otpDefinition.isEncryptionEnabled());
        response.setEncryptionAlgorithm(otpDefinition.getEncryptionAlgorithm());
        return response;
    }

    @Transactional
    public GetOtpDefinitionListResponse getOtpDefinitionList(GetOtpDefinitionListRequest request) {
        Iterable<OtpDefinitionEntity> otpPolicies = otpDefinitionRepository.findOtpDefinitionByStatus(OtpDefinitionStatus.ACTIVE);
        GetOtpDefinitionListResponse response = new GetOtpDefinitionListResponse();
        for (OtpDefinitionEntity otpDefinition: otpPolicies) {
            // TODO - use converter
            OtpDefinitionDetail otpDefinitionDetail = new OtpDefinitionDetail();
            otpDefinitionDetail.setOtpDefinitionName(otpDefinition.getName());
            otpDefinitionDetail.setOtpDefinitionStatus(otpDefinition.getStatus());
            otpDefinitionDetail.setApplicationName(otpDefinition.getApplication().getName());
            otpDefinitionDetail.setOtpPolicyName(otpDefinition.getOtpPolicy().getName());
            otpDefinitionDetail.setEncryptionEnabled(otpDefinition.isEncryptionEnabled());
            otpDefinitionDetail.setEncryptionAlgorithm(otpDefinition.getEncryptionAlgorithm());
            response.getOtpDefinitions().add(otpDefinitionDetail);
        }
        return response;
    }

    @Transactional
    public DeleteOtpDefinitionResponse deleteOtpDefinition(DeleteOtpDefinitionRequest request) throws OtpDefinitionNotFoundException {
        Optional<OtpDefinitionEntity> otpDefinitionOptional = otpDefinitionRepository.findByName(request.getOtpDefinitionName());
        if (!otpDefinitionOptional.isPresent()) {
            throw new OtpDefinitionNotFoundException("One time password not found: " + request.getOtpDefinitionName());
        }
        OtpDefinitionEntity otpDefinition = otpDefinitionOptional.get();
        otpDefinition.setStatus(OtpDefinitionStatus.REMOVED);
        otpDefinitionRepository.save(otpDefinition);
        DeleteOtpDefinitionResponse response = new DeleteOtpDefinitionResponse();
        response.setOtpDefinitionName(otpDefinition.getName());
        response.setOtpDefinitionStatus(otpDefinition.getStatus());
        return response;
    }

}
