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

import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.CredentialPolicyStatus;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.CredentialGenerationAlgorithm;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.UsernameGenerationAlgorithm;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * Entity which stores credential policies.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Entity
@Table(name = "ns_credential_policy")
@Data
@EqualsAndHashCode(of = "name")
public class CredentialPolicyEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = -4580881377865304625L;

    @Id
    @SequenceGenerator(name = "ns_credential_policy", sequenceName = "ns_credential_policy_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "ns_credential_policy")
    @Column(name = "credential_policy_id", nullable = false)
    private Long credentialPolicyId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private CredentialPolicyStatus status;

    @Column(name = "username_length_min")
    private Integer usernameLengthMin;

    @Column(name = "username_length_max")
    private Integer usernameLengthMax;

    @Column(name = "username_allowed_pattern")
    private String usernameAllowedPattern;

    @Column(name = "credential_length_min")
    private Integer credentialLengthMin;

    @Column(name = "credential_length_max")
    private Integer credentialLengthMax;

    @Column(name = "limit_soft")
    private Integer limitSoft;

    @Column(name = "limit_hard")
    private Integer limitHard;

    @Column(name = "check_history_count")
    private int checkHistoryCount;

    @Column(name = "rotation_enabled")
    private boolean rotationEnabled;

    @Column(name = "rotation_days")
    private Integer rotationDays;

    @Column(name = "credential_temp_expiration")
    private Integer temporaryCredentialExpirationTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "username_gen_algorithm", nullable = false)
    private UsernameGenerationAlgorithm usernameGenAlgorithm;

    @Column(name = "username_gen_param", length = 4000)
    private String usernameGenParam;

    @Enumerated(EnumType.STRING)
    @Column(name = "credential_gen_algorithm", nullable = false)
    private CredentialGenerationAlgorithm credentialGenAlgorithm;

    @Column(name = "credential_gen_param", length = 4000)
    private String credentialGenParam;

    @Column(name = "credential_val_param", length = 4000)
    private String credentialValParam;

    @Column(name = "timestamp_created", nullable = false)
    private Date timestampCreated;

    @Column(name = "timestamp_last_updated")
    private Date timestampLastUpdated;

}
