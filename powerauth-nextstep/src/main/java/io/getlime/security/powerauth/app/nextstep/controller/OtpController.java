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
import io.getlime.security.powerauth.lib.nextstep.model.request.CreateOtpRequest;
import io.getlime.security.powerauth.lib.nextstep.model.request.DeleteOtpRequest;
import io.getlime.security.powerauth.lib.nextstep.model.response.CreateOtpResponse;
import io.getlime.security.powerauth.lib.nextstep.model.response.DeleteOtpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for OTP management.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@RestController
@RequestMapping("otp")
public class OtpController {

    private static final Logger logger = LoggerFactory.getLogger(OtpController.class);

    @RequestMapping(method = RequestMethod.POST)
    public ObjectResponse<CreateOtpResponse> createOtp(@RequestBody ObjectRequest<CreateOtpRequest> request) {
        return new ObjectResponse<>(new CreateOtpResponse());
    }

    @RequestMapping(value = "delete", method = RequestMethod.POST)
    public @ResponseBody ObjectResponse<DeleteOtpResponse> deleteOtp(@RequestBody ObjectRequest<DeleteOtpRequest> request) {
        return new ObjectResponse<>(new DeleteOtpResponse());
    }

}
