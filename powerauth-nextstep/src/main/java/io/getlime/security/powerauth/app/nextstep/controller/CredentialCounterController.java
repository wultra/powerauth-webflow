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

import javax.validation.Valid;

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

    /**
     * REST controller constructor.
     * @param credentialCounterService Credential counter service.
     */
    @Autowired
    public CredentialCounterController(CredentialCounterService credentialCounterService) {
        this.credentialCounterService = credentialCounterService;
    }

    /**
     * Update a credential counter via PUT method.
     * @param request Update credential counter request.
     * @return Update credential counter response.
     * @throws UserNotFoundException Thrown when user identity is not found.
     * @throws CredentialDefinitionNotFoundException Thrown when credential definition is not found.
     * @throws InvalidRequestException Thrown when request is invalid.
     * @throws CredentialNotFoundException Thrown when credential is not found.
     * @throws CredentialNotActiveException Thrown when credential is not active.
     */
    @RequestMapping(method = RequestMethod.PUT)
    public ObjectResponse<UpdateCounterResponse> updateCredentialCounter(@Valid @RequestBody ObjectRequest<UpdateCounterRequest> request) throws UserNotFoundException, CredentialDefinitionNotFoundException, InvalidRequestException, CredentialNotFoundException, CredentialNotActiveException {
        logger.info("Received updateCredentialCounter request, user ID: {}, credential name: {}", request.getRequestObject().getUserId(), request.getRequestObject().getCredentialName());
        UpdateCounterResponse response = credentialCounterService.updateCredentialCounter(request.getRequestObject());
        logger.info("The updateCredentialCounter request succeeded, user ID: {}, credential name: {}", request.getRequestObject().getUserId(), request.getRequestObject().getCredentialName());
        return new ObjectResponse<>(response);
    }

    /**
     * Update a credential counter via POST method.
     * @param request Update credential counter request.
     * @return Update credential counter response.
     * @throws UserNotFoundException Thrown when user identity is not found.
     * @throws CredentialDefinitionNotFoundException Thrown when credential definition is not found.
     * @throws InvalidRequestException Thrown when request is invalid.
     * @throws CredentialNotFoundException Thrown when credential is not found.
     * @throws CredentialNotActiveException Thrown when credential is not active.
     */
    @RequestMapping(value = "update", method = RequestMethod.POST)
    public ObjectResponse<UpdateCounterResponse> updateCredentialCounterPost(@Valid @RequestBody ObjectRequest<UpdateCounterRequest> request) throws UserNotFoundException, CredentialDefinitionNotFoundException, InvalidRequestException, CredentialNotFoundException, CredentialNotActiveException {
        logger.info("Received updateCredentialCounterPost request, user ID: {}, credential name: {}", request.getRequestObject().getUserId(), request.getRequestObject().getCredentialName());
        UpdateCounterResponse response = credentialCounterService.updateCredentialCounter(request.getRequestObject());
        logger.info("The updateCredentialCounterPost request succeeded, user ID: {}, credential name: {}", request.getRequestObject().getUserId(), request.getRequestObject().getCredentialName());
        return new ObjectResponse<>(response);
    }

    /**
     * Reset all soft counters.
     * @param request Rest counters request.
     * @return Reset counters response.
     */
    @RequestMapping(value = "reset-all", method = RequestMethod.POST)
    public ObjectResponse<ResetCountersResponse> resetAllCounters(@Valid @RequestBody ObjectRequest<ResetCountersRequest> request) {
        logger.info("Received resetAllCounters request");
        ResetCountersResponse response = credentialCounterService.resetCounters(request.getRequestObject());
        logger.info("The resetAllCounters request succeeded");
        return new ObjectResponse<>(response);
    }

}
