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
import io.getlime.security.powerauth.app.nextstep.repository.model.entity.ApplicationEntity;
import io.getlime.security.powerauth.lib.nextstep.model.entity.ApplicationDetail;
import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.ApplicationStatus;
import io.getlime.security.powerauth.lib.nextstep.model.exception.ApplicationAlreadyExistsException;
import io.getlime.security.powerauth.lib.nextstep.model.exception.ApplicationNotFoundException;
import io.getlime.security.powerauth.lib.nextstep.model.request.CreateApplicationRequest;
import io.getlime.security.powerauth.lib.nextstep.model.request.DeleteApplicationRequest;
import io.getlime.security.powerauth.lib.nextstep.model.request.GetApplicationListRequest;
import io.getlime.security.powerauth.lib.nextstep.model.request.UpdateApplicationRequest;
import io.getlime.security.powerauth.lib.nextstep.model.response.CreateApplicationResponse;
import io.getlime.security.powerauth.lib.nextstep.model.response.DeleteApplicationResponse;
import io.getlime.security.powerauth.lib.nextstep.model.response.GetApplicationListResponse;
import io.getlime.security.powerauth.lib.nextstep.model.response.UpdateApplicationResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Date;
import java.util.Optional;

/**
 * This service handles persistence of Next Step applications.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Service
public class ApplicationService {

    private final ApplicationRepository applicationRepository;

    private final Logger logger = LoggerFactory.getLogger(ApplicationService.class);

    @Autowired
    public ApplicationService(ApplicationRepository applicationRepository) {
        this.applicationRepository = applicationRepository;
    }

    @Transactional
    public CreateApplicationResponse createApplication(CreateApplicationRequest request) throws ApplicationAlreadyExistsException {
        Optional<ApplicationEntity> applicationOptional = applicationRepository.findByName(request.getApplicationName());
        if (applicationOptional.isPresent()) {
            throw new ApplicationAlreadyExistsException("Application already exists: " + request.getApplicationName());
        }
        ApplicationEntity application = new ApplicationEntity();
        application.setName(request.getApplicationName());
        application.setDescription(request.getDescription());
        // TODO - lookup organization and add reference
        application.setStatus(ApplicationStatus.ACTIVE);
        application.setTimestampCreated(new Date());
        applicationRepository.save(application);
        CreateApplicationResponse response = new CreateApplicationResponse();
        response.setApplicationName(application.getName());
        response.setDescription(application.getDescription());
        response.setApplicationStatus(application.getStatus());
        // TODO - set organization
        response.setOrganizationId(null);
        return response;
    }

    @Transactional
    public UpdateApplicationResponse updateApplication(UpdateApplicationRequest request) throws ApplicationNotFoundException {
        Optional<ApplicationEntity> applicationOptional = applicationRepository.findByName(request.getApplicationName());
        if (!applicationOptional.isPresent()) {
            throw new ApplicationNotFoundException("Application not found: " + request.getApplicationName());
        }
        ApplicationEntity application = applicationOptional.get();
        if (application.getStatus() != ApplicationStatus.ACTIVE) {
            throw new ApplicationNotFoundException("Application is not ACTIVE: " + request.getApplicationName());
        }
        application.setDescription(request.getDescription());
        // TODO - lookup organization and add reference
        application.setTimestampLastUpdated(new Date());
        applicationRepository.save(application);
        UpdateApplicationResponse response = new UpdateApplicationResponse();
        response.setApplicationName(application.getName());
        response.setDescription(application.getDescription());
        response.setApplicationStatus(application.getStatus());
        // TODO - set organization
        response.setOrganizationId(null);
        return response;
    }

    @Transactional
    public GetApplicationListResponse getApplicationList(GetApplicationListRequest request) {
        Iterable<ApplicationEntity> applications = applicationRepository.findApplicationsByStatus(ApplicationStatus.ACTIVE);
        GetApplicationListResponse response = new GetApplicationListResponse();
        for (ApplicationEntity application: applications) {
            // TODO - use converter
            ApplicationDetail applicationDetail = new ApplicationDetail();
            applicationDetail.setApplicationName(application.getName());
            applicationDetail.setDescription(application.getDescription());
            applicationDetail.setApplicationStatus(application.getStatus());
            applicationDetail.setTimestampCreated(application.getTimestampCreated());
            applicationDetail.setTimestampLastUpdated(application.getTimestampLastUpdated());
            response.getApplications().add(applicationDetail);
        }
        return response;
    }

    @Transactional
    public DeleteApplicationResponse deleteApplication(DeleteApplicationRequest request) throws ApplicationNotFoundException {
        Optional<ApplicationEntity> applicationOptional = applicationRepository.findByName(request.getApplicationName());
        if (!applicationOptional.isPresent()) {
            throw new ApplicationNotFoundException("Application not found: " + request.getApplicationName());
        }
        ApplicationEntity application = applicationOptional.get();
        application.setStatus(ApplicationStatus.REMOVED);
        applicationRepository.save(application);
        DeleteApplicationResponse response = new DeleteApplicationResponse();
        response.setApplicationName(application.getName());
        response.setApplicationStatus(application.getStatus());
        return response;
    }

}
