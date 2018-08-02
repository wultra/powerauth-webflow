/*
 * Copyright 2018 Lime - HighTech Solutions s.r.o.
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

import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.ValueFormatType;

/**
 * Class that represents a formatted form field attribute. Formatting is done based on value format type.
 *
 * @author Roman Strobl, roman.strobl@lime-company.eu
 */
public class OperationFormFieldAttributeFormatted extends OperationFormFieldAttribute {

    protected ValueFormatType valueFormatType;

    protected String formattedValue;

    /**
     * Get value format type of this attribute.
     * @return Value format type.
     */
    public ValueFormatType getValueFormatType() {
        return valueFormatType;
    }

    /**
     * Get formatted value of this attribute.
     * @return Formatted value.
     */
    public String getFormattedValue() {
        return formattedValue;
    }

    /**
     * Set formatted value of this attribute.
     * @param formattedValue Formatted value.
     */
    public void setFormattedValue(String formattedValue) {
        this.formattedValue = formattedValue;
    }

}
