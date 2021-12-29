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

package io.getlime.security.powerauth.lib.dataadapter.model.entity;

/**
 * Option in OAuth 2.0 consent the user can either confirm or reject.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class ConsentOption {

    private String id;
    private String descriptionHtml;
    private boolean required;
    private ConsentOptionValue defaultValue = ConsentOptionValue.NOT_CHECKED;
    private ConsentOptionValue value;

    /**
     * Default constructor.
     */
    public ConsentOption() {
    }

    /**
     * Constructor with option details.
     * @param id Option identifier.
     * @param descriptionHtml Option description in HTML format.
     * @param required Whether the option is required.
     */
    public ConsentOption(String id, String descriptionHtml, boolean required) {
        this.id = id;
        this.descriptionHtml = descriptionHtml;
        this.required = required;
    }

    /**
     * Get option identifier.
     * @return Option identifier.
     */
    public String getId() {
        return id;
    }

    /**
     * Set option identifier.
     * @param id Option identifier.
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Get option description in HTML format.
     * @return Option description in HTML format.
     */
    public String getDescriptionHtml() {
        return descriptionHtml;
    }

    /**
     * Set option description in HTML format.
     * @param descriptionHtml Option description in HTML format.
     */
    public void setDescriptionHtml(String descriptionHtml) {
        this.descriptionHtml = descriptionHtml;
    }

    /**
     * Get whether option is required.
     * @return Whether option is required.
     */
    public boolean isRequired() {
        return required;
    }

    /**
     * Set whether option is required.
     * @param required Whether option is required.
     */
    public void setRequired(boolean required) {
        this.required = required;
    }

    /**
     * Default value of option, set to false by default.
     * @return Default value of option.
     */
    public ConsentOptionValue getDefaultValue() {
        return defaultValue;
    }

    /**
     * Set default value of option.
     * @param defaultValue Default value of option.
     */
    public void setDefaultValue(ConsentOptionValue defaultValue) {
        this.defaultValue = defaultValue;
    }

    /**
     * Get actual value of option after user action.
     * @return Actual value of option.
     */
    public ConsentOptionValue getValue() {
        return value;
    }

    /**
     * Set actual value of option after user action.
     * @param value Actual value of option.
     */
    public void setValue(ConsentOptionValue value) {
        this.value = value;
    }
}
