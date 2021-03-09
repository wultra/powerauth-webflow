/*
 * Copyright 2017 Wultra s.r.o.
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

import io.getlime.security.powerauth.app.nextstep.controller.OrganizationController;
import io.getlime.security.powerauth.app.nextstep.converter.OrganizationConverter;
import io.getlime.security.powerauth.app.nextstep.repository.ApplicationRepository;
import io.getlime.security.powerauth.app.nextstep.repository.OperationRepository;
import io.getlime.security.powerauth.app.nextstep.repository.OrganizationRepository;
import io.getlime.security.powerauth.app.nextstep.repository.model.entity.OrganizationEntity;
import io.getlime.security.powerauth.lib.nextstep.model.exception.DeleteNotAllowedException;
import io.getlime.security.powerauth.lib.nextstep.model.exception.OrganizationAlreadyExistsException;
import io.getlime.security.powerauth.lib.nextstep.model.exception.OrganizationNotFoundException;
import io.getlime.security.powerauth.lib.nextstep.model.request.CreateOrganizationRequest;
import io.getlime.security.powerauth.lib.nextstep.model.request.DeleteOrganizationRequest;
import io.getlime.security.powerauth.lib.nextstep.model.request.GetOrganizationDetailRequest;
import io.getlime.security.powerauth.lib.nextstep.model.request.GetOrganizationListRequest;
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

    private final OrganizationRepository organizationRepository;
    private final OperationRepository operationRepository;
    private final ApplicationRepository applicationRepository;

    private final OrganizationConverter organizationConverter = new OrganizationConverter();

    /**
     * Organization service constructor.
     * @param organizationRepository Organization repository.
     * @param operationRepository Operation repository.
     * @param applicationRepository Application repository.
     */
    @Autowired
    public OrganizationService(OrganizationRepository organizationRepository, OperationRepository operationRepository, ApplicationRepository applicationRepository) {
        this.organizationRepository = organizationRepository;
        this.operationRepository = operationRepository;
        this.applicationRepository = applicationRepository;
    }

    /**
     * Create an organization.
     * @param request Create organization request.
     * @return Create organization response.
     * @throws OrganizationAlreadyExistsException Thrown when organization already exists.
     */
    @Transactional
    public CreateOrganizationResponse createOrganization(CreateOrganizationRequest request) throws OrganizationAlreadyExistsException {
        Optional<OrganizationEntity> organizationOptional = organizationRepository.findById(request.getOrganizationId());
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
        organizationRepository.save(organization);
        CreateOrganizationResponse response = new CreateOrganizationResponse();
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
        Optional<OrganizationEntity> organizationOptional = organizationRepository.findById(request.getOrganizationId());
        if (!organizationOptional.isPresent()) {
            throw new OrganizationNotFoundException("Organization not found: " + request.getOrganizationId());
        }
        OrganizationEntity organization = organizationOptional.get();
        return organizationConverter.fromOrganizationEntity(organization);
    }

    /**
     * Get organization list.
     * @param requestObject Get organization list request.
     * @return Get organization list response.
     */
    @Transactional
    public GetOrganizationListResponse getOrganizationList(GetOrganizationListRequest requestObject) {
        GetOrganizationListResponse response = new GetOrganizationListResponse();
        List<OrganizationEntity> organizations = organizationRepository.findAllByOrderByOrderNumber();
        for (OrganizationEntity organization: organizations) {
            GetOrganizationDetailResponse orgResponse = organizationConverter.fromOrganizationEntity(organization);
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
        Optional<OrganizationEntity> organizationOptional = organizationRepository.findById(request.getOrganizationId());
        if (!organizationOptional.isPresent()) {
            throw new OrganizationNotFoundException("Organization not found: " + request.getOrganizationId());
        }
        OrganizationEntity organization = organizationOptional.get();
        long operationCount = operationRepository.countByOrganization(organization);
        long applicationCount = applicationRepository.countByOrganization(organization);
        if (operationCount >0 || applicationCount > 0) {
            throw new DeleteNotAllowedException("Organization cannot be deleted because it is used: " + organization.getOrganizationId());
        }
        organizationRepository.delete(organization);
        DeleteOrganizationResponse response = new DeleteOrganizationResponse();
        response.setOrganizationId(organization.getOrganizationId());
        return response;
    }
}