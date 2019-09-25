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

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

/**
 * Entity which stores AFS requests and responses.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Entity
@Table(name = "ns_operation_afs")
public class OperationAfsActionEntity implements Serializable {

    private static final long serialVersionUID = 744614077188309148L;

    @Id
    @SequenceGenerator(name = "ns_operation_afs", sequenceName = "ns_operation_afs_seq")
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

    @Column(name = "timestamp_created")
    private Date timestampCreated;

    @ManyToOne
    @JoinColumn(name = "operation_id", referencedColumnName = "operation_id", updatable = false)
    private OperationEntity operation;

    public Long getAfsActionId() {
        return afsActionId;
    }

    public void setAfsActionId(Long afsActionId) {
        this.afsActionId = afsActionId;
    }

    public OperationEntity getOperation() {
        return operation;
    }

    public void setOperation(OperationEntity operation) {
        this.operation = operation;
    }

    public String getAfsAction() {
        return afsAction;
    }

    public void setAfsAction(String afsAction) {
        this.afsAction = afsAction;
    }

    public int getStepIndex() {
        return stepIndex;
    }

    public void setStepIndex(int stepIndex) {
        this.stepIndex = stepIndex;
    }

    public String getRequestAfsExtras() {
        return requestAfsExtras;
    }

    public void setRequestAfsExtras(String requestAfsExtras) {
        this.requestAfsExtras = requestAfsExtras;
    }

    public boolean isAfsResponseApplied() {
        return afsResponseApplied;
    }

    public void setAfsResponseApplied(boolean afsResponseApplied) {
        this.afsResponseApplied = afsResponseApplied;
    }

    public String getAfsLabel() {
        return afsLabel;
    }

    public void setAfsLabel(String afsLabel) {
        this.afsLabel = afsLabel;
    }

    public String getResponseAfsExtras() {
        return responseAfsExtras;
    }

    public void setResponseAfsExtras(String responseAfsExtras) {
        this.responseAfsExtras = responseAfsExtras;
    }

    public Date getTimestampCreated() {
        return timestampCreated;
    }

    public void setTimestampCreated(Date timestampCreated) {
        this.timestampCreated = timestampCreated;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OperationAfsActionEntity that = (OperationAfsActionEntity) o;
        return stepIndex == that.stepIndex &&
                afsResponseApplied == that.afsResponseApplied &&
                afsActionId.equals(that.afsActionId) &&
                afsAction.equals(that.afsAction) &&
                Objects.equals(requestAfsExtras, that.requestAfsExtras) &&
                Objects.equals(afsLabel, that.afsLabel) &&
                Objects.equals(responseAfsExtras, that.responseAfsExtras) &&
                Objects.equals(timestampCreated, that.timestampCreated) &&
                operation.equals(that.operation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(afsActionId, afsAction, stepIndex, requestAfsExtras, afsResponseApplied, afsLabel, responseAfsExtras, timestampCreated, operation);
    }
}
