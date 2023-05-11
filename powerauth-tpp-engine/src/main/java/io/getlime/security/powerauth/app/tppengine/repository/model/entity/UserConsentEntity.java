/*
 * PowerAuth Web Flow and related software components
 * Copyright (C) 2019 Wultra s.r.o.
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

package io.getlime.security.powerauth.app.tppengine.repository.model.entity;

import jakarta.persistence.*;

import java.io.Serializable;
import java.util.Date;

/**
 * Entity representing the current status of a consent.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
@Entity
@Table(name = "tpp_user_consent")
public class UserConsentEntity implements Serializable {

    private static final long serialVersionUID = 4873304514084189625L;

    @Id
    @SequenceGenerator(name = "tpp_user_consent", sequenceName = "tpp_user_consent_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "tpp_user_consent")
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "client_id")
    private String clientId;

    @Column(name = "consent_id")
    private String consentId;

    @Column(name = "consent_parameters")
    private String parameters;

    @Column(name = "external_id")
    private String externalId;

    @Column(name = "timestamp_created")
    private Date timestampCreated;

    @Column(name = "timestamp_updated")
    private Date timestampUpdated;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getConsentId() {
        return consentId;
    }

    public void setConsentId(String consentId) {
        this.consentId = consentId;
    }

    public String getParameters() {
        return parameters;
    }

    public void setParameters(String parameters) {
        this.parameters = parameters;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public Date getTimestampCreated() {
        return timestampCreated;
    }

    public void setTimestampCreated(Date timestampCreated) {
        this.timestampCreated = timestampCreated;
    }

    public Date getTimestampUpdated() {
        return timestampUpdated;
    }

    public void setTimestampUpdated(Date timestampUpdated) {
        this.timestampUpdated = timestampUpdated;
    }
}
