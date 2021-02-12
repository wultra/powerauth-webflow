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
import io.getlime.security.powerauth.app.nextstep.repository.CredentialDefinitionRepository;
import io.getlime.security.powerauth.app.nextstep.repository.CredentialPolicyRepository;
import io.getlime.security.powerauth.app.nextstep.repository.HashingConfigRepository;
import io.getlime.security.powerauth.app.nextstep.repository.model.entity.ApplicationEntity;
import io.getlime.security.powerauth.app.nextstep.repository.model.entity.CredentialDefinitionEntity;
import io.getlime.security.powerauth.app.nextstep.repository.model.entity.CredentialPolicyEntity;
import io.getlime.security.powerauth.app.nextstep.repository.model.entity.HashingConfigEntity;
import io.getlime.security.powerauth.lib.nextstep.model.entity.CredentialDefinitionDetail;
import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.ApplicationStatus;
import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.CredentialDefinitionStatus;
import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.CredentialPolicyStatus;
import io.getlime.security.powerauth.lib.nextstep.model.exception.*;
import io.getlime.security.powerauth.lib.nextstep.model.request.CreateCredentialDefinitionRequest;
import io.getlime.security.powerauth.lib.nextstep.model.request.DeleteCredentialDefinitionRequest;
import io.getlime.security.powerauth.lib.nextstep.model.request.GetCredentialDefinitionListRequest;
import io.getlime.security.powerauth.lib.nextstep.model.request.UpdateCredentialDefinitionRequest;
import io.getlime.security.powerauth.lib.nextstep.model.response.CreateCredentialDefinitionResponse;
import io.getlime.security.powerauth.lib.nextstep.model.response.DeleteCredentialDefinitionResponse;
import io.getlime.security.powerauth.lib.nextstep.model.response.GetCredentialDefinitionListResponse;
import io.getlime.security.powerauth.lib.nextstep.model.response.UpdateCredentialDefinitionResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Date;
import java.util.Optional;

/**
 * This service handles persistence of credential definitions.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Service
public class CredentialDefinitionService {

    private final CredentialDefinitionRepository credentialDefinitionRepository;
    private final CredentialPolicyRepository credentialPolicyRepository;
    private final ApplicationRepository applicationRepository;
    private final HashingConfigRepository hashingConfigRepository;

    private final Logger logger = LoggerFactory.getLogger(CredentialDefinitionService.class);

    @Autowired
    public CredentialDefinitionService(CredentialDefinitionRepository credentialDefinitionRepository, CredentialPolicyRepository credentialPolicyRepository, ApplicationRepository applicationRepository, HashingConfigRepository hashingConfigRepository) {
        this.credentialDefinitionRepository = credentialDefinitionRepository;
        this.credentialPolicyRepository = credentialPolicyRepository;
        this.applicationRepository = applicationRepository;
        this.hashingConfigRepository = hashingConfigRepository;
    }

    @Transactional
    public CreateCredentialDefinitionResponse createCredentialDefinition(CreateCredentialDefinitionRequest request) throws CredentialDefinitionAlreadyExistsException, ApplicationNotFoundException, CredentialPolicyNotFoundException, HashingConfigNotFoundException {
        Optional<CredentialDefinitionEntity> credentialDefinitionOptional = credentialDefinitionRepository.findByName(request.getCredentialDefinitionName());
        if (credentialDefinitionOptional.isPresent()) {
            throw new CredentialDefinitionAlreadyExistsException("Credential definition already exists: " + request.getCredentialDefinitionName());
        }
        Optional<ApplicationEntity> applicationOptional = applicationRepository.findByName(request.getApplicationName());
        if (!applicationOptional.isPresent()) {
            throw new ApplicationNotFoundException("Application does not exist: " + request.getApplicationName());
        }
        ApplicationEntity application = applicationOptional.get();
        if (application.getStatus() != ApplicationStatus.ACTIVE) {
            throw new ApplicationNotFoundException("Application is not ACTIVE: " + request.getApplicationName());
        }
        Optional<CredentialPolicyEntity> credentialPolicyOptional = credentialPolicyRepository.findByName(request.getCredentialPolicyName());
        if (!credentialPolicyOptional.isPresent()) {
            throw new CredentialPolicyNotFoundException("Credential policy does not exist: " + request.getCredentialPolicyName());
        }
        CredentialPolicyEntity credentialPolicy = credentialPolicyOptional.get();
        if (credentialPolicy.getStatus() != CredentialPolicyStatus.ACTIVE) {
            throw new CredentialPolicyNotFoundException("Credential policy is not ACTIVE: " + request.getCredentialPolicyName());
        }
        HashingConfigEntity hashConfigEntity = null;
        if (request.isHashingEnabled() && request.getHashConfigName() != null) {
            Optional<HashingConfigEntity> hashConfigOptional = hashingConfigRepository.findByName(request.getHashConfigName());
            if (!hashConfigOptional.isPresent()) {
                throw new HashingConfigNotFoundException("Hashing configuration does not exist: " + request.getHashConfigName());
            }
            hashConfigEntity = hashConfigOptional.get();
        }
        CredentialDefinitionEntity credentialDefinition = new CredentialDefinitionEntity();
        credentialDefinition.setName(request.getCredentialDefinitionName());
        credentialDefinition.setApplication(application);
        credentialDefinition.setCredentialPolicy(credentialPolicy);
        credentialDefinition.setCategory(request.getCategory());
        credentialDefinition.setEncryptionEnabled(request.isEncryptionEnabled());
        credentialDefinition.setEncryptionAlgorithm(request.getEncryptionAlgorithm());
        if (hashConfigEntity != null) {
            credentialDefinition.setHashingConfig(hashConfigEntity);
        }
        credentialDefinition.setE2eEncryptionEnabled(request.isE2eEncryptionEnabled());
        credentialDefinition.setStatus(CredentialDefinitionStatus.ACTIVE);
        credentialDefinition.setTimestampCreated(new Date());
        credentialDefinitionRepository.save(credentialDefinition);
        CreateCredentialDefinitionResponse response = new CreateCredentialDefinitionResponse();
        response.setCredentialDefinitionName(credentialDefinition.getName());
        response.setCredentialDefinitionStatus(credentialDefinition.getStatus());
        response.setApplicationName(credentialDefinition.getApplication().getName());
        response.setCredentialPolicyName(credentialDefinition.getCredentialPolicy().getName());
        response.setCategory(credentialDefinition.getCategory());
        response.setEncryptionEnabled(credentialDefinition.isEncryptionEnabled());
        response.setEncryptionAlgorithm(credentialDefinition.getEncryptionAlgorithm());
        if (credentialDefinition.getHashingConfig() != null) {
            response.setHashingEnabled(true);
            response.setHashConfigName(credentialDefinition.getHashingConfig().getName());
        }
        response.setE2eEncryptionEnabled(credentialDefinition.isE2eEncryptionEnabled());
        return response;
    }

    @Transactional
    public UpdateCredentialDefinitionResponse updateCredentialDefinition(UpdateCredentialDefinitionRequest request) throws CredentialDefinitionNotFoundException, ApplicationNotFoundException, CredentialPolicyNotFoundException, HashingConfigNotFoundException {
        Optional<CredentialDefinitionEntity> credentialDefinitionOptional = credentialDefinitionRepository.findByName(request.getCredentialDefinitionName());
        if (!credentialDefinitionOptional.isPresent()) {
            throw new CredentialDefinitionNotFoundException("Credential definition not found: " + request.getCredentialDefinitionName());
        }
        CredentialDefinitionEntity credentialDefinition = credentialDefinitionOptional.get();
        if (credentialDefinition.getStatus() != CredentialDefinitionStatus.ACTIVE && request.getCredentialDefinitionStatus() != CredentialDefinitionStatus.ACTIVE) {
            throw new CredentialDefinitionNotFoundException("Credential definition is not ACTIVE: " + request.getCredentialDefinitionName());
        }
        Optional<ApplicationEntity> applicationOptional = applicationRepository.findByName(request.getApplicationName());
        if (!applicationOptional.isPresent()) {
            throw new ApplicationNotFoundException("Application does not exist: " + request.getApplicationName());
        }
        ApplicationEntity application = applicationOptional.get();
        if (application.getStatus() != ApplicationStatus.ACTIVE) {
            throw new ApplicationNotFoundException("Application is not ACTIVE: " + request.getApplicationName());
        }
        Optional<CredentialPolicyEntity> credentialPolicyOptional = credentialPolicyRepository.findByName(request.getCredentialPolicyName());
        if (!credentialPolicyOptional.isPresent()) {
            throw new CredentialPolicyNotFoundException("Credential policy does not exist: " + request.getCredentialPolicyName());
        }
        CredentialPolicyEntity credentialPolicy = credentialPolicyOptional.get();
        if (credentialPolicy.getStatus() != CredentialPolicyStatus.ACTIVE) {
            throw new CredentialPolicyNotFoundException("Credential policy is not ACTIVE: " + request.getCredentialPolicyName());
        }
        HashingConfigEntity hashConfigEntity = null;
        if (request.isHashingEnabled() && request.getHashConfigName() != null) {
            Optional<HashingConfigEntity> hashConfigOptional = hashingConfigRepository.findByName(request.getHashConfigName());
            if (!hashConfigOptional.isPresent()) {
                throw new HashingConfigNotFoundException("Hashing configuration does not exist: " + request.getHashConfigName());
            }
            hashConfigEntity = hashConfigOptional.get();
        }
        credentialDefinition.setName(request.getCredentialDefinitionName());
        if (request.getCredentialDefinitionStatus() != null) {
            credentialDefinition.setStatus(request.getCredentialDefinitionStatus());
        }
        credentialDefinition.setApplication(application);
        credentialDefinition.setCredentialPolicy(credentialPolicy);
        credentialDefinition.setCategory(request.getCategory());
        credentialDefinition.setEncryptionEnabled(request.isEncryptionEnabled());
        credentialDefinition.setEncryptionAlgorithm(request.getEncryptionAlgorithm());
        if (hashConfigEntity != null) {
            credentialDefinition.setHashingConfig(hashConfigEntity);
        }
        credentialDefinition.setE2eEncryptionEnabled(request.isE2eEncryptionEnabled());
        credentialDefinition.setTimestampLastUpdated(new Date());
        credentialDefinitionRepository.save(credentialDefinition);
        UpdateCredentialDefinitionResponse response  = new UpdateCredentialDefinitionResponse();
        response.setCredentialDefinitionName(credentialDefinition.getName());
        response.setCredentialDefinitionStatus(credentialDefinition.getStatus());
        response.setApplicationName(credentialDefinition.getApplication().getName());
        response.setCredentialPolicyName(credentialDefinition.getCredentialPolicy().getName());
        response.setCategory(credentialDefinition.getCategory());
        response.setEncryptionEnabled(credentialDefinition.isEncryptionEnabled());
        response.setEncryptionAlgorithm(credentialDefinition.getEncryptionAlgorithm());
        if (credentialDefinition.getHashingConfig() != null) {
            response.setHashingEnabled(true);
            response.setHashConfigName(credentialDefinition.getHashingConfig().getName());
        }
        response.setE2eEncryptionEnabled(credentialDefinition.isE2eEncryptionEnabled());
        return response;
    }

    @Transactional
    public GetCredentialDefinitionListResponse getCredentialDefinitionList(GetCredentialDefinitionListRequest request) {
        Iterable<CredentialDefinitionEntity> credentialDefinitions;
        if (request.isIncludeRemoved()) {
            credentialDefinitions = credentialDefinitionRepository.findAll();
        } else {
            credentialDefinitions = credentialDefinitionRepository.findCredentialDefinitionByStatus(CredentialDefinitionStatus.ACTIVE);
        }
        GetCredentialDefinitionListResponse response = new GetCredentialDefinitionListResponse();
        for (CredentialDefinitionEntity credentialDefinition: credentialDefinitions) {
            // TODO - use converter
            CredentialDefinitionDetail credentialDefinitionDetail = new CredentialDefinitionDetail();
            credentialDefinitionDetail.setCredentialDefinitionName(credentialDefinition.getName());
            credentialDefinitionDetail.setCredentialDefinitionStatus(credentialDefinition.getStatus());
            credentialDefinitionDetail.setApplicationName(credentialDefinition.getApplication().getName());
            credentialDefinitionDetail.setCredentialPolicyName(credentialDefinition.getCredentialPolicy().getName());
            credentialDefinitionDetail.setCategory(credentialDefinition.getCategory());
            credentialDefinitionDetail.setEncryptionEnabled(credentialDefinition.isEncryptionEnabled());
            credentialDefinitionDetail.setEncryptionAlgorithm(credentialDefinition.getEncryptionAlgorithm());
            if (credentialDefinition.getHashingConfig() != null) {
                credentialDefinitionDetail.setHashingEnabled(true);
                credentialDefinitionDetail.setHashConfigName(credentialDefinition.getHashingConfig().getName());
            }
            credentialDefinitionDetail.setE2eEncryptionEnabled(credentialDefinition.isE2eEncryptionEnabled());
            credentialDefinitionDetail.setTimestampCreated(credentialDefinition.getTimestampCreated());
            credentialDefinitionDetail.setTimestampLastUpdated(credentialDefinition.getTimestampLastUpdated());
            response.getCredentialDefinitions().add(credentialDefinitionDetail);
        }
        return response;
    }

    @Transactional
    public DeleteCredentialDefinitionResponse deleteCredentialDefinition(DeleteCredentialDefinitionRequest request) throws CredentialDefinitionNotFoundException {
        Optional<CredentialDefinitionEntity> credentialDefinitionOptional = credentialDefinitionRepository.findByName(request.getCredentialDefinitionName());
        if (!credentialDefinitionOptional.isPresent()) {
            throw new CredentialDefinitionNotFoundException("Credential definition not found: " + request.getCredentialDefinitionName());
        }
        CredentialDefinitionEntity credentialDefinition = credentialDefinitionOptional.get();
        credentialDefinition.setStatus(CredentialDefinitionStatus.REMOVED);
        credentialDefinitionRepository.save(credentialDefinition);
        DeleteCredentialDefinitionResponse response = new DeleteCredentialDefinitionResponse();
        response.setCredentialDefinitionName(credentialDefinition.getName());
        response.setCredentialDefinitionStatus(credentialDefinition.getStatus());
        return response;
    }

}
