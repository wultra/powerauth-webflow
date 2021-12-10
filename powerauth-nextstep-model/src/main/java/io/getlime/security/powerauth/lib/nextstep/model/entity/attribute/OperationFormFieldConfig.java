/*
 * PowerAuth Web Flow and related software components
 * Copyright (C) 2018 Wultra s.r.o.
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
