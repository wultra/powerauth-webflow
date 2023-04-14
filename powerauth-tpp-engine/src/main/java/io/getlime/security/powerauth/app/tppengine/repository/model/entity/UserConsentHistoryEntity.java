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

import io.getlime.security.powerauth.app.tppengine.model.enumeration.ConsentChange;
import jakarta.persistence.*;

import java.io.Serializable;
import java.util.Date;

/**
 * Database entity representing a historic event of consent approval or rejection,
 * useful mainly for auditing purposes and for informing the end user.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
@Entity
@Table(name = "tpp_user_consent_history")
public class UserConsentHistoryEntity implements Serializable {

    private static final long serialVersionUID = 6697728608700209704L;

    @Id
    @SequenceGenerator(name = "tpp_user_consent_history", sequenceName = "tpp_user_consent_history_seq")
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "tpp_user_consent_history")
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "client_id")
    private String clientId;

    @Column(name = "consent_id")
    private String consentId;

    @Column(name = "consent_change")
    @Enumerated(EnumType.STRING)
    private ConsentChange change;

    @Column(name = "external_id")
    private String externalId;

    @Column(name = "consent_parameters")
    private String parameters;

    @Column(name = "timestamp_created")
    private Date timestampCreated;


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

    public ConsentChange getChange() {
        return change;
    }

    public void setChange(ConsentChange change) {
        this.change = change;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public String getParameters() {
        return parameters;
    }

    public void setParameters(String parameters) {
        this.parameters = parameters;
    }

    public Date getTimestampCreated() {
        return timestampCreated;
    }

    public void setTimestampCreated(Date timestampCreated) {
        this.timestampCreated = timestampCreated;
    }

}
