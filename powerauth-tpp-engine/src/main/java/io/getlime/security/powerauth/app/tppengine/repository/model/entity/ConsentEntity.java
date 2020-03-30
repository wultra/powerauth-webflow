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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * Database entity representing a consent.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
@Entity
@Table(name = "tpp_consent")
public class ConsentEntity implements Serializable {

    @Id
    @Column(name = "consent_id", nullable = false)
    private String id;

    @Column(name = "consent_name")
    private String name;

    @Column(name = "consent_text")
    private String text;

    @Column(name = "version")
    private Long version;

    /**
     * Get consent ID.
     * @return Consent ID.
     */
    public String getId() {
        return id;
    }

    /**
     * Set consent ID.
     * @param id Consent ID.
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Get consent name.
     * @return Consent name.
     */
    public String getName() {
        return name;
    }

    /**
     * Set consent name.
     * @param name Consent name.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get consent text.
     * @return Consent text.
     */
    public String getText() {
        return text;
    }

    /**
     * Set consent text.
     * @param text Consent text.
     */
    public void setText(String text) {
        this.text = text;
    }

    /**
     * Get consent version.
     * @return Consent version.
     */
    public Long getVersion() {
        return version;
    }

    /**
     * Set consent version.
     * @param version Consent version.
     */
    public void setVersion(Long version) {
        this.version = version;
    }
}
