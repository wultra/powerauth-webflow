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

import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.EncryptionAlgorithm;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * Entity which stores credential history.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Entity
@Table(name = "ns_credential_history")
@Data
@EqualsAndHashCode(of = {"credentialDefinition", "user", "username", "timestampCreated"})
public class CredentialHistoryEntity implements Serializable {

    private static final long serialVersionUID = -3222892995455956072L;

    @Id
    @SequenceGenerator(name = "ns_credential_history", sequenceName = "ns_credential_history_seq")
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "ns_credential_history")
    @Column(name = "credential_history_id", nullable = false)
    private Long credentialHistoryId;

    @ManyToOne
    @JoinColumn(name = "credential_definition_id", referencedColumnName = "credential_definition_id", updatable = false, nullable = false)
    private CredentialDefinitionEntity credentialDefinition;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "user_id", updatable = false, nullable = false)
    private UserIdentityEntity user;

    @Column(name = "user_name")
    private String username;

    @Column(name = "value", nullable = false)
    private String value;

    @Column(name = "encryption_algorithm")
    @Enumerated(EnumType.STRING)
    private EncryptionAlgorithm encryptionAlgorithm;

    @ManyToOne
    @JoinColumn(name = "hashing_config_id", referencedColumnName = "hashing_config_id")
    private HashConfigEntity hashingConfig;

    @Column(name = "timestamp_created", nullable = false)
    private Date timestampCreated;

}
