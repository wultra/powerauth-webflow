/*
 * PowerAuth Web Flow and related software components
 * Copyright (C) 2021 Wultra s.r.o.
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

import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.CredentialStatus;
import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.CredentialType;
import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.EncryptionAlgorithm;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * Entity which stores credentials.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Entity
@Table(name = "ns_credential_storage")
@Data
@EqualsAndHashCode(of = {"credentialDefinition", "user", "type", "username"})
public class CredentialEntity implements Serializable {

    private static final long serialVersionUID = -1331139715085676624L;

    @Id
    @Column(name = "credential_id", nullable = false)
    private String credentialId;

    @ManyToOne
    @JoinColumn(name = "credential_definition_id", referencedColumnName = "credential_definition_id", updatable = false, nullable = false)
    private CredentialDefinitionEntity credentialDefinition;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "user_id", updatable = false, nullable = false)
    private UserIdentityEntity user;

    @Column(name = "type", nullable = false)
    @Enumerated(EnumType.STRING)
    private CredentialType type;

    @Column(name = "user_name")
    private String username;

    @Column(name = "value", nullable = false)
    private String value;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private CredentialStatus status;

    @Column(name = "attempt_counter")
    private int attemptCounter;

    @Column(name = "failed_attempt_counter_soft")
    private int failedAttemptCounterSoft;

    @Column(name = "failed_attempt_counter_hard")
    private int failedAttemptCounterHard;

    @Column(name = "encryption_algorithm")
    @Enumerated(EnumType.STRING)
    private EncryptionAlgorithm encryptionAlgorithm;

    @ManyToOne
    @JoinColumn(name = "hashing_config_id", referencedColumnName = "hashing_config_id")
    private HashConfigEntity hashingConfig;

    @Column(name = "timestamp_created", nullable = false)
    private Date timestampCreated;

    @Column(name = "timestamp_expires")
    private Date timestampExpires;

    @Column(name = "timestamp_blocked")
    private Date timestampBlocked;

    @Column(name = "timestamp_last_updated")
    private Date timestampLastUpdated;

    @Column(name = "timestamp_last_credential_change")
    private Date timestampLastCredentialChange;

    @Column(name = "timestamp_last_username_change")
    private Date timestampLastUsernameChange;

}
