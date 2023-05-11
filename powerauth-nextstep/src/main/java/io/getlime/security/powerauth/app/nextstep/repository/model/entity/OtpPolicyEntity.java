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

import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.OtpPolicyStatus;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.OtpGenerationAlgorithm;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

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
    @SequenceGenerator(name = "ns_otp_policy", sequenceName = "ns_otp_policy_seq", allocationSize = 1)
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

    @Column(name = "gen_param", length = 4000)
    private String genParam;

    @Column(name = "timestamp_created", nullable = false)
    private Date timestampCreated;

    @Column(name = "timestamp_last_updated")
    private Date timestampLastUpdated;
}
