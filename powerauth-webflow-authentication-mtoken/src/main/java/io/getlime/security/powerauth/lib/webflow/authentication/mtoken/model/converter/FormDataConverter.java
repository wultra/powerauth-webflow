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
package io.getlime.security.powerauth.lib.webflow.authentication.mtoken.model.converter;

import io.getlime.security.powerauth.lib.mtoken.model.entity.FormData;
import io.getlime.security.powerauth.lib.nextstep.model.entity.OperationFormData;
import io.getlime.security.powerauth.lib.nextstep.model.entity.attribute.OperationFormFieldAttribute;

/**
 * Converter for the form data objects used for mobile API.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
public class FormDataConverter {

    private AttributeConverter attributeConverter = new AttributeConverter();

    public FormData fromOperationFormData(OperationFormData input) {
        if (input == null) {
            return null;
        }
        FormData result = new FormData();
        result.setTitle(input.getTitle().getMessage());
        result.setMessage(input.getGreeting().getMessage());
        for (OperationFormFieldAttribute attribute : input.getParameters()) {
            result.getAttributes().add(attributeConverter.fromOperationFormFieldAttribute(attribute));
        }
        return result;
    }

}
