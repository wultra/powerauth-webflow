/*
 * Copyright 2017 Lime - HighTech Solutions s.r.o.
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

import javax.persistence.*;
import java.io.Serializable;

/**
 * Entity used to define steps for dynamic step resolution.
 *
 * @author Roman Strobl
 */
@Entity
@Table(name = "ns_step_definition")
public class StepDefinitionEntity implements Serializable {

    private static final long serialVersionUID = 1125553531017608411L;

    @Id
    @Column(name = "step_definition_id")
    private Long stepDefinitionId;

    @Column(name = "operation_name")
    private String operationName;

    @Column(name = "operation_type")
    @Enumerated(EnumType.STRING)
    private OperationRequestType operationType;

    @Column(name = "request_auth_step_result")
    @Enumerated(EnumType.STRING)
    private AuthStepResult requestAuthStepResult;

    @Column(name = "request_auth_method")
    @Enumerated(EnumType.STRING)
    private AuthMethod requestAuthMethod;

    @Column(name = "response_priority")
    private Long responsePriority;

    @Column(name = "response_auth_method")
    @Enumerated(EnumType.STRING)
    private AuthMethod responseAuthMethod;

    @Column(name = "response_result")
    @Enumerated(EnumType.STRING)
    private AuthResult responseResult;

    public Long getStepDefinitionId() {
        return stepDefinitionId;
    }

    public void setStepDefinitionId(Long stepDefinitionId) {
        this.stepDefinitionId = stepDefinitionId;
    }

    public String getOperationName() {
        return operationName;
    }

    public void setOperationName(String operationName) {
        this.operationName = operationName;
    }

    public OperationRequestType getOperationType() {
        return operationType;
    }

    public void setOperationType(OperationRequestType operationType) {
        this.operationType = operationType;
    }

    public AuthStepResult getRequestAuthStepResult() {
        return requestAuthStepResult;
    }

    public void setRequestAuthStepResult(AuthStepResult requestAuthStepResult) {
        this.requestAuthStepResult = requestAuthStepResult;
    }

    public AuthMethod getRequestAuthMethod() {
        return requestAuthMethod;
    }

    public void setRequestAuthMethod(AuthMethod requestAuthMethod) {
        this.requestAuthMethod = requestAuthMethod;
    }

    public Long getResponsePriority() {
        return responsePriority;
    }

    public void setResponsePriority(Long responsePriority) {
        this.responsePriority = responsePriority;
    }

    public AuthMethod getResponseAuthMethod() {
        return responseAuthMethod;
    }

    public void setResponseAuthMethod(AuthMethod responseAuthMethod) {
        this.responseAuthMethod = responseAuthMethod;
    }

    public AuthResult getResponseResult() {
        return responseResult;
    }

    public void setResponseResult(AuthResult responseResult) {
        this.responseResult = responseResult;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StepDefinitionEntity that = (StepDefinitionEntity) o;

        return stepDefinitionId != null ? stepDefinitionId.equals(that.stepDefinitionId) : that.stepDefinitionId == null;
    }

    @Override
    public int hashCode() {
        return stepDefinitionId != null ? stepDefinitionId.hashCode() : 0;
    }

}
