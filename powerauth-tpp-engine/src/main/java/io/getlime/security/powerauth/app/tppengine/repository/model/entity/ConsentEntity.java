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

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.io.Serial;
import java.io.Serializable;

/**
 * Database entity representing a consent.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
@Entity
@Table(name = "tpp_consent")
public class ConsentEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = -3031091688188605604L;

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
