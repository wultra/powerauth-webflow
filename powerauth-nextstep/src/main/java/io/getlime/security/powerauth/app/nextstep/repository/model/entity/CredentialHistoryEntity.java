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

import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.EncryptionAlgorithm;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
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
