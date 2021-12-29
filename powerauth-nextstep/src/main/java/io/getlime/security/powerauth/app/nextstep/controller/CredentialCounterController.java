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
import io.getlime.security.powerauth.app.nextstep.service.CredentialCounterService;
import io.getlime.security.powerauth.lib.nextstep.model.exception.*;
import io.getlime.security.powerauth.lib.nextstep.model.request.ResetCountersRequest;
import io.getlime.security.powerauth.lib.nextstep.model.request.UpdateCounterRequest;
import io.getlime.security.powerauth.lib.nextstep.model.response.ResetCountersResponse;
import io.getlime.security.powerauth.lib.nextstep.model.response.UpdateCounterResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * REST controller for counter management.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@RestController
@RequestMapping("credential/counter")
@Validated
public class CredentialCounterController {

    private static final Logger logger = LoggerFactory.getLogger(CredentialCounterController.class);

    private final CredentialCounterService credentialCounterService;

    /**
     * REST controller constructor.
     * @param credentialCounterService Credential counter service.
     */
    @Autowired
    public CredentialCounterController(CredentialCounterService credentialCounterService) {
        this.credentialCounterService = credentialCounterService;
    }

    /**
     * Update a credential counter via PUT method.
     * @param request Update credential counter request.
     * @return Update credential counter response.
     * @throws UserNotFoundException Thrown when user identity is not found.
     * @throws CredentialDefinitionNotFoundException Thrown when credential definition is not found.
     * @throws InvalidRequestException Thrown when request is invalid.
     * @throws CredentialNotFoundException Thrown when credential is not found.
     * @throws CredentialNotActiveException Thrown when credential is not active.
     */
    @Operation(summary = "Update a credential counter")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Credential counter was updated"),
            @ApiResponse(responseCode = "400", description = "Invalid request, error codes: REQUEST_VALIDATION_FAILED, USER_IDENTITY_NOT_FOUND, CREDENTIAL_DEFINITION_NOT_FOUND, INVALID_REQUEST, CREDENTIAL_NOT_FOUND, CREDENTIAL_NOT_ACTIVE"),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
    })
    @RequestMapping(method = RequestMethod.PUT)
    public ObjectResponse<UpdateCounterResponse> updateCredentialCounter(@Valid @RequestBody ObjectRequest<UpdateCounterRequest> request) throws UserNotFoundException, CredentialDefinitionNotFoundException, InvalidRequestException, CredentialNotFoundException, CredentialNotActiveException {
        logger.info("Received updateCredentialCounter request, user ID: {}, credential name: {}", request.getRequestObject().getUserId(), request.getRequestObject().getCredentialName());
        final UpdateCounterResponse response = credentialCounterService.updateCredentialCounter(request.getRequestObject());
        logger.info("The updateCredentialCounter request succeeded, user ID: {}, credential name: {}", request.getRequestObject().getUserId(), request.getRequestObject().getCredentialName());
        return new ObjectResponse<>(response);
    }

    /**
     * Update a credential counter via POST method.
     * @param request Update credential counter request.
     * @return Update credential counter response.
     * @throws UserNotFoundException Thrown when user identity is not found.
     * @throws CredentialDefinitionNotFoundException Thrown when credential definition is not found.
     * @throws InvalidRequestException Thrown when request is invalid.
     * @throws CredentialNotFoundException Thrown when credential is not found.
     * @throws CredentialNotActiveException Thrown when credential is not active.
     */
    @Operation(summary = "Update a credential counter")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Credential counter was updated"),
            @ApiResponse(responseCode = "400", description = "Invalid request, error codes: REQUEST_VALIDATION_FAILED, USER_IDENTITY_NOT_FOUND, CREDENTIAL_DEFINITION_NOT_FOUND, INVALID_REQUEST, CREDENTIAL_NOT_FOUND, CREDENTIAL_NOT_ACTIVE"),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
    })
    @RequestMapping(value = "update", method = RequestMethod.POST)
    public ObjectResponse<UpdateCounterResponse> updateCredentialCounterPost(@Valid @RequestBody ObjectRequest<UpdateCounterRequest> request) throws UserNotFoundException, CredentialDefinitionNotFoundException, InvalidRequestException, CredentialNotFoundException, CredentialNotActiveException {
        logger.info("Received updateCredentialCounterPost request, user ID: {}, credential name: {}", request.getRequestObject().getUserId(), request.getRequestObject().getCredentialName());
        final UpdateCounterResponse response = credentialCounterService.updateCredentialCounter(request.getRequestObject());
        logger.info("The updateCredentialCounterPost request succeeded, user ID: {}, credential name: {}", request.getRequestObject().getUserId(), request.getRequestObject().getCredentialName());
        return new ObjectResponse<>(response);
    }

    /**
     * Reset all soft failed attempt counters.
     * @param request Rest counters request.
     * @return Reset counters response.
     * @throws InvalidRequestException Thrown when request is invalid.
     */
    @Operation(summary = "Reset all soft failed attempt counters")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Credential counters were reset"),
            @ApiResponse(responseCode = "400", description = "Invalid request, error codes: REQUEST_VALIDATION_FAILED, INVALID_REQUEST"),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
    })
    @RequestMapping(value = "reset-all", method = RequestMethod.POST)
    public ObjectResponse<ResetCountersResponse> resetAllCounters(@Valid @RequestBody ObjectRequest<ResetCountersRequest> request) throws InvalidRequestException {
        logger.info("Received resetAllCounters request");
        final ResetCountersResponse response = credentialCounterService.resetCounters(request.getRequestObject());
        logger.info("The resetAllCounters request succeeded");
        return new ObjectResponse<>(response);
    }

}
