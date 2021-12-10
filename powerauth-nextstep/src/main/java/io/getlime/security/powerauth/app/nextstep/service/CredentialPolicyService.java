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
import io.getlime.security.powerauth.app.nextstep.converter.CredentialPolicyConverter;
import io.getlime.security.powerauth.app.nextstep.converter.ParameterConverter;
import io.getlime.security.powerauth.app.nextstep.repository.CredentialPolicyRepository;
import io.getlime.security.powerauth.app.nextstep.repository.catalogue.RepositoryCatalogue;
import io.getlime.security.powerauth.app.nextstep.repository.model.entity.CredentialPolicyEntity;
import io.getlime.security.powerauth.lib.nextstep.model.entity.CredentialPolicyDetail;
import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.CredentialPolicyStatus;
import io.getlime.security.powerauth.lib.nextstep.model.exception.CredentialPolicyAlreadyExistsException;
import io.getlime.security.powerauth.lib.nextstep.model.exception.CredentialPolicyNotFoundException;
import io.getlime.security.powerauth.lib.nextstep.model.exception.InvalidConfigurationException;
import io.getlime.security.powerauth.lib.nextstep.model.exception.InvalidRequestException;
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
    private static final String AUDIT_TYPE_CONFIGURATION = "CONFIGURATION";

    private final CredentialPolicyRepository credentialPolicyRepository;
    private final Audit audit;

    private final CredentialPolicyConverter credentialPolicyConverter = new CredentialPolicyConverter();
    private final ParameterConverter parameterConverter = new ParameterConverter();

    /**
     * Credential policy service constructor.
     * @param repositoryCatalogue Repository catalogue.
     * @param audit Audit interface.
     */
    @Autowired
    public CredentialPolicyService(RepositoryCatalogue repositoryCatalogue, Audit audit) {
        this.credentialPolicyRepository = repositoryCatalogue.getCredentialPolicyRepository();
        this.audit = audit;
    }

    /**
     * Create a credential policy.
     * @param request Create credential policy request.
     * @return Create credential policy response.
     * @throws CredentialPolicyAlreadyExistsException Thrown when credential policy already exists.
     * @throws InvalidRequestException Thrown when request is invalid.
     */
    @Transactional
    public CreateCredentialPolicyResponse createCredentialPolicy(CreateCredentialPolicyRequest request) throws CredentialPolicyAlreadyExistsException, InvalidRequestException {
        final Optional<CredentialPolicyEntity> credentialPolicyOptional = credentialPolicyRepository.findByName(request.getCredentialPolicyName());
        if (credentialPolicyOptional.isPresent()) {
            throw new CredentialPolicyAlreadyExistsException("Credential policy already exists: " + request.getCredentialPolicyName());
        }
        CredentialPolicyEntity credentialPolicy = new CredentialPolicyEntity();
        credentialPolicy.setName(request.getCredentialPolicyName());
        credentialPolicy.setDescription(request.getDescription());
        credentialPolicy.setStatus(CredentialPolicyStatus.ACTIVE);
        credentialPolicy.setUsernameLengthMin(request.getUsernameLengthMin());
        credentialPolicy.setUsernameLengthMax(request.getUsernameLengthMax());
        credentialPolicy.setUsernameAllowedPattern(request.getUsernameAllowedPattern());
        credentialPolicy.setCredentialLengthMin(request.getCredentialLengthMin());
        credentialPolicy.setCredentialLengthMax(request.getCredentialLengthMax());
        credentialPolicy.setLimitSoft(request.getLimitSoft());
        credentialPolicy.setLimitHard(request.getLimitHard());
        credentialPolicy.setCheckHistoryCount(request.getCheckHistoryCount());
        credentialPolicy.setRotationEnabled(request.isRotationEnabled());
        credentialPolicy.setRotationDays(request.getRotationDays());
        credentialPolicy.setTemporaryCredentialExpirationTime(request.getTemporaryCredentialExpirationTime());
        credentialPolicy.setUsernameGenAlgorithm(request.getUsernameGenAlgorithm());
        try {
            credentialPolicy.setUsernameGenParam(parameterConverter.fromObject(request.getUsernameGenParam()));
        } catch (JsonProcessingException ex) {
            throw new InvalidRequestException(ex);
        }
        credentialPolicy.setCredentialGenAlgorithm(request.getCredentialGenAlgorithm());
        try {
            credentialPolicy.setCredentialGenParam(parameterConverter.fromObject(request.getCredentialGenParam()));
        } catch (JsonProcessingException ex) {
            throw new InvalidRequestException(ex);
        }
        try {
            credentialPolicy.setCredentialValParam(parameterConverter.fromObject(request.getCredentialValParam()));
        } catch (JsonProcessingException ex) {
            throw new InvalidRequestException(ex);
        }
        credentialPolicy.setTimestampCreated(new Date());
        credentialPolicy = credentialPolicyRepository.save(credentialPolicy);
        logger.debug("Credential policy was created, credential policy ID: {}, credential policy name: {}", credentialPolicy.getCredentialPolicyId(), credentialPolicy.getName());
        audit.info("Credential policy was created", AuditDetail.builder()
                .type(AUDIT_TYPE_CONFIGURATION)
                .param("credentialPolicy", credentialPolicy)
                .build());
        final CreateCredentialPolicyResponse response = new CreateCredentialPolicyResponse();
        response.setCredentialPolicyName(credentialPolicy.getName());
        response.setDescription(credentialPolicy.getDescription());
        response.setCredentialPolicyStatus(credentialPolicy.getStatus());
        response.setUsernameLengthMin(credentialPolicy.getUsernameLengthMin());
        response.setUsernameLengthMax(credentialPolicy.getUsernameLengthMax());
        response.setUsernameAllowedPattern(credentialPolicy.getUsernameAllowedPattern());
        response.setCredentialLengthMin(credentialPolicy.getCredentialLengthMin());
        response.setCredentialLengthMax(credentialPolicy.getCredentialLengthMax());
        response.setLimitSoft(request.getLimitSoft());
        response.setLimitHard(request.getLimitHard());
        response.setCheckHistoryCount(request.getCheckHistoryCount());
        response.setRotationEnabled(request.isRotationEnabled());
        response.setRotationDays(request.getRotationDays());
        response.setTemporaryCredentialExpirationTime(request.getTemporaryCredentialExpirationTime());
        response.setUsernameGenAlgorithm(request.getUsernameGenAlgorithm());
        response.setUsernameGenParam(request.getUsernameGenParam());
        response.setCredentialGenAlgorithm(request.getCredentialGenAlgorithm());
        response.setCredentialGenParam(request.getCredentialGenParam());
        response.setCredentialValParam(request.getCredentialValParam());
        return response;
    }

    /**
     * Update a credential policy.
     * @param request Update credential policy request.
     * @return Update credential policy response.
     * @throws CredentialPolicyNotFoundException Thrown when credential policy is not found.
     * @throws InvalidRequestException Thrown when request is invalid.
     */
    @Transactional
    public UpdateCredentialPolicyResponse updateCredentialPolicy(UpdateCredentialPolicyRequest request) throws CredentialPolicyNotFoundException, InvalidRequestException {
        final Optional<CredentialPolicyEntity> credentialPolicyOptional = credentialPolicyRepository.findByName(request.getCredentialPolicyName());
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
        credentialPolicy.setUsernameAllowedPattern(request.getUsernameAllowedPattern());
        credentialPolicy.setCredentialLengthMin(request.getCredentialLengthMin());
        credentialPolicy.setCredentialLengthMax(request.getCredentialLengthMax());
        credentialPolicy.setLimitSoft(request.getLimitSoft());
        credentialPolicy.setLimitHard(request.getLimitHard());
        credentialPolicy.setCheckHistoryCount(request.getCheckHistoryCount());
        credentialPolicy.setRotationEnabled(request.isRotationEnabled());
        credentialPolicy.setRotationDays(request.getRotationDays());
        credentialPolicy.setTemporaryCredentialExpirationTime(request.getTemporaryCredentialExpirationTime());
        if (request.getUsernameGenAlgorithm() != null) {
            credentialPolicy.setUsernameGenAlgorithm(request.getUsernameGenAlgorithm());
        }
        if (request.getUsernameGenParam() != null) {
            try {
                credentialPolicy.setUsernameGenParam(parameterConverter.fromObject(request.getUsernameGenParam()));
            } catch (JsonProcessingException ex) {
                throw new InvalidRequestException(ex);
            }
        }
        if (request.getCredentialGenAlgorithm() != null) {
            credentialPolicy.setCredentialGenAlgorithm(request.getCredentialGenAlgorithm());
        }
        if (request.getCredentialGenParam() != null) {
            try {
                credentialPolicy.setCredentialGenParam(parameterConverter.fromObject(request.getCredentialGenParam()));
            } catch (JsonProcessingException ex) {
                throw new InvalidRequestException(ex);
            }
        }
        if (request.getCredentialValParam() != null) {
            try {
                credentialPolicy.setCredentialValParam(parameterConverter.fromObject(request.getCredentialValParam()));
            } catch (JsonProcessingException ex) {
                throw new InvalidRequestException(ex);
            }
        }
        credentialPolicy.setTimestampLastUpdated(new Date());
        credentialPolicy = credentialPolicyRepository.save(credentialPolicy);
        logger.debug("Credential policy was updated, credential policy ID: {}, credential policy name: {}", credentialPolicy.getCredentialPolicyId(), credentialPolicy.getName());
        audit.info("Credential policy was updated", AuditDetail.builder()
                .type(AUDIT_TYPE_CONFIGURATION)
                .param("credentialPolicy", credentialPolicy)
                .build());
        final UpdateCredentialPolicyResponse response  = new UpdateCredentialPolicyResponse();
        response.setCredentialPolicyName(credentialPolicy.getName());
        response.setDescription(credentialPolicy.getDescription());
        response.setCredentialPolicyStatus(credentialPolicy.getStatus());
        response.setUsernameLengthMin(credentialPolicy.getUsernameLengthMin());
        response.setUsernameLengthMax(credentialPolicy.getUsernameLengthMax());
        response.setUsernameAllowedPattern(credentialPolicy.getUsernameAllowedPattern());
        response.setCredentialLengthMin(credentialPolicy.getCredentialLengthMin());
        response.setCredentialLengthMax(credentialPolicy.getCredentialLengthMax());
        response.setLimitSoft(credentialPolicy.getLimitSoft());
        response.setLimitHard(credentialPolicy.getLimitHard());
        response.setCheckHistoryCount(credentialPolicy.getCheckHistoryCount());
        response.setRotationEnabled(credentialPolicy.isRotationEnabled());
        response.setRotationDays(credentialPolicy.getRotationDays());
        response.setTemporaryCredentialExpirationTime(credentialPolicy.getTemporaryCredentialExpirationTime());
        response.setUsernameGenAlgorithm(credentialPolicy.getUsernameGenAlgorithm());
        response.setUsernameGenParam(request.getUsernameGenParam());
        response.setCredentialGenAlgorithm(credentialPolicy.getCredentialGenAlgorithm());
        response.setCredentialGenParam(request.getCredentialGenParam());
        response.setCredentialValParam(request.getCredentialValParam());
        return response;
    }

    /**
     * Get credential policy list.
     * @param request Credential policy list request.
     * @return Credential policy list response.
     * @throws InvalidConfigurationException Thrown when Next Step configuration is invalid.
     */
    @Transactional
    public GetCredentialPolicyListResponse getCredentialPolicyList(GetCredentialPolicyListRequest request) throws InvalidConfigurationException {
        final Iterable<CredentialPolicyEntity> credentialPolicies;
        if (request.isIncludeRemoved()) {
            credentialPolicies = credentialPolicyRepository.findAll();
        } else {
            credentialPolicies = credentialPolicyRepository.findCredentialPolicyByStatus(CredentialPolicyStatus.ACTIVE);
        }
        final GetCredentialPolicyListResponse response = new GetCredentialPolicyListResponse();
        for (CredentialPolicyEntity credentialPolicy : credentialPolicies) {
            final CredentialPolicyDetail credentialPolicyDetail = credentialPolicyConverter.fromEntity(credentialPolicy);
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
        final Optional<CredentialPolicyEntity> credentialPolicyOptional = credentialPolicyRepository.findByName(request.getCredentialPolicyName());
        if (!credentialPolicyOptional.isPresent()) {
            throw new CredentialPolicyNotFoundException("Credential policy not found: " + request.getCredentialPolicyName());
        }
        CredentialPolicyEntity credentialPolicy = credentialPolicyOptional.get();
        if (credentialPolicy.getStatus() == CredentialPolicyStatus.REMOVED) {
            throw new CredentialPolicyNotFoundException("Credential policy is already REMOVED: " + request.getCredentialPolicyName());
        }
        credentialPolicy.setStatus(CredentialPolicyStatus.REMOVED);
        credentialPolicy.setTimestampLastUpdated(new Date());
        credentialPolicy = credentialPolicyRepository.save(credentialPolicy);
        logger.debug("Credential policy was removed, credential policy ID: {}, credential policy name: {}", credentialPolicy.getCredentialPolicyId(), credentialPolicy.getName());
        audit.info("Credential policy was removed", AuditDetail.builder()
                .type(AUDIT_TYPE_CONFIGURATION)
                .param("credentialPolicy", credentialPolicy)
                .build());
        final DeleteCredentialPolicyResponse response = new DeleteCredentialPolicyResponse();
        response.setCredentialPolicyName(credentialPolicy.getName());
        response.setCredentialPolicyStatus(credentialPolicy.getStatus());
        return response;
    }

}
