/*
 * Copyright 2017 Wultra s.r.o.
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
 * Entity which stores AFS requests and responses.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Entity
@Table(name = "ns_operation_afs")
@Data
@EqualsAndHashCode(of = {"afsAction", "stepIndex", "timestampCreated", "operation"})
public class OperationAfsActionEntity implements Serializable {

    private static final long serialVersionUID = 744614077188309148L;

    @Id
    @SequenceGenerator(name = "ns_operation_afs", sequenceName = "ns_operation_afs_seq")
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "ns_operation_afs")
    @Column(name = "afs_action_id", nullable = false)
    private Long afsActionId;

    @Column(name = "request_afs_action", nullable = false)
    private String afsAction;

    @Column(name = "request_step_index", nullable = false)
    private int stepIndex;

    @Column(name = "request_afs_extras")
    private String requestAfsExtras;

    @Column(name = "response_afs_apply", nullable = false)
    private boolean afsResponseApplied;

    @Column(name = "response_afs_label")
    private String afsLabel;

    @Column(name = "response_afs_extras")
    private String responseAfsExtras;

    @Column(name = "timestamp_created", nullable = false)
    private Date timestampCreated;

    @ManyToOne
    @JoinColumn(name = "operation_id", referencedColumnName = "operation_id", updatable = false, nullable = false)
    private OperationEntity operation;

}
