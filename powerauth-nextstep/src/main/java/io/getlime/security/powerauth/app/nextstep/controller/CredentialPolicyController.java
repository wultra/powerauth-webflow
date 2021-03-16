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
import io.getlime.security.powerauth.app.nextstep.service.CredentialPolicyService;
import io.getlime.security.powerauth.lib.nextstep.model.exception.CredentialPolicyAlreadyExistsException;
import io.getlime.security.powerauth.lib.nextstep.model.exception.CredentialPolicyNotFoundException;
import io.getlime.security.powerauth.lib.nextstep.model.exception.InvalidConfigurationException;
import io.getlime.security.powerauth.lib.nextstep.model.exception.InvalidRequestException;
import io.getlime.security.powerauth.lib.nextstep.model.request.CreateCredentialPolicyRequest;
import io.getlime.security.powerauth.lib.nextstep.model.request.DeleteCredentialPolicyRequest;
import io.getlime.security.powerauth.lib.nextstep.model.request.GetCredentialPolicyListRequest;
import io.getlime.security.powerauth.lib.nextstep.model.request.UpdateCredentialPolicyRequest;
import io.getlime.security.powerauth.lib.nextstep.model.response.CreateCredentialPolicyResponse;
import io.getlime.security.powerauth.lib.nextstep.model.response.DeleteCredentialPolicyResponse;
import io.getlime.security.powerauth.lib.nextstep.model.response.GetCredentialPolicyListResponse;
import io.getlime.security.powerauth.lib.nextstep.model.response.UpdateCredentialPolicyResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * REST controller for credential policy definitions.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@RestController
@RequestMapping("credential/policy")
public class CredentialPolicyController {

    private static final Logger logger = LoggerFactory.getLogger(CredentialPolicyController.class);

    private final CredentialPolicyService credentialPolicyService;

    /**
     * REST controller constructor.
     * @param credentialPolicyService Credential policy service.
     */
    @Autowired
    public CredentialPolicyController(CredentialPolicyService credentialPolicyService) {
        this.credentialPolicyService = credentialPolicyService;
    }

    /**
     * Create a credential policy.
     * @param request Create credential policy request.
     * @return Create credential policy response.
     * @throws CredentialPolicyAlreadyExistsException Thrown when credential policy already exists.
     * @throws InvalidRequestException Thrown when request is invalid.
     */
    @RequestMapping(method = RequestMethod.POST)
    public ObjectResponse<CreateCredentialPolicyResponse> createCredentialPolicy(@Valid @RequestBody ObjectRequest<CreateCredentialPolicyRequest> request) throws CredentialPolicyAlreadyExistsException, InvalidRequestException {
        CreateCredentialPolicyResponse response = credentialPolicyService.createCredentialPolicy(request.getRequestObject());
        return new ObjectResponse<>(response);
    }

    /**
     * Update a credential policy via PUT method.
     * @param request Update credential policy request.
     * @return Update credential policy response.
     * @throws CredentialPolicyNotFoundException Thrown when credential policy is not found.
     * @throws InvalidRequestException Thrown when request is invalid.
     */
    @RequestMapping(method = RequestMethod.PUT)
    public ObjectResponse<UpdateCredentialPolicyResponse> updateCredentialPolicy(@Valid @RequestBody ObjectRequest<UpdateCredentialPolicyRequest> request) throws CredentialPolicyNotFoundException, InvalidRequestException {
        UpdateCredentialPolicyResponse response = credentialPolicyService.updateCredentialPolicy(request.getRequestObject());
        return new ObjectResponse<>(response);
    }

    /**
     * Update a credential policy via POST method.
     * @param request Update credential policy request.
     * @return Update credential policy response.
     * @throws CredentialPolicyNotFoundException Thrown when credential policy is not found.
     * @throws InvalidRequestException Thrown when request is invalid.
     */
    @RequestMapping(value = "update", method = RequestMethod.POST)
    public ObjectResponse<UpdateCredentialPolicyResponse> updateCredentialPolicyPost(@Valid @RequestBody ObjectRequest<UpdateCredentialPolicyRequest> request) throws CredentialPolicyNotFoundException, InvalidRequestException {
        UpdateCredentialPolicyResponse response = credentialPolicyService.updateCredentialPolicy(request.getRequestObject());
        return new ObjectResponse<>(response);
    }

    /**
     * Get credential policy list.
     * @param request Get credential policy list request.
     * @return Get credential policy list response.
     * @throws InvalidConfigurationException Thrown when Next Step configuration is invalid.
     */
    @RequestMapping(value = "list", method = RequestMethod.POST)
    public ObjectResponse<GetCredentialPolicyListResponse> getCredentialPolicyList(@Valid @RequestBody ObjectRequest<GetCredentialPolicyListRequest> request) throws InvalidConfigurationException {
        GetCredentialPolicyListResponse response = credentialPolicyService.getCredentialPolicyList(request.getRequestObject());
        return new ObjectResponse<>(response);
    }

    /**
     * Delete a credential policy.
     * @param request Delete credential policy request.
     * @return Delete credential policy response.
     * @throws CredentialPolicyNotFoundException Thrown when credential policy is not found.
     */
    @RequestMapping(value = "delete", method = RequestMethod.POST)
    public ObjectResponse<DeleteCredentialPolicyResponse> deleteCredentialPolicy(@Valid @RequestBody ObjectRequest<DeleteCredentialPolicyRequest> request) throws CredentialPolicyNotFoundException {
        DeleteCredentialPolicyResponse response = credentialPolicyService.deleteCredentialPolicy(request.getRequestObject());
        return new ObjectResponse<>(response);
    }

}
