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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for credential policy definitions.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@RestController
@RequestMapping("credential/policy")
public class CredentialPolicyController {

    private static final Logger logger = LoggerFactory.getLogger(CredentialPolicyController.class);

    @RequestMapping(method = RequestMethod.POST)
    public ObjectResponse<CreateCredentialPolicyResponse> createCredentialPolicy(@RequestBody ObjectRequest<CreateCredentialPolicyRequest> request) {
        return new ObjectResponse<>(new CreateCredentialPolicyResponse());
    }

    @RequestMapping(method = RequestMethod.PUT)
    public ObjectResponse<UpdateCredentialPolicyResponse> updateCredentialPolicy(@RequestBody ObjectRequest<UpdateCredentialPolicyRequest> request) {
        return new ObjectResponse<>(new UpdateCredentialPolicyResponse());
    }

    @RequestMapping(value = "update", method = RequestMethod.POST)
    public ObjectResponse<UpdateCredentialPolicyResponse> updateCredentialPolicyPost(@RequestBody ObjectRequest<UpdateCredentialPolicyRequest> request) {
        return new ObjectResponse<>(new UpdateCredentialPolicyResponse());
    }

    @RequestMapping(value = "list", method = RequestMethod.POST)
    public ObjectResponse<GetCredentialPolicyListResponse> listCredentialPolicies(@RequestBody ObjectRequest<GetCredentialPolicyListRequest> request) {
        return new ObjectResponse<>(new GetCredentialPolicyListResponse());
    }

    @RequestMapping(value = "delete", method = RequestMethod.POST)
    public ObjectResponse<DeleteCredentialPolicyResponse> deleteCredentialPolicy(@RequestBody ObjectRequest<DeleteCredentialPolicyRequest> request) {
        return new ObjectResponse<>(new DeleteCredentialPolicyResponse());
    }

}
