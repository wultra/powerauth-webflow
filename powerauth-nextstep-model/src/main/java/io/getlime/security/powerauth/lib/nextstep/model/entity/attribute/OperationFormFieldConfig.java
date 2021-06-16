/*
 * Copyright 2018 Wultra s.r.o.
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
package io.getlime.security.powerauth.lib.nextstep.model.entity.attribute;

import java.util.Objects;

/**
 * Operation form data configuration.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 *
 */
public class OperationFormFieldConfig {

    private String id;
    private boolean enabled;
    private String defaultValue;

    /**
     * Default constructor.
     */
    public OperationFormFieldConfig() {
    }

    /**
     * Constructor with all details.
     * @param id Field ID.
     * @param enabled Whether field is enabled.
     * @param defaultValue Default field value.
     */
    public OperationFormFieldConfig(String id, boolean enabled, String defaultValue) {
        this.id = id;
        this.enabled = enabled;
        this.defaultValue = defaultValue;
    }

    /**
     * Get field ID.
     * @return Field ID.
     */
    public String getId() {
        return id;
    }

    /**
     * Set field ID.
     * @param id Field ID.
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Get whether field is enabled.
     * @return Whether field is enabled.
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Set whether field is enabled.
     * @param enabled Whether field is enabled.
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Get default value.
     * @return Default value.
     */
    public String getDefaultValue() {
        return defaultValue;
    }

    /**
     * Set default value.
     * @param defaultValue Default value.
     */
    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OperationFormFieldConfig fieldConfig = (OperationFormFieldConfig) o;
        return Objects.equals(id, fieldConfig.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
