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
import io.getlime.security.powerauth.lib.nextstep.model.request.*;
import io.getlime.security.powerauth.lib.nextstep.model.response.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    @RequestMapping(method = RequestMethod.POST)
    public ObjectResponse<CreateCredentialResponse> createCredential(@RequestBody ObjectRequest<CreateCredentialRequest> request) {
        return new ObjectResponse<>(new CreateCredentialResponse());
    }

    @RequestMapping(method = RequestMethod.PUT)
    public ObjectResponse<UpdateCredentialResponse> updateCredential(@RequestBody ObjectRequest<UpdateCredentialRequest> request) {
        return new ObjectResponse<>(new UpdateCredentialResponse());
    }

    @RequestMapping(value = "update", method = RequestMethod.POST)
    public ObjectResponse<UpdateCredentialResponse> updateCredentialPost(@RequestBody ObjectRequest<UpdateCredentialRequest> request) {
        return new ObjectResponse<>(new UpdateCredentialResponse());
    }

    @RequestMapping(value = "verify", method = RequestMethod.POST)
    public ObjectResponse<VerifyCredentialResponse> verifyCredential(@RequestBody ObjectRequest<VerifyCredentialRequest> request) {
        return new ObjectResponse<>(new VerifyCredentialResponse());
    }

    @RequestMapping(value = "reset", method = RequestMethod.POST)
    public ObjectResponse<ResetCredentialResponse> resetCredential(@RequestBody ObjectRequest<ResetCredentialRequest> request) {
        return new ObjectResponse<>(new ResetCredentialResponse());
    }

    @RequestMapping(value = "delete", method = RequestMethod.POST)
    public ObjectResponse<DeleteCredentialResponse> deleteCredential(@RequestBody ObjectRequest<DeleteCredentialRequest> request) {
        return new ObjectResponse<>(new DeleteCredentialResponse());
    }

    @RequestMapping(value = "block", method = RequestMethod.POST)
    public ObjectResponse<BlockCredentialResponse> blockCredential(@RequestBody ObjectRequest<BlockCredentialRequest> request) {
        return new ObjectResponse<>(new BlockCredentialResponse());
    }

    @RequestMapping(value = "unblock", method = RequestMethod.POST)
    public ObjectResponse<UnblockCredentialResponse> unblockCredential(@RequestBody ObjectRequest<UnblockCredentialRequest> request) {
        return new ObjectResponse<>(new UnblockCredentialResponse());
    }

}
