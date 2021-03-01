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
import io.getlime.security.powerauth.app.nextstep.service.OtpService;
import io.getlime.security.powerauth.lib.nextstep.model.exception.*;
import io.getlime.security.powerauth.lib.nextstep.model.request.CreateOtpRequest;
import io.getlime.security.powerauth.lib.nextstep.model.request.DeleteOtpRequest;
import io.getlime.security.powerauth.lib.nextstep.model.request.GetOtpDetailRequest;
import io.getlime.security.powerauth.lib.nextstep.model.request.GetOtpListRequest;
import io.getlime.security.powerauth.lib.nextstep.model.response.CreateOtpResponse;
import io.getlime.security.powerauth.lib.nextstep.model.response.DeleteOtpResponse;
import io.getlime.security.powerauth.lib.nextstep.model.response.GetOtpDetailResponse;
import io.getlime.security.powerauth.lib.nextstep.model.response.GetOtpListResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

    private final OtpService otpService;

    @Autowired
    public OtpController(OtpService otpService) {
        this.otpService = otpService;
    }

    @RequestMapping(method = RequestMethod.POST)
    public ObjectResponse<CreateOtpResponse> createOtp(@RequestBody ObjectRequest<CreateOtpRequest> request) throws OtpDefinitionNotFoundException, CredentialDefinitionNotFoundException, OperationNotFoundException, InvalidRequestException, OtpGenAlgorithmNotSupportedException, InvalidConfigurationException, OperationAlreadyFinishedException, OperationAlreadyFailedException, UserNotActiveException, CredentialNotActiveException, CredentialNotFoundException {
        // TODO - request validation
        CreateOtpResponse response = otpService.createOtp(request.getRequestObject());
        return new ObjectResponse<>(response);
    }

    @RequestMapping(value = "list", method = RequestMethod.POST)
    public ObjectResponse<GetOtpListResponse> getOptList(@RequestBody ObjectRequest<GetOtpListRequest> request) throws OperationNotFoundException {
        // TODO - request validation
        GetOtpListResponse response = otpService.getOtpList(request.getRequestObject());
        return new ObjectResponse<>(response);
    }

    @RequestMapping(value = "detail", method = RequestMethod.POST)
    public ObjectResponse<GetOtpDetailResponse> getOtpDetail(@RequestBody ObjectRequest<GetOtpDetailRequest> request) throws OperationNotFoundException, InvalidRequestException, OtpNotFoundException {
        // TODO - request validation
        GetOtpDetailResponse response = otpService.getOtpDetail(request.getRequestObject());
        return new ObjectResponse<>(response);
    }

    @RequestMapping(value = "delete", method = RequestMethod.POST)
    public @ResponseBody ObjectResponse<DeleteOtpResponse> deleteOtp(@RequestBody ObjectRequest<DeleteOtpRequest> request) throws OtpNotFoundException, InvalidRequestException, OperationNotFoundException {
        // TODO - request validation
        DeleteOtpResponse response = otpService.deleteOtp(request.getRequestObject());
        return new ObjectResponse<>(response);
    }

}
