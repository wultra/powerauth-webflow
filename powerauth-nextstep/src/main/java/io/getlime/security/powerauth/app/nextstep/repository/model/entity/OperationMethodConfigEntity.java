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

import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthMethod;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;
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

    @Serial
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

        @Serial
        private static final long serialVersionUID = -7631641120957350161L;

        @Column(name = "operation_name", nullable = false)
        private String operationName;

        @Column(name = "auth_method", nullable = false)
        @Enumerated(EnumType.STRING)
        private AuthMethod authMethod;

    }
}