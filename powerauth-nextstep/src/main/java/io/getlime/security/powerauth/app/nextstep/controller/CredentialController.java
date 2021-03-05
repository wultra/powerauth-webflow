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
import io.getlime.security.powerauth.app.nextstep.service.CredentialService;
import io.getlime.security.powerauth.lib.nextstep.model.exception.*;
import io.getlime.security.powerauth.lib.nextstep.model.request.*;
import io.getlime.security.powerauth.lib.nextstep.model.response.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

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
    private final ObjectRequestValidator requestValidator;

    /**
     * REST controller constructor.
     * @param credentialService Credential service.
     * @param requestValidator Request validator.
     */
    @Autowired
    public CredentialController(CredentialService credentialService, ObjectRequestValidator requestValidator) {
        this.credentialService = credentialService;
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
     * Create a credential.
     * @param request Create credential request.
     * @return Create credential response.
     * @throws UserNotFoundException Thrown when user identity is not found.
     * @throws CredentialDefinitionNotFoundException Thrown when credential definition is not found.
     * @throws InvalidConfigurationException Thrown when configuration is invalid.
     * @throws InvalidRequestException Thrown when request is invalid.
     * @throws CredentialValidationFailedException Thrown when credential validation fails.
     */
    @RequestMapping(method = RequestMethod.POST)
    public ObjectResponse<CreateCredentialResponse> createCredential(@Valid @RequestBody ObjectRequest<CreateCredentialRequest> request) throws UserNotFoundException, CredentialDefinitionNotFoundException, InvalidConfigurationException, InvalidRequestException, CredentialValidationFailedException {
        CreateCredentialResponse response = credentialService.createCredential(request.getRequestObject());
        return new ObjectResponse<>(response);
    }

    /**
     * Update a credential via a PUT method.
     * @param request Update credential request.
     * @return Update credential response.
     * @throws UserNotFoundException Thrown when user identity is not found.
     * @throws CredentialDefinitionNotFoundException Thrown when credential definition is not found.
     * @throws CredentialNotFoundException Thrown when credential is not found.
     * @throws InvalidRequestException Thrown when request is invalid.
     * @throws CredentialValidationFailedException Thrown when credential validation fails.
     * @throws InvalidConfigurationException Thrown when Next Step configuration is invalid.
     */
    @RequestMapping(method = RequestMethod.PUT)
    public ObjectResponse<UpdateCredentialResponse> updateCredential(@Valid @RequestBody ObjectRequest<UpdateCredentialRequest> request) throws UserNotFoundException, CredentialDefinitionNotFoundException, CredentialNotFoundException, InvalidRequestException, CredentialValidationFailedException, InvalidConfigurationException {
        UpdateCredentialResponse response = credentialService.updateCredential(request.getRequestObject());
        return new ObjectResponse<>(response);
    }

    /**
     * Update a credential via a POST method.
     * @param request Update credential request.
     * @return Update credential response.
     * @throws UserNotFoundException Thrown when user identity is not found.
     * @throws CredentialDefinitionNotFoundException Thrown when credential definition is not found.
     * @throws CredentialNotFoundException Thrown when credential is not found.
     * @throws InvalidRequestException Thrown when request is invalid.
     * @throws CredentialValidationFailedException Thrown when credential validation fails.
     * @throws InvalidConfigurationException Thrown when Next Step configuration is invalid.
     */
    @RequestMapping(value = "update", method = RequestMethod.POST)
    public ObjectResponse<UpdateCredentialResponse> updateCredentialPost(@Valid @RequestBody ObjectRequest<UpdateCredentialRequest> request) throws UserNotFoundException, CredentialDefinitionNotFoundException, CredentialNotFoundException, InvalidRequestException, CredentialValidationFailedException, InvalidConfigurationException {
        UpdateCredentialResponse response = credentialService.updateCredential(request.getRequestObject());
        return new ObjectResponse<>(response);
    }

    /**
     * Validate a credential.
     * @param request Validate credential request.
     * @return Validate credential response.
     * @throws CredentialDefinitionNotFoundException Thrown when credential definition is not found.
     * @throws InvalidRequestException Thrown when request is invalid.
     * @throws UserNotFoundException Thrown when user identity is not found.
     */
    @RequestMapping(value = "validate", method = RequestMethod.POST)
    public ObjectResponse<ValidateCredentialResponse> validateCredential(@Valid @RequestBody ObjectRequest<ValidateCredentialRequest> request) throws CredentialDefinitionNotFoundException, InvalidRequestException, UserNotFoundException, InvalidConfigurationException {
        ValidateCredentialResponse response = credentialService.validateCredential(request.getRequestObject());
        return new ObjectResponse<>(response);
    }

    /**
     * Reset a credential.
     * @param request Reset credential request.
     * @return Reset credential response.
     * @throws UserNotFoundException Thrown when user identity is not found.
     * @throws CredentialDefinitionNotFoundException Thrown when credential definition is not found.
     * @throws CredentialNotFoundException Thrown when credential is not found.
     * @throws InvalidConfigurationException Thrown when Next Step configuration is invalid.
     */
    @RequestMapping(value = "reset", method = RequestMethod.POST)
    public ObjectResponse<ResetCredentialResponse> resetCredential(@Valid @RequestBody ObjectRequest<ResetCredentialRequest> request) throws UserNotFoundException, CredentialDefinitionNotFoundException, CredentialNotFoundException, InvalidConfigurationException {
        ResetCredentialResponse response = credentialService.resetCredential(request.getRequestObject());
        return new ObjectResponse<>(response);
    }

    /**
     * Delete a credential.
     * @param request Delete credential request.
     * @return Delete credential response.
     * @throws UserNotFoundException Thrown when user identity is not found.
     * @throws CredentialDefinitionNotFoundException Thrown when credential definition is not found.
     * @throws CredentialNotFoundException Thrown when credential is not found.
     */
    @RequestMapping(value = "delete", method = RequestMethod.POST)
    public ObjectResponse<DeleteCredentialResponse> deleteCredential(@Valid @RequestBody ObjectRequest<DeleteCredentialRequest> request) throws UserNotFoundException, CredentialDefinitionNotFoundException, CredentialNotFoundException {
        DeleteCredentialResponse response = credentialService.deleteCredential(request.getRequestObject());
        return new ObjectResponse<>(response);
    }

    /**
     * Block a credential.
     * @param request Block credential request.
     * @return Block credential response.
     * @throws UserNotFoundException Thrown when user identity is not found.
     * @throws CredentialDefinitionNotFoundException Thrown when credential definition is not found.
     * @throws CredentialNotFoundException Thrown when credential is not found.
     * @throws CredentialNotActiveException Thrown when credential is not active.
     */
    @RequestMapping(value = "block", method = RequestMethod.POST)
    public ObjectResponse<BlockCredentialResponse> blockCredential(@Valid @RequestBody ObjectRequest<BlockCredentialRequest> request) throws UserNotFoundException, CredentialDefinitionNotFoundException, CredentialNotFoundException, CredentialNotActiveException {
        BlockCredentialResponse response = credentialService.blockCredential(request.getRequestObject());
        return new ObjectResponse<>(response);
    }

    /**
     * Unblock a credential.
     * @param request Unblock credential request.
     * @return Unblock credential response.
     * @throws UserNotFoundException Thrown when user identity is not found.
     * @throws CredentialDefinitionNotFoundException Thrown when credential definition is not found.
     * @throws CredentialNotFoundException Thrown when credential is not found.
     * @throws CredentialNotBlockedException Thrown when credential is not blocked.
     */
    @RequestMapping(value = "unblock", method = RequestMethod.POST)
    public ObjectResponse<UnblockCredentialResponse> unblockCredential(@Valid @RequestBody ObjectRequest<UnblockCredentialRequest> request) throws UserNotFoundException, CredentialDefinitionNotFoundException, CredentialNotFoundException, CredentialNotBlockedException {
        UnblockCredentialResponse response = credentialService.unblockCredential(request.getRequestObject());
        return new ObjectResponse<>(response);
    }

}
