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
import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.OtpStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * Entity which stores one time passwords.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Entity
@Table(name = "ns_otp_storage")
@Data
@EqualsAndHashCode(of = {"otpDefinition", "userId", "credentialDefinition", "timestampCreated"})
public class OtpEntity implements Serializable {

    private static final long serialVersionUID = 8483820995210446509L;

    @Id
    @Column(name = "otp_id", nullable = false)
    private String otpId;

    @ManyToOne
    @JoinColumn(name = "otp_definition_id", referencedColumnName = "otp_definition_id", updatable = false, nullable = false)
    private OtpDefinitionEntity otpDefinition;

    // User identity may not be present in Next Step, foreign key reference is optional
    @Column(name = "user_id", updatable = false)
    private String userId;

    @ManyToOne
    @JoinColumn(name = "credential_definition_id", referencedColumnName = "credential_definition_id", updatable = false)
    private CredentialDefinitionEntity credentialDefinition;

    @ManyToOne
    @JoinColumn(name = "operation_id", referencedColumnName = "operation_id", updatable = false)
    private OperationEntity operation;

    @Column(name = "value")
    private String value;

    @Column(name = "salt")
    private byte[] salt;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private OtpStatus status;

    @Column(name = "otp_data")
    private String otpData;

    @Column(name = "attempt_counter")
    private int attemptCounter;

    @Column(name = "failed_attempt_counter")
    private int failedAttemptCounter;

    @Column(name = "encryption_algorithm")
    @Enumerated(EnumType.STRING)
    private EncryptionAlgorithm encryptionAlgorithm;

    @Column(name = "timestamp_created", nullable = false)
    private Date timestampCreated;

    @Column(name = "timestamp_verified")
    private Date timestampVerified;

    @Column(name = "timestamp_blocked")
    private Date timestampBlocked;

    @Column(name = "timestamp_expires")
    private Date timestampExpires;

}
