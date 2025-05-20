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

import com.wultra.core.rest.model.base.request.ObjectRequest;
import com.wultra.core.rest.model.base.response.ObjectResponse;
import io.getlime.security.powerauth.app.nextstep.service.CredentialService;
import io.getlime.security.powerauth.lib.nextstep.model.exception.*;
import io.getlime.security.powerauth.lib.nextstep.model.request.*;
import io.getlime.security.powerauth.lib.nextstep.model.response.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for credential management.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@RestController
@RequestMapping("credential")
@Validated
public class CredentialController {

    private static final Logger logger = LoggerFactory.getLogger(CredentialController.class);

    private final CredentialService credentialService;

    /**
     * REST controller constructor.
     * @param credentialService Credential service.
     */
    @Autowired
    public CredentialController(CredentialService credentialService) {
        this.credentialService = credentialService;
    }

    /**
     * Create a credential.
     * @param request Create credential request.
     * @return Create credential response.
     * @throws UserNotFoundException Thrown when user identity is not found.
     * @throws CredentialDefinitionNotFoundException Thrown when credential definition is not found.
     * @throws InvalidConfigurationException Thrown when configuration is invalid.
     * @throws InvalidRequestException Thrown when request is invalid.
     * @throws CredentialValidationFailedException Thrown when credential validation fails.
     * @throws EncryptionException Thrown when encryption or decryption fails.
     */
    @Operation(summary = "Create a credential")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Credential was created"),
            @ApiResponse(responseCode = "400", description = "Invalid request, error codes: REQUEST_VALIDATION_FAILED, USER_IDENTITY_NOT_FOUND, CREDENTIAL_DEFINITION_NOT_FOUND, INVALID_CONFIGURATION, INVALID_REQUEST, CREDENTIAL_VALIDATION_FAILED, ENCRYPTION_FAILED"),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
    })
    @PostMapping
    public ObjectResponse<CreateCredentialResponse> createCredential(@Valid @RequestBody ObjectRequest<CreateCredentialRequest> request) throws UserNotFoundException, CredentialDefinitionNotFoundException, InvalidConfigurationException, InvalidRequestException, CredentialValidationFailedException, EncryptionException {
        logger.info("Received createCredential request, user ID: {}, credential name: {}", request.getRequestObject().getUserId(), request.getRequestObject().getCredentialName());
        final CreateCredentialResponse response = credentialService.createCredential(request.getRequestObject());
        logger.info("The createCredential request succeeded, user ID: {}, credential name: {}", request.getRequestObject().getUserId(), request.getRequestObject().getCredentialName());
        return new ObjectResponse<>(response);
    }

    /**
     * Update a credential via a PUT method.
     * @param request Update credential request.
     * @return Update credential response.
     * @throws UserNotFoundException Thrown when user identity is not found.
     * @throws CredentialDefinitionNotFoundException Thrown when credential definition is not found.
     * @throws CredentialNotFoundException Thrown when credential is not found.
     * @throws InvalidRequestException Thrown when request is invalid.
     * @throws CredentialValidationFailedException Thrown when credential validation fails.
     * @throws InvalidConfigurationException Thrown when Next Step configuration is invalid.
     * @throws EncryptionException Thrown when encryption or decryption fails.
     */
    @Operation(summary = "Update a credential")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Credential was updated"),
            @ApiResponse(responseCode = "400", description = "Invalid request, error codes: REQUEST_VALIDATION_FAILED, USER_IDENTITY_NOT_FOUND, CREDENTIAL_DEFINITION_NOT_FOUND, CREDENTIAL_NOT_FOUND, INVALID_REQUEST, CREDENTIAL_VALIDATION_FAILED, INVALID_CONFIGURATION, ENCRYPTION_FAILED"),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
    })
    @PutMapping
    public ObjectResponse<UpdateCredentialResponse> updateCredential(@Valid @RequestBody ObjectRequest<UpdateCredentialRequest> request) throws UserNotFoundException, CredentialDefinitionNotFoundException, CredentialNotFoundException, InvalidRequestException, CredentialValidationFailedException, InvalidConfigurationException, EncryptionException {
        logger.info("Received updateCredential request, user ID: {}, credential name: {}", request.getRequestObject().getUserId(), request.getRequestObject().getCredentialName());
        final UpdateCredentialResponse response = credentialService.updateCredential(request.getRequestObject());
        logger.info("The updateCredential request succeeded, user ID: {}, credential name: {}", request.getRequestObject().getUserId(), request.getRequestObject().getCredentialName());
        return new ObjectResponse<>(response);
    }

    /**
     * Update a credential via a POST method.
     * @param request Update credential request.
     * @return Update credential response.
     * @throws UserNotFoundException Thrown when user identity is not found.
     * @throws CredentialDefinitionNotFoundException Thrown when credential definition is not found.
     * @throws CredentialNotFoundException Thrown when credential is not found.
     * @throws InvalidRequestException Thrown when request is invalid.
     * @throws CredentialValidationFailedException Thrown when credential validation fails.
     * @throws InvalidConfigurationException Thrown when Next Step configuration is invalid.
     * @throws EncryptionException Thrown when encryption or decryption fails.
     */
    @Operation(summary = "Update a credential")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Credential was updated"),
            @ApiResponse(responseCode = "400", description = "Invalid request, error codes: REQUEST_VALIDATION_FAILED, USER_IDENTITY_NOT_FOUND, CREDENTIAL_DEFINITION_NOT_FOUND, CREDENTIAL_NOT_FOUND, INVALID_REQUEST, CREDENTIAL_VALIDATION_FAILED, INVALID_CONFIGURATION, ENCRYPTION_FAILED"),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
    })
    @PostMapping("update")
    public ObjectResponse<UpdateCredentialResponse> updateCredentialPost(@Valid @RequestBody ObjectRequest<UpdateCredentialRequest> request) throws UserNotFoundException, CredentialDefinitionNotFoundException, CredentialNotFoundException, InvalidRequestException, CredentialValidationFailedException, InvalidConfigurationException, EncryptionException {
        logger.info("Received updateCredentialPost request, user ID: {}, credential name: {}", request.getRequestObject().getUserId(), request.getRequestObject().getCredentialName());
        final UpdateCredentialResponse response = credentialService.updateCredential(request.getRequestObject());
        logger.info("The updateCredentialPost request succeeded, user ID: {}, credential name: {}", request.getRequestObject().getUserId(), request.getRequestObject().getCredentialName());
        return new ObjectResponse<>(response);
    }

    /**
     * Validate a credential.
     * @param request Validate credential request.
     * @return Validate credential response.
     * @throws CredentialDefinitionNotFoundException Thrown when credential definition is not found.
     * @throws InvalidRequestException Thrown when request is invalid.
     * @throws UserNotFoundException Thrown when user identity is not found.
     * @throws EncryptionException Thrown when decryption fails.
     */
    @Operation(summary = "Validate a credential")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Credential was validated"),
            @ApiResponse(responseCode = "400", description = "Invalid request, error codes: REQUEST_VALIDATION_FAILED, CREDENTIAL_DEFINITION_NOT_FOUND, INVALID_REQUEST, USER_IDENTITY_NOT_FOUND, INVALID_CONFIGURATION, ENCRYPTION_FAILED"),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
    })
    @PostMapping("validate")
    public ObjectResponse<ValidateCredentialResponse> validateCredential(@Valid @RequestBody ObjectRequest<ValidateCredentialRequest> request) throws CredentialDefinitionNotFoundException, InvalidRequestException, UserNotFoundException, InvalidConfigurationException, EncryptionException {
        logger.info("Received validateCredential request, user ID: {}, credential name: {}", request.getRequestObject().getUserId(), request.getRequestObject().getCredentialName());
        final ValidateCredentialResponse response = credentialService.validateCredential(request.getRequestObject());
        logger.info("The validateCredential request succeeded, user ID: {}, credential name: {}", request.getRequestObject().getUserId(), request.getRequestObject().getCredentialName());
        return new ObjectResponse<>(response);
    }

    /**
     * Reset a credential.
     * @param request Reset credential request.
     * @return Reset credential response.
     * @throws UserNotFoundException Thrown when user identity is not found.
     * @throws CredentialDefinitionNotFoundException Thrown when credential definition is not found.
     * @throws CredentialNotFoundException Thrown when credential is not found.
     * @throws InvalidConfigurationException Thrown when Next Step configuration is invalid.
     * @throws EncryptionException Thrown when encryption fails.
     */
    @Operation(summary = "Reset a credential")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Credential was reset"),
            @ApiResponse(responseCode = "400", description = "Invalid request, error codes: REQUEST_VALIDATION_FAILED, USER_IDENTITY_NOT_FOUND, CREDENTIAL_DEFINITION_NOT_FOUND, CREDENTIAL_NOT_FOUND, INVALID_CONFIGURATION, ENCRYPTION_FAILED"),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
    })
    @PostMapping("reset")
    public ObjectResponse<ResetCredentialResponse> resetCredential(@Valid @RequestBody ObjectRequest<ResetCredentialRequest> request) throws UserNotFoundException, CredentialDefinitionNotFoundException, CredentialNotFoundException, InvalidConfigurationException, EncryptionException {
        logger.info("Received resetCredential request, user ID: {}, credential name: {}", request.getRequestObject().getUserId(), request.getRequestObject().getCredentialName());
        final ResetCredentialResponse response = credentialService.resetCredential(request.getRequestObject());
        logger.info("The resetCredential request succeeded, user ID: {}, credential name: {}", request.getRequestObject().getUserId(), request.getRequestObject().getCredentialName());
        return new ObjectResponse<>(response);
    }

    /**
     * Delete a credential.
     * @param request Delete credential request.
     * @return Delete credential response.
     * @throws UserNotFoundException Thrown when user identity is not found.
     * @throws CredentialDefinitionNotFoundException Thrown when credential definition is not found.
     * @throws CredentialNotFoundException Thrown when credential is not found.
     */
    @Operation(summary = "Delete a credential")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Credential was deleted"),
            @ApiResponse(responseCode = "400", description = "Invalid request, error codes: REQUEST_VALIDATION_FAILED, USER_IDENTITY_NOT_FOUND, CREDENTIAL_DEFINITION_NOT_FOUND, CREDENTIAL_NOT_FOUND"),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
    })
    @PostMapping("delete")
    public ObjectResponse<DeleteCredentialResponse> deleteCredential(@Valid @RequestBody ObjectRequest<DeleteCredentialRequest> request) throws UserNotFoundException, CredentialDefinitionNotFoundException, CredentialNotFoundException {
        logger.info("Received deleteCredential request, user ID: {}, credential name: {}", request.getRequestObject().getUserId(), request.getRequestObject().getCredentialName());
        final DeleteCredentialResponse response = credentialService.deleteCredential(request.getRequestObject());
        logger.info("The deleteCredential request succeeded, user ID: {}, credential name: {}", request.getRequestObject().getUserId(), request.getRequestObject().getCredentialName());
        return new ObjectResponse<>(response);
    }

    /**
     * Block a credential.
     * @param request Block credential request.
     * @return Block credential response.
     * @throws UserNotFoundException Thrown when user identity is not found.
     * @throws CredentialDefinitionNotFoundException Thrown when credential definition is not found.
     * @throws CredentialNotFoundException Thrown when credential is not found.
     * @throws CredentialNotActiveException Thrown when credential is not active.
     */
    @Operation(summary = "Block a credential")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Credential was blocked"),
            @ApiResponse(responseCode = "400", description = "Invalid request, error codes: REQUEST_VALIDATION_FAILED, USER_IDENTITY_NOT_FOUND, CREDENTIAL_DEFINITION_NOT_FOUND, CREDENTIAL_NOT_FOUND, CREDENTIAL_NOT_ACTIVE"),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
    })
    @PostMapping("block")
    public ObjectResponse<BlockCredentialResponse> blockCredential(@Valid @RequestBody ObjectRequest<BlockCredentialRequest> request) throws UserNotFoundException, CredentialDefinitionNotFoundException, CredentialNotFoundException, CredentialNotActiveException {
        logger.info("Received blockCredential request, user ID: {}, credential name: {}", request.getRequestObject().getUserId(), request.getRequestObject().getCredentialName());
        final BlockCredentialResponse response = credentialService.blockCredential(request.getRequestObject());
        logger.info("The blockCredential request succeeded, user ID: {}, credential name: {}", request.getRequestObject().getUserId(), request.getRequestObject().getCredentialName());
        return new ObjectResponse<>(response);
    }

    /**
     * Unblock a credential.
     * @param request Unblock credential request.
     * @return Unblock credential response.
     * @throws UserNotFoundException Thrown when user identity is not found.
     * @throws CredentialDefinitionNotFoundException Thrown when credential definition is not found.
     * @throws CredentialNotFoundException Thrown when credential is not found.
     * @throws CredentialNotBlockedException Thrown when credential is not blocked.
     */
    @Operation(summary = "Unblock a credential")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Credential was unblocked"),
            @ApiResponse(responseCode = "400", description = "Invalid request, error codes: REQUEST_VALIDATION_FAILED, USER_IDENTITY_NOT_FOUND, CREDENTIAL_DEFINITION_NOT_FOUND, CREDENTIAL_NOT_FOUND, CREDENTIAL_NOT_BLOCKED"),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
    })
    @PostMapping("unblock")
    public ObjectResponse<UnblockCredentialResponse> unblockCredential(@Valid @RequestBody ObjectRequest<UnblockCredentialRequest> request) throws UserNotFoundException, CredentialDefinitionNotFoundException, CredentialNotFoundException, CredentialNotBlockedException {
        logger.info("Received unblockCredential request, user ID: {}, credential name: {}", request.getRequestObject().getUserId(), request.getRequestObject().getCredentialName());
        final UnblockCredentialResponse response = credentialService.unblockCredential(request.getRequestObject());
        logger.info("The unblockCredential request succeeded, user ID: {}, credential name: {}", request.getRequestObject().getUserId(), request.getRequestObject().getCredentialName());
        return new ObjectResponse<>(response);
    }

}
