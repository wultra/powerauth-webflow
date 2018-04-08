/*
 * Copyright 2017 Lime - HighTech Solutions s.r.o.
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
package io.getlime.security.powerauth.lib.mtoken.model.entity;

import io.getlime.security.powerauth.lib.mtoken.model.entity.attributes.Attribute;

import java.util.ArrayList;
import java.util.List;

/**
 * Class representing displayable attributes for mobile token data.
 *
 * @author Petr Dvorak, petr@lime-company.eu
 */
public class FormData {

    private String title;
    private String message;
    private List<Attribute> attributes;

    /**
     * Default constructor.
     */
    public FormData() {
        this.attributes = new ArrayList<>();
    }

    /**
     * Get localized operation title message.
     * @return Title message.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Set localized operation title message.
     * @param title Title message.
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Get localized operation message.
     * @return Operation message.
     */
    public String getMessage() {
        return message;
    }

    /**
     * Set localized operation message.
     * @param message Operation message.
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Get form data attributes.
     * @return Form data attributes.
     */
    public List<Attribute> getAttributes() {
        return attributes;
    }

    /**
     * Set form data attributes.
     * @param attributes Form data attributes.
     */
    public void setAttributes(List<Attribute> attributes) {
        this.attributes = attributes;
    }
}
