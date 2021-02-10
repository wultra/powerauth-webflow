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

/**
 * REST controller for Next Step applications.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@RestController
@RequestMapping("application")
public class ApplicationController {

    private final ApplicationService applicationService;

    private static final Logger logger = LoggerFactory.getLogger(ApplicationController.class);

    @Autowired
    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @RequestMapping(method = RequestMethod.POST)
    public ObjectResponse<CreateApplicationResponse> createApplication(@RequestBody ObjectRequest<CreateApplicationRequest> request) throws ApplicationAlreadyExistsException {
        // TODO - request validation
        CreateApplicationResponse response = applicationService.createApplication(request.getRequestObject());
        return new ObjectResponse<>(response);
    }

    @RequestMapping(method = RequestMethod.PUT)
    public ObjectResponse<UpdateApplicationResponse> updateApplication(@RequestBody ObjectRequest<UpdateApplicationRequest> request) throws ApplicationNotFoundException {
        // TODO - request validation
        UpdateApplicationResponse response = applicationService.updateApplication(request.getRequestObject());
        return new ObjectResponse<>(response);
    }

    @RequestMapping(value = "update", method = RequestMethod.POST)
    public ObjectResponse<UpdateApplicationResponse> updateApplicationPost(@RequestBody ObjectRequest<UpdateApplicationRequest> request) throws ApplicationNotFoundException {
        // TODO - request validation
        UpdateApplicationResponse response = applicationService.updateApplication(request.getRequestObject());
        return new ObjectResponse<>(response);
    }

    @RequestMapping(value = "list", method = RequestMethod.POST)
    public ObjectResponse<GetApplicationListResponse> listApplications(@RequestBody ObjectRequest<GetApplicationListRequest> request) {
        GetApplicationListResponse response = applicationService.getApplicationList(request.getRequestObject());
        return new ObjectResponse<>(response);
    }

    @RequestMapping(value = "delete", method = RequestMethod.POST)
    public ObjectResponse<DeleteApplicationResponse> deleteApplication(@RequestBody ObjectRequest<DeleteApplicationRequest> request) throws ApplicationNotFoundException {
        // TODO - request validation
        DeleteApplicationResponse response = applicationService.deleteApplication(request.getRequestObject());
        return new ObjectResponse<>(response);
    }

}
