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
import io.getlime.security.powerauth.app.nextstep.service.AuditLogService;
import io.getlime.security.powerauth.lib.nextstep.model.request.CreateAuditRequest;
import io.getlime.security.powerauth.lib.nextstep.model.request.GetAuditListRequest;
import io.getlime.security.powerauth.lib.nextstep.model.response.CreateAuditResponse;
import io.getlime.security.powerauth.lib.nextstep.model.response.GetAuditListResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * REST controller for auditing.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@RestController
@RequestMapping("audit")
@Validated
public class AuditController {

    private static final Logger logger = LoggerFactory.getLogger(AuditController.class);

    private final AuditLogService auditLogService;

    /**
     * REST controller constructor.
     * @param auditLogService Audit log service.
     */
    @Autowired
    public AuditController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    /**
     * Create an audit log.
     * @param request Create audit request.
     * @return Create audit response.
     */
    @Operation(summary = "Create an audit")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Audit was created"),
            @ApiResponse(responseCode = "400", description = "Invalid request, error codes: REQUEST_VALIDATION_FAILED"),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
    })
    @RequestMapping(method = RequestMethod.POST)
    public ObjectResponse<CreateAuditResponse> createAudit(@Valid @RequestBody ObjectRequest<CreateAuditRequest> request) {
        logger.info("Received createAudit request, action: {}", request.getRequestObject().getAction());
        final CreateAuditResponse response = auditLogService.createAuditLog(request.getRequestObject());
        logger.info("The createAudit request succeeded, action: {}", request.getRequestObject().getAction());
        return new ObjectResponse<>(response);
    }

    /**
     * Get audit log list.
     * @param startDate Start date filter for created timestamp.
     * @param endDate End date filter for created timestamp.
     * @return Get audit log list response.
     */
    @Operation(summary = "Get audit list")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Audit list sent in response"),
            @ApiResponse(responseCode = "400", description = "Invalid request, error codes: REQUEST_VALIDATION_FAILED"),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
    })
    @RequestMapping(method = RequestMethod.GET)
    public ObjectResponse<GetAuditListResponse> getAuditList(@RequestParam @NotNull Date startDate, @RequestParam @NotNull Date endDate) {
        GetAuditListRequest request = new GetAuditListRequest();
        request.setStartDate(startDate);
        request.setEndDate(endDate);
        logger.info("Received getAuditList request");
        final GetAuditListResponse response = auditLogService.getAuditLogList(request);
        logger.info("The getAuditList request succeeded");
        return new ObjectResponse<>(response);
    }

    /**
     * Get audit log list using POST method.
     * @param request Get audit log list request.
     * @return Get audit log list response.
     */
    @Operation(summary = "Get audit list")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Audit list sent in response"),
            @ApiResponse(responseCode = "400", description = "Invalid request, error codes: REQUEST_VALIDATION_FAILED"),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
    })
    @RequestMapping(value = "list", method = RequestMethod.POST)
    public ObjectResponse<GetAuditListResponse> getAuditListPost(@Valid @RequestBody ObjectRequest<GetAuditListRequest> request) {
        logger.info("Received getAuditListPost request");
        final GetAuditListResponse response = auditLogService.getAuditLogList(request.getRequestObject());
        logger.info("The getAuditListPost request succeeded");
        return new ObjectResponse<>(response);
    }

}
