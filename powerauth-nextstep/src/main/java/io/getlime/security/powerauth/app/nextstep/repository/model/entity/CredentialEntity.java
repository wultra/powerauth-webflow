/*
 * Copyright 2021 Wultra s.r.o.
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

import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.CredentialStatus;
import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.CredentialType;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

/**
 * Entity which stores credentials.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Entity
@Table(name = "ns_credential_storage")
@Data
@EqualsAndHashCode(of = {"credentialDefinition", "userId", "type", "username"})
public class CredentialEntity implements Serializable {

    private static final long serialVersionUID = -1331139715085676624L;

    @Id
    @GeneratedValue
    @Column(name = "credential_id", nullable = false)
    private UUID credentialId;

    @ManyToOne
    @JoinColumn(name = "credential_definition_id", referencedColumnName = "credential_definition_id", updatable = false, nullable = false)
    private CredentialDefinitionEntity credentialDefinition;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "user_id", updatable = false, nullable = false)
    private UserIdentityEntity userId;

    @Column(name = "type", nullable = false)
    private CredentialType type;

    @Column(name = "user_name")
    private String username;

    @Column(name = "value", nullable = false)
    private String value;

    @Column(name = "status", nullable = false)
    private CredentialStatus status;

    @Column(name = "attempt_counter")
    private long attemptCounter;

    @Column(name = "failed_attempt_counter_soft")
    private long failedAttemptCounterSoft;

    @Column(name = "failed_attempt_counter_hard")
    private long failedAttemptCounterHard;

    @Column(name = "timestamp_created", nullable = false)
    private Date timestampCreated;

    @Column(name = "timestamp_expired")
    private Date timestampExpired;

    @Column(name = "timestamp_blocked")
    private Date timestampBlocked;

    @Column(name = "timestamp_last_updated")
    private Date timestampLastUpdated;

    @Column(name = "timestamp_last_credential_change")
    private Date timestampLastCredentialChange;

}
