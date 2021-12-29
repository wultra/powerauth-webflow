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
package io.getlime.security.powerauth.lib.dataadapter.model.entity.attribute;

import io.getlime.security.powerauth.lib.dataadapter.model.enumeration.ValueFormatType;

import java.util.Map;

/**
 * Class representing an operation form field attribute for a heading.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class HeadingAttribute extends KeyValueAttribute {

    /**
     * Default constructor.
     */
    public HeadingAttribute() {
        this.type = Type.HEADING;
    }

    /**
     * Constructor with value format type.
     * @param valueFormatType Value format type.
     */
    public HeadingAttribute(ValueFormatType valueFormatType) {
        this.type = Type.HEADING;
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
    public HeadingAttribute(String id, String label, String value, ValueFormatType valueFormatType, Map<String, String> formattedValues) {
        this.type = Type.HEADING;
        this.id = id;
        this.label = label;
        this.value = value;
        this.valueFormatType = valueFormatType;
        if (formattedValues != null) {
            addFormattedValues(formattedValues);
        }
    }

}