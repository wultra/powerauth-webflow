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

import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.OtpPolicyStatus;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.OtpGenerationAlgorithm;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * Entity which stores one time password policies.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Entity
@Table(name = "ns_otp_policy")
@Data
@EqualsAndHashCode(of = "name")
public class OtpPolicyEntity implements Serializable {

    private static final long serialVersionUID = -1157742528088577985L;

    @Id
    @SequenceGenerator(name = "ns_otp_policy", sequenceName = "ns_otp_policy_seq")
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "ns_otp_policy")
    @Column(name = "otp_policy_id", nullable = false)
    private Long otpPolicyId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private OtpPolicyStatus status;

    @Column(name = "length", nullable = false)
    private Integer length;

    @Column(name = "attempt_limit")
    private Integer attemptLimit;

    @Column(name = "expiration_time")
    private Long expirationTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "gen_algorithm", nullable = false)
    private OtpGenerationAlgorithm genAlgorithm;

    @Column(name = "gen_param", length = 4096)
    private String genParam;

    @Column(name = "timestamp_created", nullable = false)
    private Date timestampCreated;

    @Column(name = "timestamp_last_updated")
    private Date timestampLastUpdated;
}
