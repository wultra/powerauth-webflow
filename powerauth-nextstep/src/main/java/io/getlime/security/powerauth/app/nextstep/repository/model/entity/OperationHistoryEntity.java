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

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthMethod;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthResult;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthStepResult;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
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

    @Serial
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

    @Column(name = "request_params", length = 4000)
    private String requestParams;

    @Column(name = "response_result", nullable = false)
    @Enumerated(EnumType.STRING)
    private AuthResult responseResult;

    @Column(name = "response_result_description")
    private String responseResultDescription;

    @Column(name = "response_steps", length = 4000)
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
    @JoinColumn(name = "authentication_id")
    private AuthenticationEntity authentication;

    @Column(name = "pa_operation_id")
    private String powerAuthOperationId;

    @Column(name = "pa_auth_context")
    private String powerAuthAuthenticationContext;

    @ToString.Exclude
    @JsonIgnore
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
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OperationHistoryKey implements Serializable {

        @Serial
        private static final long serialVersionUID = 7125401949386229372L;

        @Column(name = "operation_id")
        private String operationId;

        @Column(name = "result_id")
        private Long resultId;

    }
}
