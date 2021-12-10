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

import java.util.Map;

/**
 * Class representing an operation form field attribute for generic key-value pair.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
public class OperationKeyValueFieldAttribute extends OperationFormFieldAttributeFormatted {

    /**
     * Attribute value.
     */
    protected String value;

    /**
     * Default constructor.
     */
    public OperationKeyValueFieldAttribute() {
        this.type = Type.KEY_VALUE;
        this.valueFormatType = ValueFormatType.TEXT;
    }

    /**
     * Constructor with value format type.
     * @param valueFormatType Value format type.
     */
    public OperationKeyValueFieldAttribute(ValueFormatType valueFormatType) {
        this.type = Type.KEY_VALUE;
        this.valueFormatType = valueFormatType;
    }

    /**
     * Constructor with all details.
     * @param id Attribute ID.
     * @param label Label.
     * @param value Value.
     * @param valueFormatType Value format type.
     * @param formattedValues Formatted values.
     */
    public OperationKeyValueFieldAttribute(String id, String label, String value, ValueFormatType valueFormatType, Map<String, String> formattedValues) {
        this.type = Type.KEY_VALUE;
        this.id = id;
        this.label = label;
        this.value = value;
        this.valueFormatType = valueFormatType;
        if (formattedValues != null) {
            addFormattedValues(formattedValues);
        }
    }

    /**
     * Get attribute value.
     * @return Attribute value.
     */
    public String getValue() {
        return value;
    }

    /**
     * Set attribute value.
     * @param value Attribute value.
     */
    public void setValue(String value) {
        this.value = value;
    }
}