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
import io.getlime.security.powerauth.app.nextstep.service.CredentialCounterService;
import io.getlime.security.powerauth.lib.nextstep.model.exception.*;
import io.getlime.security.powerauth.lib.nextstep.model.request.ResetCountersRequest;
import io.getlime.security.powerauth.lib.nextstep.model.request.UpdateCounterRequest;
import io.getlime.security.powerauth.lib.nextstep.model.response.ResetCountersResponse;
import io.getlime.security.powerauth.lib.nextstep.model.response.UpdateCounterResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for counter management.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@RestController
@RequestMapping("credential/counter")
public class CredentialCounterController {

    private static final Logger logger = LoggerFactory.getLogger(CredentialCounterController.class);

    private final CredentialCounterService credentialCounterService;

    @Autowired
    public CredentialCounterController(CredentialCounterService credentialCounterService) {
        this.credentialCounterService = credentialCounterService;
    }

    @RequestMapping(method = RequestMethod.PUT)
    public ObjectResponse<UpdateCounterResponse> updateCredentialCounter(@RequestBody ObjectRequest<UpdateCounterRequest> request) throws UserNotFoundException, CredentialDefinitionNotFoundException, InvalidRequestException, CredentialNotFoundException, CredentialNotActiveException {
        // TODO - request validation
        UpdateCounterResponse response = credentialCounterService.updateCredentialCounter(request.getRequestObject());
        return new ObjectResponse<>(response);
    }

    @RequestMapping(value = "update", method = RequestMethod.POST)
    public ObjectResponse<UpdateCounterResponse> updateCredentialCounterPost(@RequestBody ObjectRequest<UpdateCounterRequest> request) throws UserNotFoundException, CredentialDefinitionNotFoundException, InvalidRequestException, CredentialNotFoundException, CredentialNotActiveException {
        // TODO - request validation
        UpdateCounterResponse response = credentialCounterService.updateCredentialCounter(request.getRequestObject());
        return new ObjectResponse<>(response);
    }

    @RequestMapping(value = "reset-all", method = RequestMethod.POST)
    public ObjectResponse<ResetCountersResponse> resetAllCounters(@RequestBody ObjectRequest<ResetCountersRequest> request) {
        // TODO - request validation
        ResetCountersResponse response = credentialCounterService.resetCounters(request.getRequestObject());
        return new ObjectResponse<>(response);
    }

}
