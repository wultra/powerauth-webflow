/*
 * PowerAuth Web Flow and related software components
 * Copyright (C) 2017 Wultra s.r.o.
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
package io.getlime.security.powerauth.lib.mtoken.model.entity;

import io.getlime.security.powerauth.lib.mtoken.model.entity.attributes.Attribute;

import java.util.ArrayList;
import java.util.List;

/**
 * Class representing displayable attributes for mobile token data.
 *
 * @author Petr Dvorak, petr@wultra.com
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
