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
package io.getlime.security.powerauth.lib.webflow.authentication.repository.model.entity;

import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthResult;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

/**
 * Entity which stores mapping of operations to HTTP sessions and operation results.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Entity
@Table(name = "wf_operation_session")
public class OperationSessionEntity implements Serializable {

    private static final long serialVersionUID = -5370629764971469306L;

    @Id
    @Column(name = "operation_id")
    private String operationId;

    @Column(name = "http_session_id")
    private String httpSessionId;

    @Column(name = "result")
    @Enumerated(EnumType.STRING)
    private AuthResult result;

    @Column(name = "timestamp_created")
    private Date timestampCreated;

    public OperationSessionEntity() {
        this.timestampCreated = new Date();
    }

    public OperationSessionEntity(String operationId, String httpSessionId, AuthResult result) {
        this.operationId = operationId;
        this.httpSessionId = httpSessionId;
        this.result = result;
        this.timestampCreated = new Date();
    }

    public String getOperationId() {
        return operationId;
    }

    public void setOperationId(String operationId) {
        this.operationId = operationId;
    }

    public String getHttpSessionId() {
        return httpSessionId;
    }

    public void setHttpSessionId(String httpSessionId) {
        this.httpSessionId = httpSessionId;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OperationSessionEntity that = (OperationSessionEntity) o;
        return Objects.equals(operationId, that.operationId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(operationId);
    }
}
