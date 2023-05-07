/*
 * PowerAuth Web Flow and related software components
 * Copyright (C) 2017 Wultra s.r.o.
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
package io.getlime.security.powerauth.app.nextstep.repository.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

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
    @SequenceGenerator(name = "ns_operation_afs", sequenceName = "ns_operation_afs_seq", allocationSize = 1)
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

    @ToString.Exclude
    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "operation_id", referencedColumnName = "operation_id", updatable = false, nullable = false)
    private OperationEntity operation;

}
