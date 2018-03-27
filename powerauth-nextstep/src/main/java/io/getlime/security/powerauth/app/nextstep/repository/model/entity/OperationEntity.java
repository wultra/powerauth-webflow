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

import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthResult;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * Entity which stores status of an operation, its parameters and last result.
 *
 * @author Roman Strobl
 */
@Entity
@Table(name = "ns_operation")
public class OperationEntity implements Serializable {

    private static final long serialVersionUID = -8991119412441607003L;

    @Id
    @Column(name = "operation_id")
    private String operationId;

    @Column(name = "operation_name")
    private String operationName;

    @Column(name = "operation_form_data")
    private String operationFormData;

    @Column(name = "operation_data")
    private String operationData;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "result")
    @Enumerated(EnumType.STRING)
    private AuthResult result;

    @Column(name = "timestamp_created")
    private Date timestampCreated;

    @Column(name = "timestamp_expires")
    private Date timestampExpires;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "operation")
    @OrderBy("result_id")
    private List<OperationHistoryEntity> operationHistory;

    public String getOperationId() {
        return operationId;
    }

    public void setOperationId(String operationId) {
        this.operationId = operationId;
    }

    public String getOperationName() {
        return operationName;
    }

    public void setOperationName(String operationName) {
        this.operationName = operationName;
    }

    public String getOperationFormData() {
        return operationFormData;
    }

    public void setOperationFormData(String operationFormData) {
        this.operationFormData = operationFormData;
    }

    public String getOperationData() {
        return operationData;
    }

    public void setOperationData(String operationData) {
        this.operationData = operationData;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public AuthResult getResult() {
        return result;
    }

    public void setResult(AuthResult result) {
        this.result = result;
    }

    public Date getTimestampCreated() {
        return timestampCreated;
    }

    public void setTimestampCreated(Date timestampCreated) {
        this.timestampCreated = timestampCreated;
    }

    public Date getTimestampExpires() {
        return timestampExpires;
    }

    public void setTimestampExpires(Date timestampExpires) {
        this.timestampExpires = timestampExpires;
    }

    /**
     * Is the operation expired?
     *
     * @return true if expired
     */
    public boolean isExpired() {
        return new Date().after(timestampExpires);
    }

    public List<OperationHistoryEntity> getOperationHistory() {
        return operationHistory;
    }

    public void setOperationHistory(List<OperationHistoryEntity> operationHistory) {
        this.operationHistory = operationHistory;
    }

    /**
     * Returns current OperationHistoryEntity. Null value is return in case there is no history for this operation.
     *
     * @return Current OperationHistoryEntity
     */
    public OperationHistoryEntity getCurrentOperationHistoryEntity() {
        if (operationHistory == null || operationHistory.isEmpty()) {
            return null;
        }
        return operationHistory.get(operationHistory.size() - 1);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        OperationEntity that = (OperationEntity) o;

        return operationId != null ? operationId.equals(that.operationId) : that.operationId == null;
    }

    @Override
    public int hashCode() {
        return operationId != null ? operationId.hashCode() : 0;
    }

}
