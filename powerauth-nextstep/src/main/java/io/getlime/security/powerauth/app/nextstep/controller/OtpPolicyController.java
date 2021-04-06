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
import io.getlime.security.powerauth.app.nextstep.service.OtpPolicyService;
import io.getlime.security.powerauth.lib.nextstep.model.exception.InvalidConfigurationException;
import io.getlime.security.powerauth.lib.nextstep.model.exception.InvalidRequestException;
import io.getlime.security.powerauth.lib.nextstep.model.exception.OtpPolicyAlreadyExistsException;
import io.getlime.security.powerauth.lib.nextstep.model.exception.OtpPolicyNotFoundException;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * REST controller for OTP policy definitions.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@RestController
@RequestMapping("otp/policy")
public class OtpPolicyController {

    private static final Logger logger = LoggerFactory.getLogger(OtpPolicyController.class);

    private final OtpPolicyService otpPolicyService;

    /**
     * REST controller constructor.
     * @param otpPolicyService OTP policy service.
     */
    @Autowired
    public OtpPolicyController(OtpPolicyService otpPolicyService) {
        this.otpPolicyService = otpPolicyService;
    }

    /**
     * Create an OTP policy.
     * @param request Create OTP policy request.
     * @return Create OTP policy response.
     * @throws OtpPolicyAlreadyExistsException Thrown when OTP policy already exists.
     * @throws InvalidRequestException Thrown when request is invalid.
     */
    @RequestMapping(method = RequestMethod.POST)
    public ObjectResponse<CreateOtpPolicyResponse> createOtpPolicy(@Valid @RequestBody ObjectRequest<CreateOtpPolicyRequest> request) throws OtpPolicyAlreadyExistsException, InvalidRequestException {
        logger.info("Received createOtpPolicy request, OTP policy name: {}", request.getRequestObject().getOtpPolicyName());
        CreateOtpPolicyResponse response = otpPolicyService.createOtpPolicy(request.getRequestObject());
        logger.info("The createOtpPolicy request succeeded, OTP policy name: {}", request.getRequestObject().getOtpPolicyName());
        return new ObjectResponse<>(response);
    }

    /**
     * Update an OTP policy via PUT method.
     * @param request Update OTP policy request.
     * @return Update OTP policy response.
     * @throws OtpPolicyNotFoundException Thrown when OTP policy is not found.
     * @throws InvalidRequestException Thrown when request is invalid.
     */
    @RequestMapping(method = RequestMethod.PUT)
    public ObjectResponse<UpdateOtpPolicyResponse> updateOtpPolicy(@Valid @RequestBody ObjectRequest<UpdateOtpPolicyRequest> request) throws OtpPolicyNotFoundException, InvalidRequestException {
        logger.info("Received updateOtpPolicy request, OTP policy name: {}", request.getRequestObject().getOtpPolicyName());
        UpdateOtpPolicyResponse response = otpPolicyService.updateOtpPolicy(request.getRequestObject());
        logger.info("The updateOtpPolicy request succeeded, OTP policy name: {}", request.getRequestObject().getOtpPolicyName());
        return new ObjectResponse<>(response);
    }

    /**
     * Update an OTP policy via POST method.
     * @param request Update OTP policy request.
     * @return Update OTP policy response.
     * @throws OtpPolicyNotFoundException Thrown when OTP policy is not found.
     * @throws InvalidRequestException Thrown when request is invalid.
     */
    @RequestMapping(value = "update", method = RequestMethod.POST)
    public ObjectResponse<UpdateOtpPolicyResponse> updateOtpPolicyPost(@Valid @RequestBody ObjectRequest<UpdateOtpPolicyRequest> request) throws OtpPolicyNotFoundException, InvalidRequestException {
        logger.info("Received updateOtpPolicyPost request, OTP policy name: {}", request.getRequestObject().getOtpPolicyName());
        UpdateOtpPolicyResponse response = otpPolicyService.updateOtpPolicy(request.getRequestObject());
        logger.info("The updateOtpPolicyPost request succeeded, OTP policy name: {}", request.getRequestObject().getOtpPolicyName());
        return new ObjectResponse<>(response);
    }

    /**
     * Get OTP policy list.
     * @param request Get OTP policy list request.
     * @return Get OTP policy list response.
     * @throws InvalidConfigurationException Thrown when Next Step configuration is invalid.
     */
    @RequestMapping(value = "list", method = RequestMethod.POST)
    public ObjectResponse<GetOtpPolicyListResponse> getOtpPolicyList(@Valid @RequestBody ObjectRequest<GetOtpPolicyListRequest> request) throws InvalidConfigurationException {
        logger.info("Received getOtpPolicyList request");
        GetOtpPolicyListResponse response = otpPolicyService.getOtpPolicyList(request.getRequestObject());
        logger.info("The getOtpPolicyList request succeeded");
        return new ObjectResponse<>(response);
    }

    /**
     * Delete an OTP policy.
     * @param request Delete OTP policy request.
     * @return Delete OTP policy response.
     * @throws OtpPolicyNotFoundException Thrown when OTP policy is not found.
     */
    @RequestMapping(value = "delete", method = RequestMethod.POST)
    public ObjectResponse<DeleteOtpPolicyResponse> deleteOtpPolicy(@Valid @RequestBody ObjectRequest<DeleteOtpPolicyRequest> request) throws OtpPolicyNotFoundException {
        logger.info("Received deleteOtpPolicy request, OTP policy name: {}", request.getRequestObject().getOtpPolicyName());
        DeleteOtpPolicyResponse response = otpPolicyService.deleteOtpPolicy(request.getRequestObject());
        logger.info("The deleteOtpPolicy request succeeded, OTP policy name: {}", request.getRequestObject().getOtpPolicyName());
        return new ObjectResponse<>(response);
    }

}
