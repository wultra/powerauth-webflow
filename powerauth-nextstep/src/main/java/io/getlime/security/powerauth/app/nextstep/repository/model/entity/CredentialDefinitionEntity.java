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

import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.CredentialCategory;
import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.CredentialDefinitionStatus;
import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.EncryptionAlgorithm;
import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.EndToEndEncryptionAlgorithm;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * Entity which stores credential definitions.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Entity
@Table(name = "ns_credential_definition")
@Data
@EqualsAndHashCode(of = {"name"})
public class CredentialDefinitionEntity implements Serializable {

    private static final long serialVersionUID = 1113222092995641439L;

    @Id
    @SequenceGenerator(name = "ns_credential_definition", sequenceName = "ns_credential_definition_seq")
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "ns_credential_definition")
    @Column(name = "credential_definition_id", nullable = false)
    private Long credentialDefinitionId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @ManyToOne
    @JoinColumn(name = "application_id", referencedColumnName = "application_id", nullable = false)
    private ApplicationEntity application;

    @ManyToOne
    @JoinColumn(name = "organization_id", referencedColumnName = "organization_id")
    private OrganizationEntity organization;

    @ManyToOne
    @JoinColumn(name = "credential_policy_id", referencedColumnName = "credential_policy_id", nullable = false)
    private CredentialPolicyEntity credentialPolicy;

    @Column(name = "category", nullable = false)
    @Enumerated(EnumType.STRING)
    private CredentialCategory category;

    @Column(name = "encryption_enabled")
    private boolean encryptionEnabled;

    @Column(name = "encryption_algorithm")
    @Enumerated(EnumType.STRING)
    private EncryptionAlgorithm encryptionAlgorithm;

    @ManyToOne
    @JoinColumn(name = "hashing_config_id", referencedColumnName = "hashing_config_id")
    private HashConfigEntity hashingConfig;

    @Column(name = "e2e_encryption_enabled")
    private boolean e2eEncryptionEnabled;

    @Column(name = "e2e_encryption_algorithm")
    @Enumerated(EnumType.STRING)
    private EndToEndEncryptionAlgorithm e2eEncryptionAlgorithm;

    @Column(name = "e2e_encryption_transform")
    private String e2eEncryptionCipherTransformation;

    @Column(name = "e2e_encryption_temporary")
    private boolean e2eEncryptionForTemporaryCredentialEnabled;

    @Column(name = "data_adapter_proxy_enabled")
    private boolean dataAdapterProxyEnabled;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private CredentialDefinitionStatus status;

    @Column(name = "timestamp_created", nullable = false)
    private Date timestampCreated;

    @Column(name = "timestamp_last_updated")
    private Date timestampLastUpdated;

}
