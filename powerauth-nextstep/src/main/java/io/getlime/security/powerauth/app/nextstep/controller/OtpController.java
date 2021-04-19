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
import io.getlime.security.powerauth.lib.nextstep.model.request.*;
import io.getlime.security.powerauth.lib.nextstep.model.response.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

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

    /**
     * REST controller constructor.
     * @param otpService OTP service.
     */
    @Autowired
    public OtpController(OtpService otpService) {
        this.otpService = otpService;
    }

    /**
     * Create an OTP.
     * @param request Create OTP request.
     * @return Create OTP response.
     * @throws OtpDefinitionNotFoundException Thrown when OTP definition is not found.
     * @throws CredentialDefinitionNotFoundException Thrown when credential definition is not found.
     * @throws OperationNotFoundException Thrown when operation is not found.
     * @throws InvalidRequestException Thrown when request is invalid.
     * @throws OtpGenAlgorithmNotSupportedException Thrown when OTP generation algorithm is not supported.
     * @throws InvalidConfigurationException Thrown when Next Step configuration is invalid.
     * @throws OperationAlreadyFinishedException Thrown when operation is already finished.
     * @throws OperationAlreadyFailedException Thrown when operation is already failed.
     * @throws UserNotActiveException Thrown when user is not active.
     * @throws CredentialNotActiveException Thrown when credential is not active.
     * @throws CredentialNotFoundException Thrown when credential is not found.
     * @throws EncryptionException Thrown when encryption fails.
     */
    @RequestMapping(method = RequestMethod.POST)
    public ObjectResponse<CreateOtpResponse> createOtp(@Valid @RequestBody ObjectRequest<CreateOtpRequest> request) throws OtpDefinitionNotFoundException, CredentialDefinitionNotFoundException, OperationNotFoundException, InvalidRequestException, OtpGenAlgorithmNotSupportedException, InvalidConfigurationException, OperationAlreadyFinishedException, OperationAlreadyFailedException, UserNotActiveException, CredentialNotActiveException, CredentialNotFoundException, EncryptionException {
        logger.info("Received createOtp request, operation ID: {}, user ID: {}, OTP name: {}", request.getRequestObject().getOperationId(), request.getRequestObject().getUserId(), request.getRequestObject().getOtpName());
        final CreateOtpResponse response = otpService.createOtp(request.getRequestObject());
        logger.info("The createOtp request succeeded, operation ID: {}, user ID: {}, OTP name: {}, OTP ID: {}", request.getRequestObject().getOperationId(), request.getRequestObject().getUserId(), request.getRequestObject().getOtpName(), response.getOtpId());
        return new ObjectResponse<>(response);
    }

    /**
     * Create and send an OTP.
     * @param request Create and send OTP request.
     * @return Create and send OTP response.
     * @throws OtpDefinitionNotFoundException Thrown when OTP definition is not found.
     * @throws CredentialDefinitionNotFoundException Thrown when credential definition is not found.
     * @throws OperationNotFoundException Thrown when operation is not found.
     * @throws InvalidRequestException Thrown when request is invalid.
     * @throws OtpGenAlgorithmNotSupportedException Thrown when OTP generation algorithm is not supported.
     * @throws InvalidConfigurationException Thrown when Next Step configuration is invalid.
     * @throws OperationAlreadyFinishedException Thrown when operation is already finished.
     * @throws OperationAlreadyFailedException Thrown when operation is already failed.
     * @throws UserNotActiveException Thrown when user is not active.
     * @throws CredentialNotActiveException Thrown when credential is not active.
     * @throws CredentialNotFoundException Thrown when credential is not found.
     * @throws EncryptionException Thrown when encryption fails.
     */
    @RequestMapping(value = "send", method = RequestMethod.POST)
    public ObjectResponse<CreateAndSendOtpResponse> createAndSendOtp(@Valid @RequestBody ObjectRequest<CreateAndSendOtpRequest> request) throws UserNotActiveException, CredentialNotActiveException, InvalidRequestException, CredentialDefinitionNotFoundException, OperationAlreadyFailedException, OtpGenAlgorithmNotSupportedException, InvalidConfigurationException, OperationAlreadyFinishedException, CredentialNotFoundException, OtpDefinitionNotFoundException, OperationNotFoundException, EncryptionException {
        logger.info("Received createAndSendOtp request, operation ID: {}, user ID: {}, OTP name: {}", request.getRequestObject().getOperationId(), request.getRequestObject().getUserId(), request.getRequestObject().getOtpName());
        final CreateAndSendOtpResponse response = otpService.createAndSendOtp(request.getRequestObject());
        logger.info("The createAndSendOtp request succeeded, operation ID: {}, user ID: {}, OTP name: {}, OTP ID: {}", request.getRequestObject().getOperationId(), request.getRequestObject().getUserId(), request.getRequestObject().getOtpName(), response.getOtpId());
        return new ObjectResponse<>(response);
    }

    /**
     * Get OTP list for an operation.
     * @param operationId Operation ID.
     * @param includeRemoved Whether removed OTPs should be included.
     * @return Get OTP list response.
     * @throws OperationNotFoundException Thrown when operation is not found.
     * @throws EncryptionException Thrown when decryption fails.
     */
    @RequestMapping(method = RequestMethod.GET)
    public ObjectResponse<GetOtpListResponse> getOptList(@RequestParam @NotBlank @Size(min = 1, max = 256) String operationId, @RequestParam boolean includeRemoved) throws OperationNotFoundException, InvalidConfigurationException, EncryptionException {
        logger.info("Received getOptList request, operation ID: {}", operationId);
        GetOtpListRequest request = new GetOtpListRequest();
        request.setOperationId(operationId);
        request.setIncludeRemoved(includeRemoved);
        final GetOtpListResponse response = otpService.getOtpList(request);
        logger.info("The getOptList request succeeded, operation ID: {}", operationId);
        return new ObjectResponse<>(response);
    }

    /**
     * Get OTP list for an operation using POST method.
     * @param request Get OTP list request.
     * @return Get OTP list response.
     * @throws OperationNotFoundException Thrown when operation is not found.
     * @throws EncryptionException Thrown when decryption fails.
     */
    @RequestMapping(value = "list", method = RequestMethod.POST)
    public ObjectResponse<GetOtpListResponse> getOptListPost(@Valid @RequestBody ObjectRequest<GetOtpListRequest> request) throws OperationNotFoundException, InvalidConfigurationException, EncryptionException {
        logger.info("Received getOptListPost request, operation ID: {}", request.getRequestObject().getOperationId());
        final GetOtpListResponse response = otpService.getOtpList(request.getRequestObject());
        logger.info("The getOptListPost request succeeded, operation ID: {}", request.getRequestObject().getOperationId());
        return new ObjectResponse<>(response);
    }

    /**
     * Get OTP detail for an OTP or operation.
     * @param otpId OTP ID, use null if operation ID should be used instead.
     * @param operationId Operation ID, use null if OTP ID should be used instead.
     * @return Get OTP detail response.
     * @throws OperationNotFoundException Thrown when operation is not found.
     * @throws InvalidRequestException Thrown when request is invalid.
     * @throws OtpNotFoundException Thrown when OTP is not found.
     * @throws EncryptionException Thrown when decryption fails.
     */
    @RequestMapping(value = "detail", method = RequestMethod.GET)
    public ObjectResponse<GetOtpDetailResponse> getOtpDetail(@RequestParam @Size(min = 36, max = 36) String otpId, @RequestParam @Size(min = 1, max = 256) String operationId) throws OperationNotFoundException, InvalidRequestException, OtpNotFoundException, InvalidConfigurationException, EncryptionException {
        logger.info("Received getOtpDetail request, OTP ID: {}, operation ID: {}", otpId, operationId);
        GetOtpDetailRequest request = new GetOtpDetailRequest();
        request.setOtpId(otpId);
        request.setOperationId(operationId);
        final GetOtpDetailResponse response = otpService.getOtpDetail(request);
        logger.info("The getOtpDetail request succeeded, OTP ID: {}, operation ID: {}", otpId, operationId);
        return new ObjectResponse<>(response);
    }

    /**
     * Get OTP detail for an OTP or operation.
     * @param request Get OTP detail request.
     * @return Get OTP detail response.
     * @throws OperationNotFoundException Thrown when operation is not found.
     * @throws InvalidRequestException Thrown when request is invalid.
     * @throws OtpNotFoundException Thrown when OTP is not found.
     * @throws EncryptionException Thrown when decryption fails.
     */
    @RequestMapping(value = "detail", method = RequestMethod.POST)
    public ObjectResponse<GetOtpDetailResponse> getOtpDetailPost(@Valid @RequestBody ObjectRequest<GetOtpDetailRequest> request) throws OperationNotFoundException, InvalidRequestException, OtpNotFoundException, InvalidConfigurationException, EncryptionException {
        logger.info("Received getOtpDetailPost request, OTP ID: {}, operation ID: {}", request.getRequestObject().getOtpId(), request.getRequestObject().getOperationId());
        final GetOtpDetailResponse response = otpService.getOtpDetail(request.getRequestObject());
        logger.info("The getOtpDetailPost request succeeded, OTP ID: {}, operation ID: {}", request.getRequestObject().getOtpId(), request.getRequestObject().getOperationId());
        return new ObjectResponse<>(response);
    }

    /**
     * Delete an OTP.
     * @param request Delete OTP request.
     * @return Delete OTP response.
     * @throws OtpNotFoundException Thrown when OTP is not found.
     * @throws InvalidRequestException Thrown when request is invalid.
     * @throws OperationNotFoundException Thrown when operation is not found.
     */
    @RequestMapping(value = "delete", method = RequestMethod.POST)
    public @ResponseBody ObjectResponse<DeleteOtpResponse> deleteOtp(@Valid @RequestBody ObjectRequest<DeleteOtpRequest> request) throws OtpNotFoundException, InvalidRequestException, OperationNotFoundException {
        logger.info("Received deleteOtp request, OTP ID: {}, operation ID: {}", request.getRequestObject().getOtpId(), request.getRequestObject().getOperationId());
        final DeleteOtpResponse response = otpService.deleteOtp(request.getRequestObject());
        logger.info("The deleteOtp request succeeded, OTP ID: {}, operation ID: {}", request.getRequestObject().getOtpId(), request.getRequestObject().getOperationId());
        return new ObjectResponse<>(response);
    }

}
