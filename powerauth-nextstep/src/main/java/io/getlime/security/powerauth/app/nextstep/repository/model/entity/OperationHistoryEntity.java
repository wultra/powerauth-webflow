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

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * Entity which stores history for an operation including request and response data.
 *
 * @author Roman Strobl
 */
@Entity
@Table(name = "ns_operation_history")
public class OperationHistoryEntity implements Serializable {

    private static final long serialVersionUID = 4536813173706547247L;

    @EmbeddedId
    private OperationHistoryKey primaryKey;

    @Column(name = "request_auth_step_result")
    @Enumerated(EnumType.STRING)
    private AuthStepResult requestAuthStepResult;

    @Column(name = "request_auth_method")
    @Enumerated(EnumType.STRING)
    private AuthMethod requestAuthMethod;

    @Column(name = "request_params")
    private String requestParams;

    @Column(name = "response_result")
    @Enumerated(EnumType.STRING)
    private AuthResult responseResult;

    @Column(name = "response_result_description")
    private String responseResultDescription;

    @Column(name = "response_steps")
    private String responseSteps;

    @Column(name = "response_timestamp_created")
    private Date responseTimestampCreated;

    @Column(name = "response_timestamp_expires")
    private Date responseTimestampExpires;

    @Column(name = "chosen_auth_method")
    @Enumerated(EnumType.STRING)
    private AuthMethod chosenAuthMethod;

    @ManyToOne
    @JoinColumn(name = "operation_id", insertable = false, updatable = false)
    private OperationEntity operation;

    public OperationHistoryEntity() {
    }

    public OperationHistoryEntity(String operationId, Long resultId) {
        primaryKey = new OperationHistoryKey(operationId, resultId);
    }

    public OperationHistoryKey getPrimaryKey() {
        return primaryKey;
    }

    public void setPrimaryKey(OperationHistoryKey primaryKey) {
        this.primaryKey = primaryKey;
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

    public String getRequestParams() {
        return requestParams;
    }

    public void setRequestParams(String requestParams) {
        this.requestParams = requestParams;
    }

    public AuthResult getResponseResult() {
        return responseResult;
    }

    public void setResponseResult(AuthResult responseResult) {
        this.responseResult = responseResult;
    }

    public String getResponseResultDescription() {
        return responseResultDescription;
    }

    public void setResponseResultDescription(String responseResultDescription) {
        this.responseResultDescription = responseResultDescription;
    }

    public String getResponseSteps() {
        return responseSteps;
    }

    public void setResponseSteps(String responseSteps) {
        this.responseSteps = responseSteps;
    }

    public Date getResponseTimestampCreated() {
        return responseTimestampCreated;
    }

    public void setResponseTimestampCreated(Date responseTimestampCreated) {
        this.responseTimestampCreated = responseTimestampCreated;
    }

    public Date getResponseTimestampExpires() {
        return responseTimestampExpires;
    }

    public void setResponseTimestampExpires(Date responseTimestampExpires) {
        this.responseTimestampExpires = responseTimestampExpires;
    }

    public AuthMethod getChosenAuthMethod() {
        return chosenAuthMethod;
    }

    public void setChosenAuthMethod(AuthMethod chosenAuthMethod) {
        this.chosenAuthMethod = chosenAuthMethod;
    }

    /**
     * Is the action expired?
     *
     * @return true if expired
     */
    public boolean isExpired() {
        return new Date().after(responseTimestampExpires);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        OperationHistoryEntity that = (OperationHistoryEntity) o;

        return getPrimaryKey() != null ? getPrimaryKey().equals(that.getPrimaryKey()) : that.getPrimaryKey() == null;
    }

    @Override
    public int hashCode() {
        return getPrimaryKey() != null ? getPrimaryKey().hashCode() : 0;
    }

    @Embeddable
    public static class OperationHistoryKey implements Serializable {

        @Column(name = "operation_id")
        private String operationId;

        @Column(name = "result_id")
        private Long resultId;

        public OperationHistoryKey() {
        }

        public OperationHistoryKey(String operationId, Long resultId) {
            this.operationId = operationId;
            this.resultId = resultId;
        }

        public String getOperationId() {
            return operationId;
        }

        public void setOperationId(String operationId) {
            this.operationId = operationId;
        }

        public Long getResultId() {
            return resultId;
        }

        public void setResultId(Long resultId) {
            this.resultId = resultId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            OperationHistoryKey that = (OperationHistoryKey) o;

            if (operationId != null ? !operationId.equals(that.operationId) : that.operationId != null) return false;
            return resultId != null ? resultId.equals(that.resultId) : that.resultId == null;
        }

        @Override
        public int hashCode() {
            int result = operationId != null ? operationId.hashCode() : 0;
            result = 31 * result + (resultId != null ? resultId.hashCode() : 0);
            return result;
        }
    }
}
