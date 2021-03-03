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
import io.getlime.security.powerauth.app.nextstep.exception.ObjectRequestValidator;
import io.getlime.security.powerauth.app.nextstep.service.AuditLogService;
import io.getlime.security.powerauth.lib.nextstep.model.request.CreateAuditRequest;
import io.getlime.security.powerauth.lib.nextstep.model.request.GetAuditListRequest;
import io.getlime.security.powerauth.lib.nextstep.model.response.CreateAuditResponse;
import io.getlime.security.powerauth.lib.nextstep.model.response.GetAuditListResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * REST controller for auditing.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@RestController
@RequestMapping("audit")
public class AuditController {

    private static final Logger logger = LoggerFactory.getLogger(AuditController.class);

    private final AuditLogService auditLogService;
    private final ObjectRequestValidator requestValidator;

    /**
     * REST controller constructor.
     * @param auditLogService Audit log service.
     * @param requestValidator Request validator.
     */
    @Autowired
    public AuditController(AuditLogService auditLogService, ObjectRequestValidator requestValidator) {
        this.auditLogService = auditLogService;
        this.requestValidator = requestValidator;
    }

    /**
     * Initialize the request validator.
     * @param binder Data binder.
     */
    @InitBinder
    private void initBinder(WebDataBinder binder) {
        binder.setValidator(requestValidator);
    }

    /**
     * Create an audit log.
     * @param request Create audit request.
     * @return Create audit response.
     */
    @RequestMapping(method = RequestMethod.POST)
    public ObjectResponse<CreateAuditResponse> createAudit(@Valid @RequestBody ObjectRequest<CreateAuditRequest> request) {
        CreateAuditResponse response = auditLogService.createAuditLog(request.getRequestObject());
        return new ObjectResponse<>(response);
    }

    /**
     * Get audit log list.
     * @param request Get audit log list request.
     * @return Get audit log list response.
     */
    @RequestMapping(value = "list", method = RequestMethod.POST)
    public ObjectResponse<GetAuditListResponse> getAuditList(@Valid @RequestBody ObjectRequest<GetAuditListRequest> request) {
        GetAuditListResponse response = auditLogService.getAuditLogList(request.getRequestObject());
        return new ObjectResponse<>(response);
    }

}
