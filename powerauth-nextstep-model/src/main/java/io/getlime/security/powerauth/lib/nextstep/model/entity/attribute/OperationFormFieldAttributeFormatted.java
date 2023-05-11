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

import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.ValueFormatType;

import java.util.HashMap;
import java.util.Map;

/**
 * Class that represents a formatted form field attribute. Formatting is done based on value format type.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class OperationFormFieldAttributeFormatted extends OperationFormFieldAttribute {

    /**
     * Type of formatted value.
     */
    protected ValueFormatType valueFormatType;

    /**
     * Formatted values.
     */
    protected final Map<String, String> formattedValues = new HashMap<>();

    /**
     * Get value format type of this attribute.
     * @return Value format type.
     */
    public ValueFormatType getValueFormatType() {
        return valueFormatType;
    }

    /**
     * Get formatted values for this attribute.
     * @return Formatted value.
     */
    public Map<String, String> getFormattedValues() {
        return formattedValues;
    }

    /**
     * Add formatted value for this attribute.
     * @param key Value key.
     * @param formattedValue Formatted value.
     */
    public void addFormattedValue(String key, String formattedValue) {
        formattedValues.put(key, formattedValue);
    }

    /**
     * Add multiple formatted values.
     * @param formattedValues Formatted values.
     */
    public void addFormattedValues(Map<String, String> formattedValues) {
        this.formattedValues.putAll(formattedValues);
    }

}
