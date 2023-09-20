/*
 * PowerAuth Web Flow and related software components
 * Copyright (C) 2021 Wultra s.r.o.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.getlime.security.powerauth.app.nextstep.controller;

import io.getlime.core.rest.model.base.request.ObjectRequest;
import io.getlime.core.rest.model.base.response.ObjectResponse;
import io.getlime.security.powerauth.app.nextstep.service.OtpService;
import io.getlime.security.powerauth.lib.nextstep.model.exception.*;
import io.getlime.security.powerauth.lib.nextstep.model.request.*;
import io.getlime.security.powerauth.lib.nextstep.model.response.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Nullable;

/**
 * REST controller for OTP management.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@RestController
@RequestMapping("otp")
@Validated
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
    @Operation(summary = "Create an OTP")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OTP was created"),
            @ApiResponse(responseCode = "400", description = "Invalid request, error codes: REQUEST_VALIDATION_FAILED, OTP_DEFINITION_NOT_FOUND, CREDENTIAL_DEFINITION_NOT_FOUND, OPERATION_NOT_FOUND, INVALID_REQUEST, OTP_GEN_ALGORITHM_NOT_SUPPORTED, INVALID_CONFIGURATION, OPERATION_ALREADY_FINISHED, OPERATION_ALREADY_FAILED, USER_IDENTITY_NOT_ACTIVE, CREDENTIAL_NOT_ACTIVE, CREDENTIAL_NOT_FOUND, ENCRYPTION_FAILED"),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
    })
    @PostMapping
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
    @Operation(summary = "Create an send an OTP")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OTP was created and sent"),
            @ApiResponse(responseCode = "400", description = "Invalid request, error codes: REQUEST_VALIDATION_FAILED, USER_IDENTITY_NOT_ACTIVE, CREDENTIAL_NOT_ACTIVE, INVALID_REQUEST, CREDENTIAL_DEFINITION_NOT_FOUND, OPERATION_ALREADY_FINISHED, OPERATION_ALREADY_FAILED, OTP_GEN_ALGORITHM_NOT_SUPPORTED, INVALID_CONFIGURATION, CREDENTIAL_NOT_FOUND, OTP_DEFINITION_NOT_FOUND, OPERATION_NOT_FOUND, ENCRYPTION_FAILED"),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
    })
    @PostMapping("send")
    public ObjectResponse<CreateAndSendOtpResponse> createAndSendOtp(@Valid @RequestBody ObjectRequest<CreateAndSendOtpRequest> request) throws UserNotActiveException, CredentialNotActiveException, InvalidRequestException, CredentialDefinitionNotFoundException, OperationAlreadyFinishedException, OperationAlreadyFailedException, OtpGenAlgorithmNotSupportedException, InvalidConfigurationException, CredentialNotFoundException, OtpDefinitionNotFoundException, OperationNotFoundException, EncryptionException {
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
     */
    @Operation(summary = "Get an OTP list")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OTP list sent in response"),
            @ApiResponse(responseCode = "400", description = "Invalid request, error codes: REQUEST_VALIDATION_FAILED, OPERATION_NOT_FOUND, ENCRYPTION_FAILED"),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
    })
    @GetMapping
    public ObjectResponse<GetOtpListResponse> getOptList(@RequestParam @NotBlank @Size(min = 1, max = 256) String operationId, @RequestParam boolean includeRemoved) throws OperationNotFoundException {
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
     */
    @Operation(summary = "Get an OTP list")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OTP list sent in response"),
            @ApiResponse(responseCode = "400", description = "Invalid request, error codes: REQUEST_VALIDATION_FAILED, OPERATION_NOT_FOUND, ENCRYPTION_FAILED"),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
    })
    @PostMapping("list")
    public ObjectResponse<GetOtpListResponse> getOptListPost(@Valid @RequestBody ObjectRequest<GetOtpListRequest> request) throws OperationNotFoundException {
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
    @Operation(summary = "Get an OTP detail")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OTP detail sent in response"),
            @ApiResponse(responseCode = "400", description = "Invalid request, error codes: REQUEST_VALIDATION_FAILED, OPERATION_NOT_FOUND, INVALID_REQUEST, OTP_NOT_FOUND, INVALID_CONFIGURATION, ENCRYPTION_FAILED"),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
    })
    @GetMapping("detail")
    public ObjectResponse<GetOtpDetailResponse> getOtpDetail(@RequestParam @Nullable @Size(min = 36, max = 36) String otpId, @RequestParam @Nullable @Size(min = 1, max = 256) String operationId) throws OperationNotFoundException, InvalidRequestException, OtpNotFoundException, InvalidConfigurationException, EncryptionException {
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
    @Operation(summary = "Get an OTP detail")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OTP detail sent in response"),
            @ApiResponse(responseCode = "400", description = "Invalid request, error codes: REQUEST_VALIDATION_FAILED, OPERATION_NOT_FOUND, INVALID_REQUEST, OTP_NOT_FOUND, INVALID_CONFIGURATION, ENCRYPTION_FAILED"),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
    })
    @PostMapping("detail")
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
    @Operation(summary = "Delete an OTP")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OTP was deleted"),
            @ApiResponse(responseCode = "400", description = "Invalid request, error codes: REQUEST_VALIDATION_FAILED, OTP_NOT_FOUND, INVALID_REQUEST, OPERATION_NOT_FOUND"),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
    })
    @PostMapping("delete")
    public @ResponseBody ObjectResponse<DeleteOtpResponse> deleteOtp(@Valid @RequestBody ObjectRequest<DeleteOtpRequest> request) throws OtpNotFoundException, InvalidRequestException, OperationNotFoundException {
        logger.info("Received deleteOtp request, OTP ID: {}, operation ID: {}", request.getRequestObject().getOtpId(), request.getRequestObject().getOperationId());
        final DeleteOtpResponse response = otpService.deleteOtp(request.getRequestObject());
        logger.info("The deleteOtp request succeeded, OTP ID: {}, operation ID: {}", request.getRequestObject().getOtpId(), request.getRequestObject().getOperationId());
        return new ObjectResponse<>(response);
    }

}
