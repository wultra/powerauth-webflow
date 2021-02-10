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
import io.getlime.security.powerauth.app.nextstep.service.OtpDefinitionService;
import io.getlime.security.powerauth.lib.nextstep.model.exception.ApplicationNotFoundException;
import io.getlime.security.powerauth.lib.nextstep.model.exception.OtpDefinitionAlreadyExistsException;
import io.getlime.security.powerauth.lib.nextstep.model.exception.OtpDefinitionNotFoundException;
import io.getlime.security.powerauth.lib.nextstep.model.exception.OtpPolicyNotFoundException;
import io.getlime.security.powerauth.lib.nextstep.model.request.CreateOtpDefinitionRequest;
import io.getlime.security.powerauth.lib.nextstep.model.request.DeleteOtpDefinitionRequest;
import io.getlime.security.powerauth.lib.nextstep.model.request.GetOtpDefinitionListRequest;
import io.getlime.security.powerauth.lib.nextstep.model.request.UpdateOtpDefinitionRequest;
import io.getlime.security.powerauth.lib.nextstep.model.response.CreateOtpDefinitionResponse;
import io.getlime.security.powerauth.lib.nextstep.model.response.DeleteOtpDefinitionResponse;
import io.getlime.security.powerauth.lib.nextstep.model.response.GetOtpDefinitionListResponse;
import io.getlime.security.powerauth.lib.nextstep.model.response.UpdateOtpDefinitionResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for OTP definitions.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@RestController
@RequestMapping("otp/definition")
public class OtpDefinitionController {

    private static final Logger logger = LoggerFactory.getLogger(OtpDefinitionController.class);

    private final OtpDefinitionService otpDefinitionService;

    @Autowired
    public OtpDefinitionController(OtpDefinitionService otpDefinitionService) {
        this.otpDefinitionService = otpDefinitionService;
    }

    @RequestMapping(method = RequestMethod.POST)
    public ObjectResponse<CreateOtpDefinitionResponse> createOtpDefinition(@RequestBody ObjectRequest<CreateOtpDefinitionRequest> request) throws OtpDefinitionAlreadyExistsException, ApplicationNotFoundException, OtpPolicyNotFoundException {
        // TODO - request validation
        CreateOtpDefinitionResponse response = otpDefinitionService.createOtpDefinition(request.getRequestObject());
        return new ObjectResponse<>(response);
    }

    @RequestMapping(method = RequestMethod.PUT)
    public ObjectResponse<UpdateOtpDefinitionResponse> updateOtpDefinition(@RequestBody ObjectRequest<UpdateOtpDefinitionRequest> request) throws OtpDefinitionNotFoundException, ApplicationNotFoundException, OtpPolicyNotFoundException {
        // TODO - request validation
        UpdateOtpDefinitionResponse response = otpDefinitionService.updateOtpDefinition(request.getRequestObject());
        return new ObjectResponse<>(response);
    }

    @RequestMapping(value = "update", method = RequestMethod.POST)
    public ObjectResponse<UpdateOtpDefinitionResponse> updateOtpDefinitionPost(@RequestBody ObjectRequest<UpdateOtpDefinitionRequest> request) throws OtpDefinitionNotFoundException, ApplicationNotFoundException, OtpPolicyNotFoundException {
        // TODO - request validation
        UpdateOtpDefinitionResponse response = otpDefinitionService.updateOtpDefinition(request.getRequestObject());
        return new ObjectResponse<>(response);
    }

    @RequestMapping(value = "list", method = RequestMethod.POST)
    public ObjectResponse<GetOtpDefinitionListResponse> listOtpPolicies(@RequestBody ObjectRequest<GetOtpDefinitionListRequest> request) {
        GetOtpDefinitionListResponse response = otpDefinitionService.getOtpDefinitionList(request.getRequestObject());
        return new ObjectResponse<>(response);
    }

    @RequestMapping(value = "delete", method = RequestMethod.POST)
    public ObjectResponse<DeleteOtpDefinitionResponse> deleteOtpDefinition(@RequestBody ObjectRequest<DeleteOtpDefinitionRequest> request) throws OtpDefinitionNotFoundException {
        // TODO - request validation
        DeleteOtpDefinitionResponse response = otpDefinitionService.deleteOtpDefinition(request.getRequestObject());
        return new ObjectResponse<>(response);
    }


}
