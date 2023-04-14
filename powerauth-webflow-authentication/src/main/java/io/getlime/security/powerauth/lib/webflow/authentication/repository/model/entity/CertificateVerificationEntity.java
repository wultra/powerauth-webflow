/*
 * PowerAuth Web Flow and related software components
 * Copyright (C) 2020 Wultra s.r.o.
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
package io.getlime.security.powerauth.lib.webflow.authentication.repository.model.entity;

import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthMethod;
import jakarta.persistence.*;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

/**
 * Entity which stores information about client TLS certificate verification.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Entity
@Table(name = "wf_certificate_verification")
public class CertificateVerificationEntity implements Serializable {

    private static final long serialVersionUID = 6993451807635603808L;

    @EmbeddedId
    private CertificateVerificationKey certificateVerificationKey;

    @Column(name = "client_certificate_issuer", nullable = false, length = 4000)
    private String certificateIssuer;

    @Column(name = "client_certificate_subject", nullable = false, length = 4000)
    private String certificateSubject;

    @Column(name = "client_certificate_sn", nullable = false)
    private String certificateSerialNumber;

    @Column(name = "operation_data", nullable = false)
    private String operationData;

    @Column(name = "timestamp_verified", nullable = false)
    private Date timestampVerified;

    /**
     * Default constructor.
     */
    public CertificateVerificationEntity() {
    }

    /**
     * Entity constructor.
     * @param operationId Operation ID.
     * @param authMethod Authentication method.
     * @param certificateIssuer Certificate issuer.
     * @param certificateSubject Certificate subject.
     * @param certificateSerialNumber Certificate serial number.
     */
    public CertificateVerificationEntity(String operationId, AuthMethod authMethod, String certificateIssuer, String certificateSubject, String certificateSerialNumber) {
        this.certificateVerificationKey = new CertificateVerificationKey(operationId, authMethod);
        this.certificateIssuer = certificateIssuer;
        this.certificateSubject = certificateSubject;
        this.certificateSerialNumber = certificateSerialNumber;
        this.timestampVerified = new Date();
    }

    public CertificateVerificationKey getCertificateVerificationKey() {
        return certificateVerificationKey;
    }

    public void setCertificateVerificationKey(CertificateVerificationKey certificateVerificationKey) {
        this.certificateVerificationKey = certificateVerificationKey;
    }

    public String getOperationId() {
        return certificateVerificationKey.operationId;
    }

    public AuthMethod getAuthMethod() {
        return certificateVerificationKey.authMethod;
    }

    public String getCertificateIssuer() {
        return certificateIssuer;
    }

    public void setCertificateIssuer(String certificateIssuer) {
        this.certificateIssuer = certificateIssuer;
    }

    public String getCertificateSubject() {
        return certificateSubject;
    }

    public void setCertificateSubject(String certificateSubject) {
        this.certificateSubject = certificateSubject;
    }

    public String getCertificateSerialNumber() {
        return certificateSerialNumber;
    }

    public void setCertificateSerialNumber(String certificateSerialNumber) {
        this.certificateSerialNumber = certificateSerialNumber;
    }

    public String getOperationData() {
        return operationData;
    }

    public void setOperationData(String operationData) {
        this.operationData = operationData;
    }

    public Date getTimestampVerified() {
        return timestampVerified;
    }

    public void setTimestampVerified(Date timestampVerified) {
        this.timestampVerified = timestampVerified;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CertificateVerificationEntity that = (CertificateVerificationEntity) o;
        return certificateVerificationKey == that.certificateVerificationKey &&
                certificateIssuer.equals(that.certificateIssuer) &&
                certificateSubject.equals(that.certificateSubject) &&
                certificateSerialNumber.equals(that.certificateSerialNumber) &&
                operationData.equals(that.operationData);
    }

    @Override
    public int hashCode() {
        return Objects.hash(certificateVerificationKey, certificateIssuer, certificateSubject, certificateSerialNumber, operationData);
    }

    @Embeddable
    public static class CertificateVerificationKey implements Serializable {

        private static final long serialVersionUID = -8783963465967422879L;

        @Column(name = "operation_id", nullable = false)
        private String operationId;

        @Column(name = "auth_method", nullable = false)
        @Enumerated(EnumType.STRING)
        private AuthMethod authMethod;

        public CertificateVerificationKey() {
        }

        public CertificateVerificationKey(String operationId, AuthMethod authMethod) {
            this.operationId = operationId;
            this.authMethod = authMethod;
        }

        public String getOperationId() {
            return operationId;
        }

        public void setOperationId(String operationId) {
            this.operationId = operationId;
        }

        public AuthMethod getAuthMethod() {
            return authMethod;
        }

        public void setAuthMethod(AuthMethod authMethod) {
            this.authMethod = authMethod;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            CertificateVerificationKey that = (CertificateVerificationKey) o;
            return operationId.equals(that.operationId) &&
                    authMethod == that.authMethod;
        }

        @Override
        public int hashCode() {
            return Objects.hash(operationId, authMethod);
        }
    }
}
