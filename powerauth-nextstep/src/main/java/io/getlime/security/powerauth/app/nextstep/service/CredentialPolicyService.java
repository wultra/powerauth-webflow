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

import io.getlime.security.powerauth.app.nextstep.converter.CredentialPolicyConverter;
import io.getlime.security.powerauth.app.nextstep.repository.CredentialPolicyRepository;
import io.getlime.security.powerauth.app.nextstep.repository.model.entity.CredentialPolicyEntity;
import io.getlime.security.powerauth.lib.nextstep.model.entity.CredentialPolicyDetail;
import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.CredentialPolicyStatus;
import io.getlime.security.powerauth.lib.nextstep.model.exception.CredentialPolicyAlreadyExistsException;
import io.getlime.security.powerauth.lib.nextstep.model.exception.CredentialPolicyNotFoundException;
import io.getlime.security.powerauth.lib.nextstep.model.request.CreateCredentialPolicyRequest;
import io.getlime.security.powerauth.lib.nextstep.model.request.DeleteCredentialPolicyRequest;
import io.getlime.security.powerauth.lib.nextstep.model.request.GetCredentialPolicyListRequest;
import io.getlime.security.powerauth.lib.nextstep.model.request.UpdateCredentialPolicyRequest;
import io.getlime.security.powerauth.lib.nextstep.model.response.CreateCredentialPolicyResponse;
import io.getlime.security.powerauth.lib.nextstep.model.response.DeleteCredentialPolicyResponse;
import io.getlime.security.powerauth.lib.nextstep.model.response.GetCredentialPolicyListResponse;
import io.getlime.security.powerauth.lib.nextstep.model.response.UpdateCredentialPolicyResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Date;
import java.util.Optional;

/**
 * This service handles persistence of credential policies.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Service
public class CredentialPolicyService {

    private final Logger logger = LoggerFactory.getLogger(CredentialPolicyService.class);

    private final CredentialPolicyRepository credentialPolicyRepository;

    private final CredentialPolicyConverter credentialPolicyConverter = new CredentialPolicyConverter();

    /**
     * Credential policy service constructor.
     * @param credentialPolicyRepository Credential policy repository.
     */
    @Autowired
    public CredentialPolicyService(CredentialPolicyRepository credentialPolicyRepository) {
        this.credentialPolicyRepository = credentialPolicyRepository;
    }

    /**
     * Create a credential policy.
     * @param request Create credential policy request.
     * @return Create credential policy response.
     * @throws CredentialPolicyAlreadyExistsException Thrown when credential policy already exists.
     */
    @Transactional
    public CreateCredentialPolicyResponse createCredentialPolicy(CreateCredentialPolicyRequest request) throws CredentialPolicyAlreadyExistsException {
        Optional<CredentialPolicyEntity> credentialPolicyOptional = credentialPolicyRepository.findByName(request.getCredentialPolicyName());
        if (credentialPolicyOptional.isPresent()) {
            throw new CredentialPolicyAlreadyExistsException("Credential policy already exists: " + request.getCredentialPolicyName());
        }
        CredentialPolicyEntity credentialPolicy = new CredentialPolicyEntity();
        credentialPolicy.setName(request.getCredentialPolicyName());
        credentialPolicy.setDescription(request.getDescription());
        credentialPolicy.setStatus(CredentialPolicyStatus.ACTIVE);
        credentialPolicy.setUsernameLengthMin(request.getUsernameLengthMin());
        credentialPolicy.setUsernameLengthMax(request.getUsernameLengthMax());
        credentialPolicy.setUsernameAllowedChars(request.getUsernameAllowedChars());
        credentialPolicy.setCredentialLengthMin(request.getCredentialLengthMin());
        credentialPolicy.setCredentialLengthMax(request.getCredentialLengthMax());
        credentialPolicy.setCredentialAllowedChars(request.getCredentialAllowedChars());
        credentialPolicy.setLimitSoft(request.getLimitSoft());
        credentialPolicy.setLimitHard(request.getLimitHard());
        credentialPolicy.setCheckHistoryCount(request.getCheckHistoryCount());
        credentialPolicy.setRotationEnabled(request.isRotationEnabled());
        credentialPolicy.setRotationDays(request.getRotationDays());
        credentialPolicy.setUsernameGenAlgorithm(request.getUsernameGenAlgorithm());
        credentialPolicy.setCredentialGenAlgorithm(request.getCredentialGenAlgorithm());
        credentialPolicy.setTimestampCreated(new Date());
        credentialPolicyRepository.save(credentialPolicy);
        CreateCredentialPolicyResponse response = new CreateCredentialPolicyResponse();
        response.setCredentialPolicyName(credentialPolicy.getName());
        response.setDescription(credentialPolicy.getDescription());
        response.setCredentialPolicyStatus(credentialPolicy.getStatus());
        response.setUsernameLengthMin(credentialPolicy.getUsernameLengthMin());
        response.setUsernameLengthMax(credentialPolicy.getUsernameLengthMax());
        response.setUsernameAllowedChars(credentialPolicy.getUsernameAllowedChars());
        response.setCredentialLengthMin(credentialPolicy.getCredentialLengthMin());
        response.setCredentialLengthMax(credentialPolicy.getCredentialLengthMax());
        response.setCredentialAllowedChars(credentialPolicy.getCredentialAllowedChars());
        response.setLimitSoft(request.getLimitSoft());
        response.setLimitHard(request.getLimitHard());
        response.setCheckHistoryCount(request.getCheckHistoryCount());
        response.setRotationEnabled(request.isRotationEnabled());
        response.setRotationDays(request.getRotationDays());
        response.setUsernameGenAlgorithm(request.getUsernameGenAlgorithm());
        response.setCredentialGenAlgorithm(request.getCredentialGenAlgorithm());
        return response;
    }

    /**
     * Update a credential policy.
     * @param request Update credential policy request.
     * @return Update credential policy response.
     * @throws CredentialPolicyNotFoundException Thrown when credential policy is not found.
     */
    @Transactional
    public UpdateCredentialPolicyResponse updateCredentialPolicy(UpdateCredentialPolicyRequest request) throws CredentialPolicyNotFoundException {
        Optional<CredentialPolicyEntity> credentialPolicyOptional = credentialPolicyRepository.findByName(request.getCredentialPolicyName());
        if (!credentialPolicyOptional.isPresent()) {
            throw new CredentialPolicyNotFoundException("Credential policy not found: " + request.getCredentialPolicyName());
        }
        CredentialPolicyEntity credentialPolicy = credentialPolicyOptional.get();
        if (credentialPolicy.getStatus() != CredentialPolicyStatus.ACTIVE && request.getCredentialPolicyStatus() != CredentialPolicyStatus.ACTIVE) {
            throw new CredentialPolicyNotFoundException("Credential policy is not ACTIVE: " + request.getCredentialPolicyName());
        }
        credentialPolicy.setName(request.getCredentialPolicyName());
        credentialPolicy.setDescription(request.getDescription());
        if (request.getCredentialPolicyStatus() != null) {
            credentialPolicy.setStatus(request.getCredentialPolicyStatus());
        }
        credentialPolicy.setUsernameLengthMin(request.getUsernameLengthMin());
        credentialPolicy.setUsernameLengthMax(request.getUsernameLengthMax());
        credentialPolicy.setUsernameAllowedChars(request.getUsernameAllowedChars());
        credentialPolicy.setCredentialLengthMin(request.getCredentialLengthMin());
        credentialPolicy.setCredentialLengthMax(request.getCredentialLengthMax());
        credentialPolicy.setCredentialAllowedChars(request.getCredentialAllowedChars());
        credentialPolicy.setLimitSoft(request.getLimitSoft());
        credentialPolicy.setLimitHard(request.getLimitHard());
        credentialPolicy.setCheckHistoryCount(request.getCheckHistoryCount());
        credentialPolicy.setRotationEnabled(request.isRotationEnabled());
        credentialPolicy.setRotationDays(request.getRotationDays());
        credentialPolicy.setUsernameGenAlgorithm(request.getUsernameGenAlgorithm());
        credentialPolicy.setCredentialGenAlgorithm(request.getCredentialGenAlgorithm());
        credentialPolicy.setTimestampLastUpdated(new Date());
        credentialPolicyRepository.save(credentialPolicy);
        UpdateCredentialPolicyResponse response  = new UpdateCredentialPolicyResponse();
        response.setCredentialPolicyName(credentialPolicy.getName());
        response.setDescription(credentialPolicy.getDescription());
        response.setCredentialPolicyStatus(credentialPolicy.getStatus());
        response.setUsernameLengthMin(credentialPolicy.getUsernameLengthMin());
        response.setUsernameLengthMax(credentialPolicy.getUsernameLengthMax());
        response.setUsernameAllowedChars(credentialPolicy.getUsernameAllowedChars());
        response.setCredentialLengthMin(credentialPolicy.getCredentialLengthMin());
        response.setCredentialLengthMax(credentialPolicy.getCredentialLengthMax());
        response.setCredentialAllowedChars(credentialPolicy.getCredentialAllowedChars());
        response.setLimitSoft(credentialPolicy.getLimitSoft());
        response.setLimitHard(credentialPolicy.getLimitHard());
        response.setCheckHistoryCount(credentialPolicy.getCheckHistoryCount());
        response.setRotationEnabled(credentialPolicy.isRotationEnabled());
        response.setRotationDays(credentialPolicy.getRotationDays());
        response.setUsernameGenAlgorithm(credentialPolicy.getUsernameGenAlgorithm());
        response.setCredentialGenAlgorithm(credentialPolicy.getCredentialGenAlgorithm());
        return response;
    }

    /**
     * Get credential policy list.
     * @param request Credential policy list request.
     * @return Credential policy list response.
     */
    @Transactional
    public GetCredentialPolicyListResponse getCredentialPolicyList(GetCredentialPolicyListRequest request) {
        Iterable<CredentialPolicyEntity> credentialPolicies;
        if (request.isIncludeRemoved()) {
            credentialPolicies = credentialPolicyRepository.findAll();
        } else {
            credentialPolicies = credentialPolicyRepository.findCredentialPolicyByStatus(CredentialPolicyStatus.ACTIVE);
        }
        GetCredentialPolicyListResponse response = new GetCredentialPolicyListResponse();
        for (CredentialPolicyEntity credentialPolicy : credentialPolicies) {
            CredentialPolicyDetail credentialPolicyDetail = credentialPolicyConverter.fromEntity(credentialPolicy);
            response.getCredentialPolicies().add(credentialPolicyDetail);
        }
        return response;
    }

    /**
     * Delete a credential policy.
     * @param request Delete credential policy request.
     * @return Delete credential policy response.
     * @throws CredentialPolicyNotFoundException Thrown when credential policy is not found.
     */
    @Transactional
    public DeleteCredentialPolicyResponse deleteCredentialPolicy(DeleteCredentialPolicyRequest request) throws CredentialPolicyNotFoundException {
        Optional<CredentialPolicyEntity> credentialPolicyOptional = credentialPolicyRepository.findByName(request.getCredentialPolicyName());
        if (!credentialPolicyOptional.isPresent()) {
            throw new CredentialPolicyNotFoundException("Credential policy not found: " + request.getCredentialPolicyName());
        }
        CredentialPolicyEntity credentialPolicy = credentialPolicyOptional.get();
        if (credentialPolicy.getStatus() == CredentialPolicyStatus.REMOVED) {
            throw new CredentialPolicyNotFoundException("Credential policy is already REMOVED: " + request.getCredentialPolicyName());
        }
        credentialPolicy.setStatus(CredentialPolicyStatus.REMOVED);
        credentialPolicyRepository.save(credentialPolicy);
        DeleteCredentialPolicyResponse response = new DeleteCredentialPolicyResponse();
        response.setCredentialPolicyName(credentialPolicy.getName());
        response.setCredentialPolicyStatus(credentialPolicy.getStatus());
        return response;
    }

}
