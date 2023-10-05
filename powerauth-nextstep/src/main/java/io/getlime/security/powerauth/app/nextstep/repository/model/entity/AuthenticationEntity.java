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

import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.AuthenticationResult;
import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.AuthenticationType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * Entity which stores authentication attempts.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Entity
@Table(name = "ns_authentication")
@Data
@EqualsAndHashCode(of = {"userId", "authenticationType", "credential", "otp", "operation", "timestampCreated"})
public class AuthenticationEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1598100682966462736L;

    @Id
    @Column(name = "authentication_id", nullable = false)
    private String authenticationId;

    // User identity may not be present in Next Step, foreign key reference is optional
    @Column(name = "user_id", updatable = false)
    private String userId;

    @Column(name = "type", nullable = false)
    @Enumerated(EnumType.STRING)
    private AuthenticationType authenticationType;

    @ManyToOne
    @JoinColumn(name = "credential_id", referencedColumnName = "credential_id", updatable = false)
    private CredentialEntity credential;

    @ManyToOne
    @JoinColumn(name = "otp_id", referencedColumnName = "otp_id", updatable = false)
    private OtpEntity otp;

    @ManyToOne
    @JoinColumn(name = "operation_id", referencedColumnName = "operation_id", updatable = false)
    private OperationEntity operation;

    @Column(name = "result", nullable = false)
    @Enumerated(EnumType.STRING)
    private AuthenticationResult result;

    @Column(name = "result_credential")
    @Enumerated(EnumType.STRING)
    private AuthenticationResult resultCredential;

    @Column(name = "result_otp")
    @Enumerated(EnumType.STRING)
    private AuthenticationResult resultOtp;

    @Column(name = "timestamp_created", nullable = false)
    private Date timestampCreated;

}
