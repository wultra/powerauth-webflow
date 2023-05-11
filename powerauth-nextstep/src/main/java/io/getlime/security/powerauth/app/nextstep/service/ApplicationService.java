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
import io.getlime.security.powerauth.app.nextstep.converter.ApplicationConverter;
import io.getlime.security.powerauth.app.nextstep.repository.ApplicationRepository;
import io.getlime.security.powerauth.app.nextstep.repository.catalogue.RepositoryCatalogue;
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
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Optional;

/**
 * This service handles persistence of Next Step applications.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Service
public class ApplicationService {

    private final Logger logger = LoggerFactory.getLogger(ApplicationService.class);
    private static final String AUDIT_TYPE_CONFIGURATION = "CONFIGURATION";

    private final ApplicationRepository applicationRepository;
    private final Audit audit;

    private final ApplicationConverter applicationConverter = new ApplicationConverter();

    /**
     * Application service constructor.
     * @param repositoryCatalogue Repository catalogue.
     * @param audit Audit interface.
     */
    @Autowired
    public ApplicationService(RepositoryCatalogue repositoryCatalogue, Audit audit) {
        this.applicationRepository = repositoryCatalogue.getApplicationRepository();
        this.audit = audit;
    }

    /**
     * Create an application.
     * @param request Create application request.
     * @return Create application response.
     * @throws ApplicationAlreadyExistsException Thrown when application already exists.
     */
    @Transactional
    public CreateApplicationResponse createApplication(CreateApplicationRequest request) throws ApplicationAlreadyExistsException {
        final Optional<ApplicationEntity> applicationOptional = applicationRepository.findByName(request.getApplicationName());
        if (applicationOptional.isPresent()) {
            throw new ApplicationAlreadyExistsException("Application already exists: " + request.getApplicationName());
        }
        ApplicationEntity application = new ApplicationEntity();
        application.setName(request.getApplicationName());
        application.setDescription(request.getDescription());
        application.setStatus(ApplicationStatus.ACTIVE);
        application.setTimestampCreated(new Date());
        application = applicationRepository.save(application);
        logger.debug("Application was created, application ID: {}, application name: {}", application.getApplicationId(), application.getName());
        audit.info("Application was created", AuditDetail.builder()
                .type(AUDIT_TYPE_CONFIGURATION)
                .param("application", application)
                .build());
        final CreateApplicationResponse response = new CreateApplicationResponse();
        response.setApplicationName(application.getName());
        response.setDescription(application.getDescription());
        response.setApplicationStatus(application.getStatus());
        return response;
    }

    /**
     * Update an application.
     * @param request Update application request.
     * @return Update application response.
     * @throws ApplicationNotFoundException Thrown when application is not found.
     */
    @Transactional
    public UpdateApplicationResponse updateApplication(UpdateApplicationRequest request) throws ApplicationNotFoundException {
        ApplicationEntity application = applicationRepository.findByName(request.getApplicationName()).orElseThrow(() ->
                new ApplicationNotFoundException("Application not found: " + request.getApplicationName()));
        if (application.getStatus() != ApplicationStatus.ACTIVE && request.getApplicationStatus() != ApplicationStatus.ACTIVE) {
            throw new ApplicationNotFoundException("Application is not ACTIVE: " + request.getApplicationName());
        }
        application.setDescription(request.getDescription());
        if (request.getApplicationStatus() != null) {
            application.setStatus(request.getApplicationStatus());
        }
        application.setTimestampLastUpdated(new Date());
        application = applicationRepository.save(application);
        logger.debug("Application was updated, application ID: {}, application name: {}", application.getApplicationId(), application.getName());
        audit.info("Application was updated", AuditDetail.builder()
                .type(AUDIT_TYPE_CONFIGURATION)
                .param("application", application)
                .build());
        final UpdateApplicationResponse response = new UpdateApplicationResponse();
        response.setApplicationName(application.getName());
        response.setDescription(application.getDescription());
        response.setApplicationStatus(application.getStatus());
        return response;
    }

    /**
     * Get application list.
     * @param request Get application list request.
     * @return Get application list response.
     */
    @Transactional
    public GetApplicationListResponse getApplicationList(GetApplicationListRequest request) {
        final Iterable<ApplicationEntity> applications;
        if (request.isIncludeRemoved()) {
            applications = applicationRepository.findAll();
        } else {
            applications = applicationRepository.findApplicationsByStatus(ApplicationStatus.ACTIVE);
        }
        final GetApplicationListResponse response = new GetApplicationListResponse();
        for (ApplicationEntity application: applications) {
            final ApplicationDetail applicationDetail = applicationConverter.fromEntity(application);
            response.getApplications().add(applicationDetail);
        }
        return response;
    }

    /**
     * Delete an application.
     * @param request Delete application request.
     * @return Delete application response.
     * @throws ApplicationNotFoundException Thrown when application is not found.
     */
    @Transactional
    public DeleteApplicationResponse deleteApplication(DeleteApplicationRequest request) throws ApplicationNotFoundException {
        ApplicationEntity application = applicationRepository.findByName(request.getApplicationName()).orElseThrow(() ->
                new ApplicationNotFoundException("Application not found: " + request.getApplicationName()));
        application.setStatus(ApplicationStatus.REMOVED);
        application.setTimestampLastUpdated(new Date());
        application = applicationRepository.save(application);
        logger.debug("Application was removed, application ID: {}, application name: {}", application.getApplicationId(), application.getName());
        audit.info("Application was removed", AuditDetail.builder()
                .type(AUDIT_TYPE_CONFIGURATION)
                .param("application", application)
                .build());
        final DeleteApplicationResponse response = new DeleteApplicationResponse();
        response.setApplicationName(application.getName());
        response.setApplicationStatus(application.getStatus());
        return response;
    }

}
