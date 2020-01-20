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
package io.getlime.security.powerauth.lib.dataadapter.model.entity.attribute;


import io.getlime.security.powerauth.lib.dataadapter.model.enumeration.ValueFormatType;

import java.util.HashMap;
import java.util.Map;

/**
 * Class that represents a formatted form field attribute. Formatting is done based on value format type.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class AttributeFormatted extends Attribute {

    protected ValueFormatType valueFormatType;

    protected Map<String, String> formattedValues = new HashMap<>();

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
