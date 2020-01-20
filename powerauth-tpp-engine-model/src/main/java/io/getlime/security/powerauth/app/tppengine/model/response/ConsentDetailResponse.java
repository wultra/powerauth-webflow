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
