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
import io.getlime.security.powerauth.lib.nextstep.model.request.CreateOtpPolicyRequest;
import io.getlime.security.powerauth.lib.nextstep.model.request.DeleteOtpPolicyRequest;
import io.getlime.security.powerauth.lib.nextstep.model.request.GetOtpPolicyListRequest;
import io.getlime.security.powerauth.lib.nextstep.model.request.UpdateOtpPolicyRequest;
import io.getlime.security.powerauth.lib.nextstep.model.response.CreateOtpPolicyResponse;
import io.getlime.security.powerauth.lib.nextstep.model.response.DeleteOtpPolicyResponse;
import io.getlime.security.powerauth.lib.nextstep.model.response.GetOtpPolicyListResponse;
import io.getlime.security.powerauth.lib.nextstep.model.response.UpdateOtpPolicyResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for OTP policy definitions.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@RestController
@RequestMapping("otp/policy")
public class OtpPolicyController {

    private static final Logger logger = LoggerFactory.getLogger(OtpPolicyController.class);

    @RequestMapping(method = RequestMethod.POST)
    public ObjectResponse<CreateOtpPolicyResponse> createOtpPolicy(@RequestBody ObjectRequest<CreateOtpPolicyRequest> request) {
        return new ObjectResponse<>(new CreateOtpPolicyResponse());
    }

    @RequestMapping(method = RequestMethod.PUT)
    public ObjectResponse<UpdateOtpPolicyResponse> updateOtpPolicy(@RequestBody ObjectRequest<UpdateOtpPolicyRequest> request) {
        return new ObjectResponse<>(new UpdateOtpPolicyResponse());
    }

    @RequestMapping(value = "update", method = RequestMethod.POST)
    public ObjectResponse<UpdateOtpPolicyResponse> updateOtpPolicyPost(@RequestBody ObjectRequest<UpdateOtpPolicyRequest> request) {
        return new ObjectResponse<>(new UpdateOtpPolicyResponse());
    }

    @RequestMapping(value = "list", method = RequestMethod.POST)
    public ObjectResponse<GetOtpPolicyListResponse> listOtpPolicies(@RequestBody ObjectRequest<GetOtpPolicyListRequest> request) {
        return new ObjectResponse<>(new GetOtpPolicyListResponse());
    }

    @RequestMapping(value = "delete", method = RequestMethod.POST)
    public ObjectResponse<DeleteOtpPolicyResponse> deleteOtpPolicy(@RequestBody ObjectRequest<DeleteOtpPolicyRequest> request) {
        return new ObjectResponse<>(new DeleteOtpPolicyResponse());
    }

}
