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
import io.getlime.security.powerauth.app.nextstep.exception.ObjectRequestValidator;
import io.getlime.security.powerauth.app.nextstep.service.CredentialDefinitionService;
import io.getlime.security.powerauth.lib.nextstep.model.exception.*;
import io.getlime.security.powerauth.lib.nextstep.model.request.CreateCredentialDefinitionRequest;
import io.getlime.security.powerauth.lib.nextstep.model.request.DeleteCredentialDefinitionRequest;
import io.getlime.security.powerauth.lib.nextstep.model.request.GetCredentialDefinitionListRequest;
import io.getlime.security.powerauth.lib.nextstep.model.request.UpdateCredentialDefinitionRequest;
import io.getlime.security.powerauth.lib.nextstep.model.response.CreateCredentialDefinitionResponse;
import io.getlime.security.powerauth.lib.nextstep.model.response.DeleteCredentialDefinitionResponse;
import io.getlime.security.powerauth.lib.nextstep.model.response.GetCredentialDefinitionListResponse;
import io.getlime.security.powerauth.lib.nextstep.model.response.UpdateCredentialDefinitionResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * REST controller for credential definitions.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@RestController
@RequestMapping("credential/definition")
public class CredentialDefinitionController {

    private static final Logger logger = LoggerFactory.getLogger(CredentialDefinitionController.class);

    private final CredentialDefinitionService credentialDefinitionService;
    private final ObjectRequestValidator requestValidator;

    /**
     * REST controller constructor.
     * @param credentialDefinitionService Credential definition service.
     * @param requestValidator Request validator.
     */
    @Autowired
    public CredentialDefinitionController(CredentialDefinitionService credentialDefinitionService, ObjectRequestValidator requestValidator) {
        this.credentialDefinitionService = credentialDefinitionService;
        this.requestValidator = requestValidator;
    }

    /**
     * Initialize the request validator.
     * @param binder Data binder.
     */
    @InitBinder
    private void initBinder(WebDataBinder binder) {
        binder.setValidator(requestValidator);
    }

    /**
     * Create a credential definition.
     * @param request Create credential definition request.
     * @return Create credential definition response.
     * @throws CredentialDefinitionAlreadyExistsException Thrown when credential definition already exists.
     * @throws ApplicationNotFoundException Thrown when application is not found.
     * @throws HashConfigNotFoundException Thrown when hashing configuration is not found.
     * @throws CredentialPolicyNotFoundException Thrown when credential policy is not found.
     */
    @RequestMapping(method = RequestMethod.POST)
    public ObjectResponse<CreateCredentialDefinitionResponse> createCredentialDefinition(@Valid @RequestBody ObjectRequest<CreateCredentialDefinitionRequest> request) throws CredentialDefinitionAlreadyExistsException, ApplicationNotFoundException, HashConfigNotFoundException, CredentialPolicyNotFoundException {
        CreateCredentialDefinitionResponse response = credentialDefinitionService.createCredentialDefinition(request.getRequestObject());
        return new ObjectResponse<>(response);
    }

    /**
     * Update a credential definition via PUT method.
     * @param request Update credential definition request.
     * @return Update credential definition response.
     * @throws CredentialDefinitionNotFoundException Thrown when credential definition is not found.
     * @throws ApplicationNotFoundException Thrown when application is not found.
     * @throws HashConfigNotFoundException Thrown when hashing configuration is not found.
     * @throws CredentialPolicyNotFoundException Thrown when credential policy is not found.
     */
    @RequestMapping(method = RequestMethod.PUT)
    public ObjectResponse<UpdateCredentialDefinitionResponse> updateCredentialDefinition(@Valid @RequestBody ObjectRequest<UpdateCredentialDefinitionRequest> request) throws CredentialDefinitionNotFoundException, ApplicationNotFoundException, HashConfigNotFoundException, CredentialPolicyNotFoundException {
        UpdateCredentialDefinitionResponse response = credentialDefinitionService.updateCredentialDefinition(request.getRequestObject());
        return new ObjectResponse<>(response);
    }

    /**
     * Update a credential definition via POST method.
     * @param request Update credential definition request.
     * @return Update credential definition response.
     * @throws CredentialDefinitionNotFoundException Thrown when credential definition is not found.
     * @throws ApplicationNotFoundException Thrown when application is not found.
     * @throws HashConfigNotFoundException Thrown when hashing configuration is not found.
     * @throws CredentialPolicyNotFoundException Thrown when credential policy is not found.
     */
    @RequestMapping(value = "update", method = RequestMethod.POST)
    public ObjectResponse<UpdateCredentialDefinitionResponse> updateCredentialDefinitionPost(@Valid @RequestBody ObjectRequest<UpdateCredentialDefinitionRequest> request) throws CredentialDefinitionNotFoundException, ApplicationNotFoundException, HashConfigNotFoundException, CredentialPolicyNotFoundException {
        UpdateCredentialDefinitionResponse response = credentialDefinitionService.updateCredentialDefinition(request.getRequestObject());
        return new ObjectResponse<>(response);
    }

    /**
     * Get credential definition list.
     * @param request Get credential definition list request.
     * @return Get credential definition list response.
     */
    @RequestMapping(value = "list", method = RequestMethod.POST)
    public ObjectResponse<GetCredentialDefinitionListResponse> getCredentialDefinitionList(@Valid @RequestBody ObjectRequest<GetCredentialDefinitionListRequest> request) {
        GetCredentialDefinitionListResponse response = credentialDefinitionService.getCredentialDefinitionList(request.getRequestObject());
        return new ObjectResponse<>(response);
    }

    /**
     * Delete a credential definition.
     * @param request Delete credential definition request.
     * @return Delete credential definition response.
     * @throws CredentialDefinitionNotFoundException Thrown when credential definition is not found.
     */
    @RequestMapping(value = "delete", method = RequestMethod.POST)
    public ObjectResponse<DeleteCredentialDefinitionResponse> deleteCredentialDefinition(@Valid @RequestBody ObjectRequest<DeleteCredentialDefinitionRequest> request) throws CredentialDefinitionNotFoundException {
        DeleteCredentialDefinitionResponse response = credentialDefinitionService.deleteCredentialDefinition(request.getRequestObject());
        return new ObjectResponse<>(response);
    }

}
