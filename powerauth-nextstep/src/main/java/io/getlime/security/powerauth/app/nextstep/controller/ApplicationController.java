/*
 * Copyright 2021 Wultra s.r.o.
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
import io.getlime.security.powerauth.app.nextstep.service.ApplicationService;
import io.getlime.security.powerauth.lib.nextstep.model.exception.ApplicationAlreadyExistsException;
import io.getlime.security.powerauth.lib.nextstep.model.exception.ApplicationNotFoundException;
import io.getlime.security.powerauth.lib.nextstep.model.exception.OrganizationNotFoundException;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * REST controller for Next Step applications.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@RestController
@RequestMapping("application")
public class ApplicationController {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationController.class);

    private final ApplicationService applicationService;

    /**
     * REST controller constructor.
     * @param applicationService Application service.
     */
    @Autowired
    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    /**
     * Create an application.
     * @param request Create application request.
     * @return Create application response.
     * @throws ApplicationAlreadyExistsException Thrown when application already exists.
     * @throws OrganizationNotFoundException Thrown when organization is not found.
     */
    @RequestMapping(method = RequestMethod.POST)
    public ObjectResponse<CreateApplicationResponse> createApplication(@Valid @RequestBody ObjectRequest<CreateApplicationRequest> request) throws ApplicationAlreadyExistsException, OrganizationNotFoundException {
        CreateApplicationResponse response = applicationService.createApplication(request.getRequestObject());
        return new ObjectResponse<>(response);
    }

    /**
     * Update an application via PUT method.
     * @param request Update application request.
     * @return Update application response.
     * @throws ApplicationNotFoundException Thrown when application is not found.
     * @throws OrganizationNotFoundException Thrown when organization is not found.
     */
    @RequestMapping(method = RequestMethod.PUT)
    public ObjectResponse<UpdateApplicationResponse> updateApplication(@Valid @RequestBody ObjectRequest<UpdateApplicationRequest> request) throws ApplicationNotFoundException, OrganizationNotFoundException {
        UpdateApplicationResponse response = applicationService.updateApplication(request.getRequestObject());
        return new ObjectResponse<>(response);
    }

    /**
     * Update an application via POST method.
     * @param request Update application request.
     * @return Update application response.
     * @throws ApplicationNotFoundException Thrown when application is not found.
     * @throws OrganizationNotFoundException Thrown when organization is not found.
     */
    @RequestMapping(value = "update", method = RequestMethod.POST)
    public ObjectResponse<UpdateApplicationResponse> updateApplicationPost(@Valid @RequestBody ObjectRequest<UpdateApplicationRequest> request) throws ApplicationNotFoundException, OrganizationNotFoundException {
        UpdateApplicationResponse response = applicationService.updateApplication(request.getRequestObject());
        return new ObjectResponse<>(response);
    }

    /**
     * Get application list.
     * @param request Get application list request.
     * @return Get application list response.
     */
    @RequestMapping(value = "list", method = RequestMethod.POST)
    public ObjectResponse<GetApplicationListResponse> getApplicationList(@Valid @RequestBody ObjectRequest<GetApplicationListRequest> request) {
        GetApplicationListResponse response = applicationService.getApplicationList(request.getRequestObject());
        return new ObjectResponse<>(response);
    }

    /**
     * Delete an application.
     * @param request Delete application request.
     * @return Delete application response.
     * @throws ApplicationNotFoundException Thrown when application is not found.
     */
    @RequestMapping(value = "delete", method = RequestMethod.POST)
    public ObjectResponse<DeleteApplicationResponse> deleteApplication(@Valid @RequestBody ObjectRequest<DeleteApplicationRequest> request) throws ApplicationNotFoundException {
        DeleteApplicationResponse response = applicationService.deleteApplication(request.getRequestObject());
        return new ObjectResponse<>(response);
    }

}
