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
import io.getlime.security.powerauth.app.nextstep.converter.OtpDefinitionConverter;
import io.getlime.security.powerauth.app.nextstep.repository.ApplicationRepository;
import io.getlime.security.powerauth.app.nextstep.repository.OtpDefinitionRepository;
import io.getlime.security.powerauth.app.nextstep.repository.OtpPolicyRepository;
import io.getlime.security.powerauth.app.nextstep.repository.catalogue.RepositoryCatalogue;
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
    private static final String AUDIT_TYPE_CONFIGURATION = "CONFIGURATION";

    private final OtpDefinitionRepository otpDefinitionRepository;
    private final OtpPolicyRepository otpPolicyRepository;
    private final ApplicationRepository applicationRepository;
    private final Audit audit;

    private final OtpDefinitionConverter otpDefinitionConverter = new OtpDefinitionConverter();

    /**
     * OTP definition service constructor.
     * @param repositoryCatalogue Repository catalogue.
     * @param audit Audit interface.
     */
    @Autowired
    public OtpDefinitionService(RepositoryCatalogue repositoryCatalogue, Audit audit) {
        this.otpDefinitionRepository = repositoryCatalogue.getOtpDefinitionRepository();
        this.otpPolicyRepository = repositoryCatalogue.getOtpPolicyRepository();
        this.applicationRepository = repositoryCatalogue.getApplicationRepository();
        this.audit = audit;
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
        final Optional<OtpDefinitionEntity> otpDefinitionOptional = otpDefinitionRepository.findByName(request.getOtpDefinitionName());
        if (otpDefinitionOptional.isPresent()) {
            throw new OtpDefinitionAlreadyExistsException("One time password already exists: " + request.getOtpDefinitionName());
        }
        final Optional<ApplicationEntity> applicationOptional = applicationRepository.findByName(request.getApplicationName());
        if (!applicationOptional.isPresent()) {
            throw new ApplicationNotFoundException("Application does not exist: " + request.getApplicationName());
        }
        final ApplicationEntity application = applicationOptional.get();
        if (application.getStatus() != ApplicationStatus.ACTIVE) {
            throw new ApplicationNotFoundException("Application is not ACTIVE: " + request.getApplicationName());
        }
        final Optional<OtpPolicyEntity> otpPolicyOptional = otpPolicyRepository.findByName(request.getOtpPolicyName());
        if (!otpPolicyOptional.isPresent()) {
            throw new OtpPolicyNotFoundException("Otp policy does not exist: " + request.getOtpPolicyName());
        }
        final OtpPolicyEntity otpPolicy = otpPolicyOptional.get();
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
        otpDefinition.setDataAdapterProxyEnabled(request.isDataAdapterProxyEnabled());
        otpDefinition.setTimestampCreated(new Date());
        otpDefinition = otpDefinitionRepository.save(otpDefinition);
        logger.debug("OTP definition was created, OTP definition ID: {}, OTP definition name: {}", otpDefinition.getOtpDefinitionId(), otpDefinition.getName());
        audit.info("OTP definition was created", AuditDetail.builder()
                .type(AUDIT_TYPE_CONFIGURATION)
                .param("otpDefinition", otpDefinition)
                .build());
        final CreateOtpDefinitionResponse response = new CreateOtpDefinitionResponse();
        response.setOtpDefinitionName(otpDefinition.getName());
        response.setOtpDefinitionStatus(otpDefinition.getStatus());
        response.setApplicationName(otpDefinition.getApplication().getName());
        response.setOtpPolicyName(otpDefinition.getOtpPolicy().getName());
        response.setEncryptionEnabled(otpDefinition.isEncryptionEnabled());
        response.setEncryptionAlgorithm(otpDefinition.getEncryptionAlgorithm());
        response.setDataAdapterProxyEnabled(otpDefinition.isDataAdapterProxyEnabled());
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
        final Optional<OtpDefinitionEntity> otpDefinitionOptional = otpDefinitionRepository.findByName(request.getOtpDefinitionName());
        if (!otpDefinitionOptional.isPresent()) {
            throw new OtpDefinitionNotFoundException("One time password definition not found: " + request.getOtpDefinitionName());
        }
        OtpDefinitionEntity otpDefinition = otpDefinitionOptional.get();
        if (otpDefinition.getStatus() != OtpDefinitionStatus.ACTIVE && request.getOtpDefinitionStatus() != OtpDefinitionStatus.ACTIVE) {
            throw new OtpDefinitionNotFoundException("One time password definition is not ACTIVE: " + request.getOtpDefinitionName());
        }
        final Optional<ApplicationEntity> applicationOptional = applicationRepository.findByName(request.getApplicationName());
        if (!applicationOptional.isPresent()) {
            throw new ApplicationNotFoundException("Application does not exist: " + request.getApplicationName());
        }
        final ApplicationEntity application = applicationOptional.get();
        if (application.getStatus() != ApplicationStatus.ACTIVE) {
            throw new ApplicationNotFoundException("Application is not ACTIVE: " + request.getApplicationName());
        }
        final Optional<OtpPolicyEntity> otpPolicyOptional = otpPolicyRepository.findByName(request.getOtpPolicyName());
        if (!otpPolicyOptional.isPresent()) {
            throw new OtpPolicyNotFoundException("Otp policy does not exist: " + request.getOtpPolicyName());
        }
        final OtpPolicyEntity otpPolicy = otpPolicyOptional.get();
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
        otpDefinition.setDataAdapterProxyEnabled(request.isDataAdapterProxyEnabled());
        otpDefinition.setTimestampLastUpdated(new Date());
        otpDefinition = otpDefinitionRepository.save(otpDefinition);
        logger.debug("OTP definition was updated, OTP definition ID: {}, OTP definition name: {}", otpDefinition.getOtpDefinitionId(), otpDefinition.getName());
        audit.info("OTP definition was updated", AuditDetail.builder()
                .type(AUDIT_TYPE_CONFIGURATION)
                .param("otpDefinition", otpDefinition)
                .build());
        final UpdateOtpDefinitionResponse response  = new UpdateOtpDefinitionResponse();
        response.setOtpDefinitionName(otpDefinition.getName());
        response.setDescription(otpDefinition.getDescription());
        response.setOtpDefinitionStatus(otpDefinition.getStatus());
        response.setApplicationName(otpDefinition.getApplication().getName());
        response.setOtpPolicyName(otpDefinition.getOtpPolicy().getName());
        response.setEncryptionEnabled(otpDefinition.isEncryptionEnabled());
        response.setEncryptionAlgorithm(otpDefinition.getEncryptionAlgorithm());
        response.setDataAdapterProxyEnabled(otpDefinition.isDataAdapterProxyEnabled());
        return response;
    }

    /**
     * Get OTP definition list.
     * @param request Get OTP definition list request.
     * @return Get OTP definition list response.
     */
    @Transactional
    public GetOtpDefinitionListResponse getOtpDefinitionList(GetOtpDefinitionListRequest request) {
        final Iterable<OtpDefinitionEntity> otpDefinitions;
        if (request.isIncludeRemoved()) {
            otpDefinitions = otpDefinitionRepository.findAll();
        } else {
            otpDefinitions = otpDefinitionRepository.findOtpDefinitionByStatus(OtpDefinitionStatus.ACTIVE);
        }
        final GetOtpDefinitionListResponse response = new GetOtpDefinitionListResponse();
        for (OtpDefinitionEntity otpDefinition : otpDefinitions) {
            final OtpDefinitionDetail otpDefinitionDetail = otpDefinitionConverter.fromEntity(otpDefinition);
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
        final Optional<OtpDefinitionEntity> otpDefinitionOptional = otpDefinitionRepository.findByName(request.getOtpDefinitionName());
        if (!otpDefinitionOptional.isPresent()) {
            throw new OtpDefinitionNotFoundException("One time password definition not found: " + request.getOtpDefinitionName());
        }
        OtpDefinitionEntity otpDefinition = otpDefinitionOptional.get();
        if (otpDefinition.getStatus() == OtpDefinitionStatus.REMOVED) {
            throw new OtpDefinitionNotFoundException("One time password definition is already REMOVED: " + request.getOtpDefinitionName());
        }
        otpDefinition.setStatus(OtpDefinitionStatus.REMOVED);
        otpDefinition.setTimestampLastUpdated(new Date());
        otpDefinition = otpDefinitionRepository.save(otpDefinition);
        logger.debug("OTP definition was removed, OTP definition ID: {}, OTP definition name: {}", otpDefinition.getOtpDefinitionId(), otpDefinition.getName());
        audit.info("OTP definition was removed", AuditDetail.builder()
                .type(AUDIT_TYPE_CONFIGURATION)
                .param("otpDefinition", otpDefinition)
                .build());
        final DeleteOtpDefinitionResponse response = new DeleteOtpDefinitionResponse();
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
        final Optional<OtpDefinitionEntity> otpDefinitionOptional = otpDefinitionRepository.findByName(otpDefinitionName);
        if (!otpDefinitionOptional.isPresent()) {
            throw new OtpDefinitionNotFoundException("OTP definition not found: " + otpDefinitionName);
        }
        final OtpDefinitionEntity otpDefinition = otpDefinitionOptional.get();
        if (otpDefinition.getStatus() != OtpDefinitionStatus.ACTIVE) {
            throw new OtpDefinitionNotFoundException("OTP definition is not ACTIVE: " + otpDefinitionName);
        }
        return otpDefinition;
    }
}
