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
package io.getlime.security.powerauth.lib.webflow.authentication.mtoken.model.converter;

import io.getlime.security.powerauth.lib.mtoken.model.entity.FormData;
import io.getlime.security.powerauth.lib.mtoken.model.entity.attributes.Attribute;
import io.getlime.security.powerauth.lib.nextstep.model.entity.OperationFormData;
import io.getlime.security.powerauth.lib.nextstep.model.entity.attribute.OperationFormFieldAttribute;

import java.util.List;

/**
 * Converter for the form data objects used for mobile API.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
public class FormDataConverter {

    private final AttributeConverter attributeConverter = new AttributeConverter();

    /**
     * Convert operation form data.
     * @param input Operation form data.
     * @return Form data.
     */
    public FormData fromOperationFormData(OperationFormData input) {
        if (input == null) {
            return null;
        }
        FormData result = new FormData();
        result.setTitle(input.getTitle().getMessage());
        result.setMessage(input.getGreeting().getMessage());
        List<Attribute> attributes = result.getAttributes();
        for (OperationFormFieldAttribute attribute : input.getParameters()) {
            Integer existingIndex = null;
            int counter = 0;
            for (Attribute attr: attributes) {
                // Make sure attribute with already existing ID is present only once
                if (attr.getId().equals(attribute.getId())) {
                    existingIndex = counter;
                    break;
                }
                counter++;
            }
            Attribute attributeToSave = attributeConverter.fromOperationFormFieldAttribute(attribute);
            if (existingIndex != null) {
                attributes.set(existingIndex, attributeToSave);
            } else {
                attributes.add(attributeToSave);
            }
        }
        return result;
    }

}
