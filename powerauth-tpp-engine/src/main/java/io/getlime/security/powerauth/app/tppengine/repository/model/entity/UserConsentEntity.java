/*
 * Copyright 2019 Wultra s.r.o.
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

package io.getlime.security.powerauth.app.tppengine.repository.model.entity;

import javax.persistence.*;
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
    @SequenceGenerator(name = "tpp_user_consent", sequenceName = "tpp_user_consent_seq")
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
