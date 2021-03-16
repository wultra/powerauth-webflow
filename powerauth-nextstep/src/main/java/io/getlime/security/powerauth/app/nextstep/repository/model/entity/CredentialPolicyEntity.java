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

import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.CredentialPolicyStatus;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.CredentialGenerationAlgorithm;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.UsernameGenerationAlgorithm;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
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

    private static final long serialVersionUID = -4580881377865304625L;

    @Id
    @SequenceGenerator(name = "ns_credential_policy", sequenceName = "ns_credential_policy_seq")
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

    @Enumerated(EnumType.STRING)
    @Column(name = "username_gen_algorithm", nullable = false)
    private UsernameGenerationAlgorithm usernameGenAlgorithm;

    @Column(name = "username_gen_param", length = 4096)
    private String usernameGenParam;

    @Enumerated(EnumType.STRING)
    @Column(name = "credential_gen_algorithm", nullable = false)
    private CredentialGenerationAlgorithm credentialGenAlgorithm;

    @Column(name = "credential_gen_param", length = 4096)
    private String credentialGenParam;

    @Column(name = "credential_val_param", length = 4096)
    private String credentialValParam;

    @Column(name = "timestamp_created", nullable = false)
    private Date timestampCreated;

    @Column(name = "timestamp_last_updated")
    private Date timestampLastUpdated;

}
