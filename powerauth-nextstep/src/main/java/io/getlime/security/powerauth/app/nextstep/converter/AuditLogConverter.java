/*
 * Copyright 2019 Wultra s.r.o.
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
package io.getlime.security.powerauth.app.nextstep.converter;

import io.getlime.security.powerauth.app.nextstep.repository.model.entity.AuditLogEntity;
import io.getlime.security.powerauth.lib.nextstep.model.entity.AuditDetail;

/**
 * Converter for audit logs.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class AuditLogConverter {

    /**
     * Convert audit log from entity to detail.
     * @param auditLog Audit log entity.
     * @return Audit detail.
     */
    public AuditDetail fromEntity(AuditLogEntity auditLog) {
        AuditDetail auditDetail = new AuditDetail();
        auditDetail.setAction(auditLog.getAction());
        auditDetail.setData(auditLog.getData());
        auditDetail.setTimestampCreated(auditLog.getTimestampCreated());
        return auditDetail;
    }

}