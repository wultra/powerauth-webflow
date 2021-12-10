/*
 * PowerAuth Web Flow and related software components
 * Copyright (C) 2017 Wultra s.r.o.
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
import io.getlime.security.powerauth.app.nextstep.controller.OrganizationController;
import io.getlime.security.powerauth.app.nextstep.converter.OrganizationConverter;
import io.getlime.security.powerauth.app.nextstep.repository.OrganizationRepository;
import io.getlime.security.powerauth.app.nextstep.repository.catalogue.RepositoryCatalogue;
import io.getlime.security.powerauth.app.nextstep.repository.model.entity.OrganizationEntity;
import io.getlime.security.powerauth.lib.nextstep.model.exception.DeleteNotAllowedException;
import io.getlime.security.powerauth.lib.nextstep.model.exception.OrganizationAlreadyExistsException;
import io.getlime.security.powerauth.lib.nextstep.model.exception.OrganizationNotFoundException;
import io.getlime.security.powerauth.lib.nextstep.model.request.CreateOrganizationRequest;
import io.getlime.security.powerauth.lib.nextstep.model.request.DeleteOrganizationRequest;
import io.getlime.security.powerauth.lib.nextstep.model.request.GetOrganizationDetailRequest;
import io.getlime.security.powerauth.lib.nextstep.model.response.CreateOrganizationResponse;
import io.getlime.security.powerauth.lib.nextstep.model.response.DeleteOrganizationResponse;
import io.getlime.security.powerauth.lib.nextstep.model.response.GetOrganizationDetailResponse;
import io.getlime.security.powerauth.lib.nextstep.model.response.GetOrganizationListResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

/**
 * Service which handles persistence of organizations.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Service
public class OrganizationService {

    private static final Logger logger = LoggerFactory.getLogger(OrganizationController.class);
    private static final String AUDIT_TYPE_CONFIGURATION = "CONFIGURATION";

    private final OrganizationRepository organizationRepository;
    private final Audit audit;

    private final OrganizationConverter organizationConverter = new OrganizationConverter();

    /**
     * Organization service constructor.
     * @param repositoryCatalogue Repository catalogue.
     * @param audit Audit interface.
     */
    @Autowired
    public OrganizationService(RepositoryCatalogue repositoryCatalogue, Audit audit) {
        this.organizationRepository = repositoryCatalogue.getOrganizationRepository();
        this.audit = audit;
    }

    /**
     * Create an organization.
     * @param request Create organization request.
     * @return Create organization response.
     * @throws OrganizationAlreadyExistsException Thrown when organization already exists.
     */
    @Transactional
    public CreateOrganizationResponse createOrganization(CreateOrganizationRequest request) throws OrganizationAlreadyExistsException {
        final Optional<OrganizationEntity> organizationOptional = organizationRepository.findById(request.getOrganizationId());
        if (organizationOptional.isPresent()) {
            throw new OrganizationAlreadyExistsException("Organization already exists: " + request.getOrganizationId());
        }
        OrganizationEntity organization = new OrganizationEntity();
        organization.setOrganizationId(request.getOrganizationId());
        organization.setDisplayNameKey(request.getDisplayNameKey());
        organization.setDefault(request.isDefault());
        organization.setOrderNumber(request.getOrderNumber());
        organization.setDefaultCredentialName(request.getDefaultCredentialName());
        organization.setDefaultOtpName(request.getDefaultOtpName());
        organization = organizationRepository.save(organization);
        logger.debug("Organization was created: {}", organization.getOrganizationId());
        audit.info("Organization was created", AuditDetail.builder()
                .type(AUDIT_TYPE_CONFIGURATION)
                .param("organization", organization)
                .build());
        final CreateOrganizationResponse response = new CreateOrganizationResponse();
        response.setOrganizationId(organization.getOrganizationId());
        response.setDisplayNameKey(organization.getDisplayNameKey());
        response.setDefault(organization.isDefault());
        response.setOrderNumber(organization.getOrderNumber());
        response.setDefaultCredentialName(organization.getDefaultCredentialName());
        response.setDefaultOtpName(organization.getDefaultOtpName());
        return response;
    }

    /**
     * Get an organization detail.
     * @param request Get organization detail request.
     * @return Get organization detail response.
     * @throws OrganizationNotFoundException Thrown when organization is not found.
     */
    @Transactional
    public GetOrganizationDetailResponse getOrganizationDetail(GetOrganizationDetailRequest request) throws OrganizationNotFoundException {
        final Optional<OrganizationEntity> organizationOptional = organizationRepository.findById(request.getOrganizationId());
        if (!organizationOptional.isPresent()) {
            throw new OrganizationNotFoundException("Organization not found: " + request.getOrganizationId());
        }
        final OrganizationEntity organization = organizationOptional.get();
        return organizationConverter.fromOrganizationEntity(organization);
    }

    /**
     * Get organization list.
     * @return Get organization list response.
     */
    @Transactional
    public GetOrganizationListResponse getOrganizationList() {
        final GetOrganizationListResponse response = new GetOrganizationListResponse();
        final List<OrganizationEntity> organizations = organizationRepository.findAllByOrderByOrderNumber();
        for (OrganizationEntity organization: organizations) {
            final GetOrganizationDetailResponse orgResponse = organizationConverter.fromOrganizationEntity(organization);
            response.getOrganizations().add(orgResponse);
        }
        return response;
    }

    /**
     * Delete an organization.
     * @param request Delete organization request.
     * @return Delete organization response.
     * @throws OrganizationNotFoundException Thrown when organization is not found.
     * @throws DeleteNotAllowedException Thrown when record cannot be deleted.
     */
    @Transactional
    public DeleteOrganizationResponse deleteOrganization(DeleteOrganizationRequest request) throws OrganizationNotFoundException, DeleteNotAllowedException {
        final Optional<OrganizationEntity> organizationOptional = organizationRepository.findById(request.getOrganizationId());
        if (!organizationOptional.isPresent()) {
            throw new OrganizationNotFoundException("Organization not found: " + request.getOrganizationId());
        }
        final OrganizationEntity organization = organizationOptional.get();
        organizationRepository.delete(organization);
        logger.debug("Organization was deleted: {}", organization.getOrganizationId());
        audit.info("Organization was deleted", AuditDetail.builder()
                .type(AUDIT_TYPE_CONFIGURATION)
                .param("organizationId", organization.getOrganizationId())
                .build());
        final DeleteOrganizationResponse response = new DeleteOrganizationResponse();
        response.setOrganizationId(organization.getOrganizationId());
        return response;
    }
}