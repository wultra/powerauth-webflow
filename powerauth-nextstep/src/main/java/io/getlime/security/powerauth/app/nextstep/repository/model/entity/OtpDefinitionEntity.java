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
import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.OtpDefinitionStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * Entity which stores one time password definitions.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Entity
@Table(name = "ns_otp_definition")
@Data
@EqualsAndHashCode(of = "name")
public class OtpDefinitionEntity implements Serializable {

    private static final long serialVersionUID = 5337106400618975622L;

    @Id
    @SequenceGenerator(name = "ns_otp_definition", sequenceName = "ns_otp_definition_seq")
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "ns_otp_definition")
    @Column(name = "otp_definition_id", nullable = false)
    private Long otpDefinitionId;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @ManyToOne
    @JoinColumn(name = "application_id", referencedColumnName = "application_id", nullable = false)
    private ApplicationEntity application;

    @ManyToOne
    @JoinColumn(name = "otp_policy_id", referencedColumnName = "otp_policy_id", nullable = false)
    private OtpPolicyEntity otpPolicy;

    @Column(name = "encryption_enabled")
    private boolean encryptionEnabled;

    @Column(name = "encryption_algorithm")
    private EncryptionAlgorithm encryptionAlgorithm;

    @Column(name = "data_adapter_proxy_enabled")
    private boolean dataAdapterProxyEnabled;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private OtpDefinitionStatus status;

    @Column(name = "timestamp_created", nullable = false)
    private Date timestampCreated;

    @Column(name = "timestamp_last_updated")
    private Date timestampLastUpdated;

}
