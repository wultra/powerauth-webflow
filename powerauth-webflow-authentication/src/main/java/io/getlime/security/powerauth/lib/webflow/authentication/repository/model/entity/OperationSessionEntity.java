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
package io.getlime.security.powerauth.lib.webflow.authentication.repository.model.entity;

import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthResult;
import jakarta.persistence.*;

import java.io.Serial;
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

    @Serial
    private static final long serialVersionUID = -5370629764971469306L;

    @Id
    @Column(name = "operation_id")
    private String operationId;

    @Column(name = "http_session_id")
    private String httpSessionId;

    @Column(name = "operation_hash")
    private String operationHash;

    @Column(name = "websocket_session_id")
    private String webSocketSessionId;

    @Column(name = "client_ip_address")
    private String clientIpAddress;

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

    public String getOperationHash() {
        return operationHash;
    }

    public void setOperationHash(String operationHash) {
        this.operationHash = operationHash;
    }

    public String getWebSocketSessionId() {
        return webSocketSessionId;
    }

    public void setWebSocketSessionId(String webSocketSessionId) {
        this.webSocketSessionId = webSocketSessionId;
    }

    public String getClientIp() {
        return clientIpAddress;
    }

    public void setClientIp(String clientIpAddress) {
        this.clientIpAddress = clientIpAddress;
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
