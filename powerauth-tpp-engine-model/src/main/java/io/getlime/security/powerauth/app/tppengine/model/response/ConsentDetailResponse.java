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

package io.getlime.security.powerauth.app.tppengine.model.response;

/**
 * Class representing a consent detail response.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
public class ConsentDetailResponse {

    /**
     * Identifier of the consent used for the consent query.
     */
    private String id;

    /**
     * Name of the consent, used for display of the list of existing consents.
     */
    private String name;

    /**
     * Text of the consent. It may contains placeholders in the form `{{PLACEHOLDER}}` that will
     * be replaced at the later stages with a specific text (for example, a TPP name, an app name
     * or resource owner data).
     */
    private String text;

    /**
     * Current version of the consent.
     */
    private Long version;

    /**
     * Non-parametric constructor.
     */
    public ConsentDetailResponse() {
    }

    /**
     * Constructor for consent information.
     * @param id Identifier of the consent.
     * @param name Name of the consent, used for display.
     * @param text Text of the consent with the placeholders (raw consent string).
     * @param version Consent version.
     */
    public ConsentDetailResponse(String id, String name, String text, Long version) {
        this.id = id;
        this.name = name;
        this.text = text;
        this.version = version;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
}
