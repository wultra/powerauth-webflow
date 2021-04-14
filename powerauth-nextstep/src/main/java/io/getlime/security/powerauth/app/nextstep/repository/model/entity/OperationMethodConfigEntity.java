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

import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthMethod;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Entity which stores configuration of authentication methods by operation name.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Entity
@Table(name = "ns_operation_method_config")
@Data
@EqualsAndHashCode(of = "primaryKey")
public class OperationMethodConfigEntity implements Serializable {

    private static final long serialVersionUID = 6602831455566058868L;

    @EmbeddedId
    private OperationAuthMethodKey primaryKey;

    @Column(name = "max_auth_fails")
    private Integer maxAuthFails;

    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OperationAuthMethodKey implements Serializable {

        private static final long serialVersionUID = -7631641120957350161L;

        @Column(name = "operation_name", nullable = false)
        private String operationName;

        @Column(name = "auth_method", nullable = false)
        @Enumerated(EnumType.STRING)
        private AuthMethod authMethod;

    }
}