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
