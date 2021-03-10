/*
 * Copyright 2017 Wultra s.r.o.
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

    private AttributeConverter attributeConverter = new AttributeConverter();

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
