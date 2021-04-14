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
package io.getlime.security.powerauth.app.nextstep.repository.model.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * Entity which stores audit logs.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Entity
@Table(name = "ns_audit_log")
@Data
@EqualsAndHashCode(of = {"action", "data", "timestampCreated"})
public class AuditLogEntity implements Serializable {

    private static final long serialVersionUID = 2306451782086796781L;

    @Id
    @SequenceGenerator(name = "ns_audit_log", sequenceName = "ns_audit_log_seq")
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "ns_audit_log")
    @Column(name = "audit_log_id", nullable = false)
    private Long auditLogId;

    @Column(name = "action", nullable = false)
    private String action;

    @Column(name = "data")
    private String data;

    @Column(name = "timestamp_created", nullable = false)
    private Date timestampCreated;

}
