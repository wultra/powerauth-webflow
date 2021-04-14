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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
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

}
