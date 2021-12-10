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
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthResult;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthStepResult;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.OperationRequestType;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Entity used to define steps for dynamic step resolution.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Entity
@Table(name = "ns_step_definition")
@Data
@EqualsAndHashCode(of = {"operationName", "stepDefinitionId"})
public class StepDefinitionEntity implements Serializable {

    private static final long serialVersionUID = 1125553531017608411L;

    @Id
    @Column(name = "step_definition_id")
    private long stepDefinitionId;

    @Column(name = "operation_name", nullable = false)
    private String operationName;

    @Column(name = "operation_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private OperationRequestType operationType;

    @Column(name = "request_auth_step_result")
    @Enumerated(EnumType.STRING)
    private AuthStepResult requestAuthStepResult;

    @Column(name = "request_auth_method")
    @Enumerated(EnumType.STRING)
    private AuthMethod requestAuthMethod;

    @Column(name = "response_priority")
    private long responsePriority;

    @Column(name = "response_auth_method")
    @Enumerated(EnumType.STRING)
    private AuthMethod responseAuthMethod;

    @Column(name = "response_result", nullable = false)
    @Enumerated(EnumType.STRING)
    private AuthResult responseResult;

}
