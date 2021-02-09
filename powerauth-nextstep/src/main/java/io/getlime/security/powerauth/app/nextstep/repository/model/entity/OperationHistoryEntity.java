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
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * Entity which stores history for an operation including request and response data.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Entity
@Table(name = "ns_operation_history")
@Data
@EqualsAndHashCode(of = "primaryKey")
public class OperationHistoryEntity implements Serializable {

    private static final long serialVersionUID = 4536813173706547247L;

    @EmbeddedId
    private OperationHistoryKey primaryKey;

    @Column(name = "request_auth_step_result", nullable = false)
    @Enumerated(EnumType.STRING)
    private AuthStepResult requestAuthStepResult;

    @Column(name = "request_auth_method", nullable = false)
    @Enumerated(EnumType.STRING)
    private AuthMethod requestAuthMethod;

    @Column(name = "request_auth_instruments")
    private String requestAuthInstruments;

    @Column(name = "request_params")
    private String requestParams;

    @Column(name = "response_result", nullable = false)
    @Enumerated(EnumType.STRING)
    private AuthResult responseResult;

    @Column(name = "response_result_description")
    private String responseResultDescription;

    @Column(name = "response_steps")
    private String responseSteps;

    @Column(name = "response_timestamp_created", nullable = false)
    private Date responseTimestampCreated;

    @Column(name = "response_timestamp_expires", nullable = false)
    private Date responseTimestampExpires;

    @Column(name = "chosen_auth_method")
    @Enumerated(EnumType.STRING)
    private AuthMethod chosenAuthMethod;

    @Column(name = "mobile_token_active")
    private boolean mobileTokenActive;

    @ManyToOne
    @JoinColumn(name = "operation_id", insertable = false, updatable = false, nullable = false)
    private OperationEntity operation;

    public OperationHistoryEntity() {
    }

    public OperationHistoryEntity(String operationId, Long resultId) {
        primaryKey = new OperationHistoryKey(operationId, resultId);
    }

    /**
     * Is the action expired?
     *
     * @return true if expired
     */
    public boolean isExpired() {
        return new Date().after(responseTimestampExpires);
    }

    @Embeddable
    @Data
    public static class OperationHistoryKey implements Serializable {

        private static final long serialVersionUID = 7125401949386229372L;

        @Column(name = "operation_id")
        @NonNull
        private String operationId;

        @Column(name = "result_id")
        @NonNull
        private Long resultId;

    }
}
