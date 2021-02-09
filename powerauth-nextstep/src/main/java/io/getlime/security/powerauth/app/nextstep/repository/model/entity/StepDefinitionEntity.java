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

    @Column(name = "request_auth_step_result", nullable = false)
    @Enumerated(EnumType.STRING)
    private AuthStepResult requestAuthStepResult;

    @Column(name = "request_auth_method", nullable = false)
    @Enumerated(EnumType.STRING)
    private AuthMethod requestAuthMethod;

    @Column(name = "response_priority")
    private long responsePriority;

    @Column(name = "response_auth_method", nullable = false)
    @Enumerated(EnumType.STRING)
    private AuthMethod responseAuthMethod;

    @Column(name = "response_result", nullable = false)
    @Enumerated(EnumType.STRING)
    private AuthResult responseResult;

}
