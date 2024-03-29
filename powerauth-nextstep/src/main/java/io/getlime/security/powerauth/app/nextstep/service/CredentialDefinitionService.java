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

import com.wultra.core.audit.base.Audit;
import com.wultra.core.audit.base.model.AuditDetail;
import io.getlime.security.powerauth.app.nextstep.converter.CredentialDefinitionConverter;
import io.getlime.security.powerauth.app.nextstep.repository.*;
import io.getlime.security.powerauth.app.nextstep.repository.catalogue.RepositoryCatalogue;
import io.getlime.security.powerauth.app.nextstep.repository.model.entity.*;
import io.getlime.security.powerauth.lib.nextstep.model.entity.CredentialDefinitionDetail;
import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.ApplicationStatus;
import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.CredentialDefinitionStatus;
import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.CredentialPolicyStatus;
import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.HashConfigStatus;
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
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Optional;

/**
 * This service handles persistence of credential definitions.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Service
public class CredentialDefinitionService {

    private final Logger logger = LoggerFactory.getLogger(CredentialDefinitionService.class);
    private static final String AUDIT_TYPE_CONFIGURATION = "CONFIGURATION";

    private final CredentialDefinitionRepository credentialDefinitionRepository;
    private final CredentialPolicyRepository credentialPolicyRepository;
    private final ApplicationRepository applicationRepository;
    private final HashConfigRepository hashConfigRepository;
    private final OrganizationRepository organizationRepository;
    private final Audit audit;

    private final CredentialDefinitionConverter credentialDefinitionConverter = new CredentialDefinitionConverter();

    /**
     * Credential definition service constructor.
     * @param repositoryCatalogue Repository catalogue.
     * @param audit Audit interface.
     */
    @Autowired
    public CredentialDefinitionService(RepositoryCatalogue repositoryCatalogue, Audit audit) {
        this.credentialDefinitionRepository = repositoryCatalogue.getCredentialDefinitionRepository();
        this.credentialPolicyRepository = repositoryCatalogue.getCredentialPolicyRepository();
        this.applicationRepository = repositoryCatalogue.getApplicationRepository();
        this.hashConfigRepository = repositoryCatalogue.getHashConfigRepository();
        this.organizationRepository = repositoryCatalogue.getOrganizationRepository();
        this.audit = audit;
    }

    /**
     * Create a credential definition.
     * @param request Create credential definition request.
     * @return Create credential definition response.
     * @throws CredentialDefinitionAlreadyExistsException Thrown when credential definition already exists.
     * @throws ApplicationNotFoundException Thrown when application is not found.
     * @throws CredentialPolicyNotFoundException Thrown when credential policy is not found.
     * @throws HashConfigNotFoundException Thrown when hashing configuration is not found.
     * @throws OrganizationNotFoundException Thrown when organization is not found.
     */
    @Transactional
    public CreateCredentialDefinitionResponse createCredentialDefinition(CreateCredentialDefinitionRequest request) throws CredentialDefinitionAlreadyExistsException, ApplicationNotFoundException, CredentialPolicyNotFoundException, HashConfigNotFoundException, OrganizationNotFoundException {
        final Optional<CredentialDefinitionEntity> credentialDefinitionOptional = credentialDefinitionRepository.findByName(request.getCredentialDefinitionName());
        if (credentialDefinitionOptional.isPresent()) {
            throw new CredentialDefinitionAlreadyExistsException("Credential definition already exists: " + request.getCredentialDefinitionName());
        }
        final ApplicationEntity application = applicationRepository.findByName(request.getApplicationName()).orElseThrow(() ->
                new ApplicationNotFoundException("Application does not exist: " + request.getApplicationName()));
        if (application.getStatus() != ApplicationStatus.ACTIVE) {
            throw new ApplicationNotFoundException("Application is not ACTIVE: " + request.getApplicationName());
        }
        final CredentialPolicyEntity credentialPolicy = credentialPolicyRepository.findByName(request.getCredentialPolicyName()).orElseThrow(() ->
                new CredentialPolicyNotFoundException("Credential policy does not exist: " + request.getCredentialPolicyName()));
        if (credentialPolicy.getStatus() != CredentialPolicyStatus.ACTIVE) {
            throw new CredentialPolicyNotFoundException("Credential policy is not ACTIVE: " + request.getCredentialPolicyName());
        }
        HashConfigEntity hashConfigEntity = null;
        if (request.isHashingEnabled() && request.getHashConfigName() != null) {
            final Optional<HashConfigEntity> hashConfigOptional = hashConfigRepository.findByName(request.getHashConfigName());
            if (hashConfigOptional.isEmpty()) {
                throw new HashConfigNotFoundException("Hashing configuration does not exist: " + request.getHashConfigName());
            }
            hashConfigEntity = hashConfigOptional.get();
            if (hashConfigEntity.getStatus() != HashConfigStatus.ACTIVE) {
                throw new HashConfigNotFoundException("Hashing configuration is not ACTIVE: " + request.getHashConfigName());
            }
        }
        CredentialDefinitionEntity credentialDefinition = new CredentialDefinitionEntity();
        credentialDefinition.setName(request.getCredentialDefinitionName());
        credentialDefinition.setDescription(request.getDescription());
        credentialDefinition.setApplication(application);
        if (request.getOrganizationId() != null) {
            final OrganizationEntity organization = organizationRepository.findById(request.getOrganizationId()).orElseThrow(() ->
                    new OrganizationNotFoundException("Organization not found: " + request.getOrganizationId()));
            credentialDefinition.setOrganization(organization);
        }
        credentialDefinition.setCredentialPolicy(credentialPolicy);
        credentialDefinition.setCategory(request.getCategory());
        credentialDefinition.setEncryptionEnabled(request.isEncryptionEnabled());
        credentialDefinition.setEncryptionAlgorithm(request.getEncryptionAlgorithm());
        if (hashConfigEntity != null) {
            credentialDefinition.setHashingConfig(hashConfigEntity);
        }
        credentialDefinition.setE2eEncryptionEnabled(request.isE2eEncryptionEnabled());
        credentialDefinition.setE2eEncryptionAlgorithm(request.getE2eEncryptionAlgorithm());
        credentialDefinition.setE2eEncryptionCipherTransformation(request.getE2eEncryptionCipherTransformation());
        credentialDefinition.setE2eEncryptionForTemporaryCredentialEnabled(request.isE2eEncryptionForTemporaryCredentialEnabled());
        credentialDefinition.setStatus(CredentialDefinitionStatus.ACTIVE);
        credentialDefinition.setDataAdapterProxyEnabled(request.isDataAdapterProxyEnabled());
        credentialDefinition.setTimestampCreated(new Date());
        credentialDefinition = credentialDefinitionRepository.save(credentialDefinition);
        logger.debug("Credential definition was created, credential definition ID: {}, credential definition name: {}", credentialDefinition.getCredentialDefinitionId(), credentialDefinition.getName());
        audit.info("Credential definition was created", AuditDetail.builder()
                .type(AUDIT_TYPE_CONFIGURATION)
                .param("credentialDefinition", credentialDefinition)
                .build());
        final CreateCredentialDefinitionResponse response = new CreateCredentialDefinitionResponse();
        response.setCredentialDefinitionName(credentialDefinition.getName());
        response.setDescription(credentialDefinition.getDescription());
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
        response.setE2eEncryptionAlgorithm(credentialDefinition.getE2eEncryptionAlgorithm());
        response.setE2eEncryptionCipherTransformation(credentialDefinition.getE2eEncryptionCipherTransformation());
        response.setE2eEncryptionForTemporaryCredentialEnabled(credentialDefinition.isE2eEncryptionForTemporaryCredentialEnabled());
        response.setDataAdapterProxyEnabled(credentialDefinition.isDataAdapterProxyEnabled());
        return response;
    }

    /**
     * Update a credential definition.
     * @param request Update credential definition request.
     * @return Update credential definition response.
     * @throws CredentialDefinitionNotFoundException Thrown when credential definition is not found.
     * @throws ApplicationNotFoundException Thrown when application is not found.
     * @throws CredentialPolicyNotFoundException Thrown when credential policy is not found.
     * @throws HashConfigNotFoundException Thrown when hashing configuration is not found.
     * @throws OrganizationNotFoundException Thrown when organization is not found.
     */
    @Transactional
    public UpdateCredentialDefinitionResponse updateCredentialDefinition(UpdateCredentialDefinitionRequest request) throws CredentialDefinitionNotFoundException, ApplicationNotFoundException, CredentialPolicyNotFoundException, HashConfigNotFoundException, OrganizationNotFoundException {
        CredentialDefinitionEntity credentialDefinition = credentialDefinitionRepository.findByName(request.getCredentialDefinitionName()).orElseThrow(() ->
            new CredentialDefinitionNotFoundException("Credential definition not found: " + request.getCredentialDefinitionName()));
        if (credentialDefinition.getStatus() != CredentialDefinitionStatus.ACTIVE && request.getCredentialDefinitionStatus() != CredentialDefinitionStatus.ACTIVE) {
            throw new CredentialDefinitionNotFoundException("Credential definition is not ACTIVE: " + request.getCredentialDefinitionName());
        }
        final ApplicationEntity application = applicationRepository.findByName(request.getApplicationName()).orElseThrow(() ->
               new ApplicationNotFoundException("Application does not exist: " + request.getApplicationName()));
        if (application.getStatus() != ApplicationStatus.ACTIVE) {
            throw new ApplicationNotFoundException("Application is not ACTIVE: " + request.getApplicationName());
        }
        final CredentialPolicyEntity credentialPolicy = credentialPolicyRepository.findByName(request.getCredentialPolicyName()).orElseThrow(() ->
                new CredentialPolicyNotFoundException("Credential policy does not exist: " + request.getCredentialPolicyName()));
        if (credentialPolicy.getStatus() != CredentialPolicyStatus.ACTIVE) {
            throw new CredentialPolicyNotFoundException("Credential policy is not ACTIVE: " + request.getCredentialPolicyName());
        }
        HashConfigEntity hashConfigEntity = null;
        if (request.isHashingEnabled() && request.getHashConfigName() != null) {
            hashConfigEntity = hashConfigRepository.findByName(request.getHashConfigName()).orElseThrow(() ->
                    new HashConfigNotFoundException("Hashing configuration does not exist: " + request.getHashConfigName()));
            if (hashConfigEntity.getStatus() != HashConfigStatus.ACTIVE) {
                throw new HashConfigNotFoundException("Hashing configuration is not ACTIVE: " + request.getHashConfigName());
            }
        }
        credentialDefinition.setName(request.getCredentialDefinitionName());
        credentialDefinition.setDescription(request.getDescription());
        if (request.getCredentialDefinitionStatus() != null) {
            credentialDefinition.setStatus(request.getCredentialDefinitionStatus());
        }
        credentialDefinition.setApplication(application);
        OrganizationEntity organization = null;
        if (request.getOrganizationId() != null) {
            organization = organizationRepository.findById(request.getOrganizationId()).orElseThrow(() ->
                    new OrganizationNotFoundException("Organization not found: " + request.getOrganizationId()));
            credentialDefinition.setOrganization(organization);
        }
        credentialDefinition.setCredentialPolicy(credentialPolicy);
        credentialDefinition.setCategory(request.getCategory());
        credentialDefinition.setEncryptionEnabled(request.isEncryptionEnabled());
        credentialDefinition.setEncryptionAlgorithm(request.getEncryptionAlgorithm());
        credentialDefinition.setHashingConfig(hashConfigEntity);
        credentialDefinition.setE2eEncryptionEnabled(request.isE2eEncryptionEnabled());
        credentialDefinition.setE2eEncryptionAlgorithm(request.getE2eEncryptionAlgorithm());
        credentialDefinition.setE2eEncryptionCipherTransformation(request.getE2eEncryptionCipherTransformation());
        credentialDefinition.setE2eEncryptionForTemporaryCredentialEnabled(request.isE2eEncryptionForTemporaryCredentialEnabled());
        credentialDefinition.setDataAdapterProxyEnabled(request.isDataAdapterProxyEnabled());
        credentialDefinition.setTimestampLastUpdated(new Date());
        credentialDefinition = credentialDefinitionRepository.save(credentialDefinition);
        logger.debug("Credential definition was updated, credential definition ID: {}, credential definition name: {}", credentialDefinition.getCredentialDefinitionId(), credentialDefinition.getName());
        audit.info("Credential definition was updated", AuditDetail.builder()
                .type(AUDIT_TYPE_CONFIGURATION)
                .param("credentialDefinition", credentialDefinition)
                .build());
        final UpdateCredentialDefinitionResponse response  = new UpdateCredentialDefinitionResponse();
        response.setCredentialDefinitionName(credentialDefinition.getName());
        response.setDescription(credentialDefinition.getDescription());
        response.setCredentialDefinitionStatus(credentialDefinition.getStatus());
        response.setApplicationName(credentialDefinition.getApplication().getName());
        if (organization != null) {
            response.setOrganizationId(organization.getOrganizationId());
        }
        response.setCredentialPolicyName(credentialDefinition.getCredentialPolicy().getName());
        response.setCategory(credentialDefinition.getCategory());
        response.setEncryptionEnabled(credentialDefinition.isEncryptionEnabled());
        response.setEncryptionAlgorithm(credentialDefinition.getEncryptionAlgorithm());
        if (credentialDefinition.getHashingConfig() != null) {
            response.setHashingEnabled(true);
            response.setHashConfigName(credentialDefinition.getHashingConfig().getName());
        }
        response.setE2eEncryptionEnabled(credentialDefinition.isE2eEncryptionEnabled());
        response.setE2eEncryptionAlgorithm(credentialDefinition.getE2eEncryptionAlgorithm());
        response.setE2eEncryptionCipherTransformation(credentialDefinition.getE2eEncryptionCipherTransformation());
        response.setE2eEncryptionForTemporaryCredentialEnabled(credentialDefinition.isE2eEncryptionForTemporaryCredentialEnabled());
        response.setDataAdapterProxyEnabled(credentialDefinition.isDataAdapterProxyEnabled());
        return response;
    }

    /**
     * Get credential definition list.
     * @param request Get credential definition list request.
     * @return Get credential definition list response.
     */
    @Transactional
    public GetCredentialDefinitionListResponse getCredentialDefinitionList(GetCredentialDefinitionListRequest request) {
        final Iterable<CredentialDefinitionEntity> credentialDefinitions;
        if (request.isIncludeRemoved()) {
            credentialDefinitions = credentialDefinitionRepository.findAll();
        } else {
            credentialDefinitions = credentialDefinitionRepository.findCredentialDefinitionByStatus(CredentialDefinitionStatus.ACTIVE);
        }
        final GetCredentialDefinitionListResponse response = new GetCredentialDefinitionListResponse();
        for (CredentialDefinitionEntity credentialDefinition : credentialDefinitions) {
            final CredentialDefinitionDetail credentialDefinitionDetail = credentialDefinitionConverter.fromEntity(credentialDefinition);
            response.getCredentialDefinitions().add(credentialDefinitionDetail);
        }
        return response;
    }

    /**
     * Delete a credential definition.
     * @param request Delete credential definition request.
     * @return Delete credential definition response.
     * @throws CredentialDefinitionNotFoundException Thrown when credential definition is not found.
     */
    @Transactional
    public DeleteCredentialDefinitionResponse deleteCredentialDefinition(DeleteCredentialDefinitionRequest request) throws CredentialDefinitionNotFoundException {
        CredentialDefinitionEntity credentialDefinition = credentialDefinitionRepository.findByName(request.getCredentialDefinitionName()).orElseThrow(() ->
                new CredentialDefinitionNotFoundException("Credential definition not found: " + request.getCredentialDefinitionName()));
        if (credentialDefinition.getStatus() == CredentialDefinitionStatus.REMOVED) {
            throw new CredentialDefinitionNotFoundException("Credential definition is already REMOVED: " + request.getCredentialDefinitionName());
        }
        credentialDefinition.setStatus(CredentialDefinitionStatus.REMOVED);
        credentialDefinition.setTimestampLastUpdated(new Date());
        credentialDefinition = credentialDefinitionRepository.save(credentialDefinition);
        logger.debug("Credential definition was removed, credential definition ID: {}, credential definition name: {}", credentialDefinition.getCredentialDefinitionId(), credentialDefinition.getName());
        audit.info("Credential definition was removed", AuditDetail.builder()
                .type(AUDIT_TYPE_CONFIGURATION)
                .param("credentialDefinition", credentialDefinition)
                .build());
        final DeleteCredentialDefinitionResponse response = new DeleteCredentialDefinitionResponse();
        response.setCredentialDefinitionName(credentialDefinition.getName());
        response.setCredentialDefinitionStatus(credentialDefinition.getStatus());
        return response;
    }

    /**
     * Find a credential definition. This method is not transactional.
     * @param credentialDefinitionName Credential definition name.
     * @return Credential definition.
     * @throws CredentialDefinitionNotFoundException Thrown when credential definition is not found.
     */
    public CredentialDefinitionEntity findActiveCredentialDefinition(String credentialDefinitionName) throws CredentialDefinitionNotFoundException {
        final CredentialDefinitionEntity credentialDefinition = credentialDefinitionRepository.findByName(credentialDefinitionName).orElseThrow(() ->
                new CredentialDefinitionNotFoundException("Credential definition not found: " + credentialDefinitionName));
        if (credentialDefinition.getStatus() != CredentialDefinitionStatus.ACTIVE) {
            throw new CredentialDefinitionNotFoundException("Credential definition is not ACTIVE: " + credentialDefinitionName);
        }
        return credentialDefinition;
    }

}
