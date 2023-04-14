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

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * Entity which stores configuration of operations.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Entity
@Table(name = "ns_operation_config")
@Data
@EqualsAndHashCode(of = "operationName")
public class OperationConfigEntity implements Serializable {

    private static final long serialVersionUID = 4855111531493246740L;

    @Id
    @Column(name = "operation_name", nullable = false)
    private String operationName;

    @Column(name = "template_version", nullable = false)
    private String templateVersion;

    @Column(name = "template_id", nullable = false)
    private Integer templateId;

    @Column(name = "mobile_token_enabled")
    private boolean mobileTokenEnabled;

    @Column(name = "mobile_token_mode")
    private String mobileTokenMode;

    @Column(name = "afs_enabled")
    private boolean afsEnabled;

    @Column(name = "afs_config_id")
    private String afsConfigId;

    @Column(name = "expiration_time")
    private Integer expirationTime;

}
