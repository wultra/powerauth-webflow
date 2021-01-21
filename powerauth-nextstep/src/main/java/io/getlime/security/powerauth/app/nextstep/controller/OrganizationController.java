/*
 * Copyright 2019 Wultra s.r.o.
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

package io.getlime.security.powerauth.app.nextstep.controller;

import io.getlime.core.rest.model.base.request.ObjectRequest;
import io.getlime.core.rest.model.base.response.ObjectResponse;
import io.getlime.security.powerauth.app.nextstep.converter.OrganizationConverter;
import io.getlime.security.powerauth.app.nextstep.repository.OrganizationRepository;
import io.getlime.security.powerauth.app.nextstep.repository.model.entity.OrganizationEntity;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

/**
 * REST controller class related to Next Step organizations.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@RestController
@RequestMapping(value = "organization")
public class OrganizationController {

    private static final Logger logger = LoggerFactory.getLogger(OrganizationController.class);

    private final OrganizationRepository organizationRepository;
    private final OrganizationConverter organizationConverter = new OrganizationConverter();

    /**
     * Controller constructor.
     * @param organizationRepository Organization repository.
     */
    @Autowired
    public OrganizationController(OrganizationRepository organizationRepository) {
        this.organizationRepository = organizationRepository;
    }

    @RequestMapping(method = RequestMethod.POST)
    public ObjectResponse<CreateOrganizationResponse> createOrganization(@RequestBody ObjectRequest<CreateOrganizationRequest> request) {
        return new ObjectResponse<>(new CreateOrganizationResponse());
    }

    /**
     * List organizations defined in Next Step service.
     *
     * @param request Get organizations request.
     * @return Get organizations response.
     * @throws OrganizationNotFoundException Thrown in case organization does not exist.
     */
    @RequestMapping(value = "detail", method = RequestMethod.POST)
    public ObjectResponse<GetOrganizationDetailResponse> getOrganizationDetail(@RequestBody ObjectRequest<GetOrganizationDetailRequest> request) throws OrganizationNotFoundException {
        logger.info("Received getOrganizationDetail request");
        if (request == null || request.getRequestObject() == null) {
            throw new OrganizationNotFoundException("Invalid request");
        }
        Optional<OrganizationEntity> organizationOptional = organizationRepository.findById(request.getRequestObject().getOrganizationId());
        if (!organizationOptional.isPresent()) {
            throw new OrganizationNotFoundException("Organization not found, organization ID: " + request.getRequestObject().getOrganizationId());
        }
        OrganizationEntity organization = organizationOptional.get();
        GetOrganizationDetailResponse response = organizationConverter.fromOrganizationEntity(organization);
        logger.info("The getOrganizationDetail request succeeded");
        return new ObjectResponse<>(response);
    }

    /**
     * List organizations defined in Next Step service.
     *
     * @param request Get organizations request.
     * @return Get organizations response.
     */
    @RequestMapping(value = "list", method = RequestMethod.POST)
    public ObjectResponse<GetOrganizationListResponse> getOrganizationList(@RequestBody ObjectRequest<GetOrganizationListRequest> request) {
        logger.info("Received getOrganizationList request");
        GetOrganizationListResponse response = new GetOrganizationListResponse();
        List<OrganizationEntity> organizations = organizationRepository.findAllByOrderByOrderNumber();
        for (OrganizationEntity organization: organizations) {
            GetOrganizationDetailResponse orgResponse = organizationConverter.fromOrganizationEntity(organization);
            response.addOrganization(orgResponse);
        }
        logger.info("The getOrganizationList request succeeded, number of organizations: {}", response.getOrganizations().size());
        return new ObjectResponse<>(response);
    }

    @RequestMapping(value = "delete", method = RequestMethod.POST)
    public ObjectResponse<DeleteOrganizationResponse> deleteOrganization(@RequestBody ObjectRequest<DeleteOrganizationRequest> request) {
        return new ObjectResponse<>(new DeleteOrganizationResponse());
    }


}
