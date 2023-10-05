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
package io.getlime.security.powerauth.lib.dataadapter.model.converter;

import io.getlime.security.powerauth.lib.dataadapter.model.entity.FormData;
import io.getlime.security.powerauth.lib.dataadapter.model.entity.attribute.Attribute;
import io.getlime.security.powerauth.lib.dataadapter.model.entity.attribute.FormBanner;
import io.getlime.security.powerauth.lib.dataadapter.model.entity.attribute.FormFieldConfig;
import io.getlime.security.powerauth.lib.nextstep.model.entity.OperationFormData;
import io.getlime.security.powerauth.lib.nextstep.model.entity.attribute.OperationFormBanner;
import io.getlime.security.powerauth.lib.nextstep.model.entity.attribute.OperationFormFieldAttribute;
import io.getlime.security.powerauth.lib.nextstep.model.entity.attribute.OperationFormFieldConfig;

/**
 * Converter for the form data objects used for mobile API.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
public class FormDataConverter {

    private final AttributeConverter attributeConverter = new AttributeConverter();

    /**
     * Converter from OperationFormData.
     * @param input OperationFormData.
     * @return FormData.
     */
    public FormData fromOperationFormData(OperationFormData input) {
        if (input == null) {
            return null;
        }
        FormData result = new FormData();
        result.setTitle(attributeConverter.fromOperationFormMessageAttribute(input.getTitle()));
        result.setGreeting(attributeConverter.fromOperationFormMessageAttribute(input.getGreeting()));
        result.setSummary(attributeConverter.fromOperationFormMessageAttribute(input.getSummary()));
        for (OperationFormFieldConfig config : input.getConfig()) {
            result.getConfig().add(attributeConverter.fromOperationFormFieldConfig(config));
        }
        for (OperationFormFieldAttribute attribute : input.getParameters()) {
            result.getParameters().add(attributeConverter.fromOperationFormFieldAttribute(attribute));
        }
        for (OperationFormBanner banner : input.getBanners()) {
            result.getBanners().add(attributeConverter.fromOperationFormBanner(banner));
        }
        for (String key : input.getUserInput().keySet()) {
            result.getUserInput().put(key, input.getUserInput().get(key));
        }
        return result;
    }

    /**
     * Converter from FormData.
     * @param input FormData.
     * @return OperationFormData.
     */
    public OperationFormData fromFormData(FormData input) {
        if (input == null) {
            return null;
        }
        OperationFormData result = new OperationFormData();
        result.setTitle(attributeConverter.fromMessageAttribute(input.getTitle()));
        result.setGreeting(attributeConverter.fromMessageAttribute(input.getGreeting()));
        result.setSummary(attributeConverter.fromMessageAttribute(input.getSummary()));
        for (FormFieldConfig config : input.getConfig()) {
            result.getConfig().add(attributeConverter.fromFormFieldConfig(config));
        }
        for (Attribute attribute : input.getParameters()) {
            result.getParameters().add(attributeConverter.fromAttribute(attribute));
        }
        for (FormBanner banner : input.getBanners()) {
            result.getBanners().add(attributeConverter.fromFormBanner(banner));
        }
        for (String key : input.getUserInput().keySet()) {
            result.getUserInput().put(key, input.getUserInput().get(key));
        }
        return result;
    }

}
