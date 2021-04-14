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
package io.getlime.security.powerauth.app.nextstep.service;

import io.getlime.security.powerauth.app.nextstep.converter.AuditLogConverter;
import io.getlime.security.powerauth.app.nextstep.repository.AuditLogRepository;
import io.getlime.security.powerauth.app.nextstep.repository.model.entity.AuditLogEntity;
import io.getlime.security.powerauth.lib.nextstep.model.entity.AuditDetail;
import io.getlime.security.powerauth.lib.nextstep.model.request.CreateAuditRequest;
import io.getlime.security.powerauth.lib.nextstep.model.request.GetAuditListRequest;
import io.getlime.security.powerauth.lib.nextstep.model.response.CreateAuditResponse;
import io.getlime.security.powerauth.lib.nextstep.model.response.GetAuditListResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Date;

/**
 * This service handles persistence of audit logs.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Service
public class AuditLogService {

    private final Logger logger = LoggerFactory.getLogger(AuditLogService.class);

    private final AuditLogRepository auditLogRepository;

    private final AuditLogConverter auditLogConverter = new AuditLogConverter();

    /**
     * Audit log service constructor.
     * @param auditLogRepository Audit log repository.
     */
    @Autowired
    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    /**
     * Create an audit log.
     * @param request Create audit log request.
     * @return Create audit log response.
     */
    @Transactional
    public CreateAuditResponse createAuditLog(CreateAuditRequest request) {
        AuditLogEntity auditLog = new AuditLogEntity();
        auditLog.setAction(request.getAction());
        auditLog.setData(request.getData());
        auditLog.setTimestampCreated(new Date());
        auditLogRepository.save(auditLog);
        CreateAuditResponse response = new CreateAuditResponse();
        response.setAction(auditLog.getAction());
        return response;
    }

    /**
     * Get list of audit logs.
     * @param request Get audit logs request.
     * @return Get audit logs response.
     */
    @Transactional
    public GetAuditListResponse getAuditLogList(GetAuditListRequest request) {
        Date startDate = request.getStartDate();
        Date endDate = request.getEndDate();
        Iterable<AuditLogEntity> auditLogs = auditLogRepository.findAuditLogsByCreatedDate(startDate, endDate);
        GetAuditListResponse response = new GetAuditListResponse();
        for (AuditLogEntity auditLog : auditLogs) {
            AuditDetail auditDetail = auditLogConverter.fromEntity(auditLog);
            response.getAudits().add(auditDetail);
        }
        return response;
    }

}
