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
import io.getlime.security.powerauth.app.nextstep.service.CredentialService;
import io.getlime.security.powerauth.lib.nextstep.model.exception.*;
import io.getlime.security.powerauth.lib.nextstep.model.request.*;
import io.getlime.security.powerauth.lib.nextstep.model.response.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for credential management.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@RestController
@RequestMapping("credential")
public class CredentialController {

    private static final Logger logger = LoggerFactory.getLogger(CredentialController.class);

    private final CredentialService credentialService;

    @Autowired
    public CredentialController(CredentialService credentialService) {
        this.credentialService = credentialService;
    }

    @RequestMapping(method = RequestMethod.POST)
    public ObjectResponse<CreateCredentialResponse> createCredential(@RequestBody ObjectRequest<CreateCredentialRequest> request) throws UserNotFoundException, CredentialDefinitionNotFoundException, UsernameAlreadyExistsException {
        // TODO - request validation
        CreateCredentialResponse response = credentialService.createCredential(request.getRequestObject());
        return new ObjectResponse<>(response);
    }

    @RequestMapping(method = RequestMethod.PUT)
    public ObjectResponse<UpdateCredentialResponse> updateCredential(@RequestBody ObjectRequest<UpdateCredentialRequest> request) throws UserNotFoundException, CredentialDefinitionNotFoundException, CredentialNotFoundException {
        // TODO - request validation
        UpdateCredentialResponse response = credentialService.updateCredential(request.getRequestObject());
        return new ObjectResponse<>(response);
    }

    @RequestMapping(value = "update", method = RequestMethod.POST)
    public ObjectResponse<UpdateCredentialResponse> updateCredentialPost(@RequestBody ObjectRequest<UpdateCredentialRequest> request) throws UserNotFoundException, CredentialDefinitionNotFoundException, CredentialNotFoundException {
        // TODO - request validation
        UpdateCredentialResponse response = credentialService.updateCredential(request.getRequestObject());
        return new ObjectResponse<>(response);
    }

    @RequestMapping(value = "validate", method = RequestMethod.POST)
    public ObjectResponse<ValidateCredentialResponse> validateCredential(@RequestBody ObjectRequest<ValidateCredentialRequest> request) throws CredentialNotFoundException, CredentialDefinitionNotFoundException, InvalidRequestException {
        // TODO - request validation
        ValidateCredentialResponse response = credentialService.validateCredential(request.getRequestObject());
        return new ObjectResponse<>(response);
    }

    @RequestMapping(value = "reset", method = RequestMethod.POST)
    public ObjectResponse<ResetCredentialResponse> resetCredential(@RequestBody ObjectRequest<ResetCredentialRequest> request) throws UserNotFoundException, CredentialDefinitionNotFoundException, CredentialNotFoundException {
        // TODO - request validation
        ResetCredentialResponse response = credentialService.resetCredential(request.getRequestObject());
        return new ObjectResponse<>(response);
    }

    @RequestMapping(value = "delete", method = RequestMethod.POST)
    public ObjectResponse<DeleteCredentialResponse> deleteCredential(@RequestBody ObjectRequest<DeleteCredentialRequest> request) throws UserNotFoundException, CredentialDefinitionNotFoundException, CredentialNotFoundException {
        // TODO - request validation
        DeleteCredentialResponse response = credentialService.deleteCredential(request.getRequestObject());
        return new ObjectResponse<>(response);
    }

    @RequestMapping(value = "block", method = RequestMethod.POST)
    public ObjectResponse<BlockCredentialResponse> blockCredential(@RequestBody ObjectRequest<BlockCredentialRequest> request) throws UserNotFoundException, CredentialDefinitionNotFoundException, CredentialNotFoundException {
        // TODO - request validation
        BlockCredentialResponse response = credentialService.blockCredential(request.getRequestObject());
        return new ObjectResponse<>(response);
    }

    @RequestMapping(value = "unblock", method = RequestMethod.POST)
    public ObjectResponse<UnblockCredentialResponse> unblockCredential(@RequestBody ObjectRequest<UnblockCredentialRequest> request) throws UserNotFoundException, CredentialDefinitionNotFoundException, CredentialNotFoundException {
        // TODO - request validation
        UnblockCredentialResponse response = credentialService.unblockCredential(request.getRequestObject());
        return new ObjectResponse<>(response);
    }

}
