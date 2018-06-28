/*
 * Copyright 2017 Lime - HighTech Solutions s.r.o.
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

import io.getlime.security.powerauth.lib.mtoken.model.entity.attributes.*;
import io.getlime.security.powerauth.lib.nextstep.model.entity.attribute.*;

/**
 * Converter for various attribute types.
 *
 * @author Petr Dvorak, petr@lime-company.eu
 */
public class AttributeConverter {

    public Attribute fromOperationFormFieldAttribute(OperationFormFieldAttribute input) {
        if (input == null) {
            return null;
        }
        switch (input.getType()) {
            case AMOUNT: {
                OperationAmountFieldAttribute attr = (OperationAmountFieldAttribute) input;
                return new AmountAttribute(attr.getId(), attr.getLabel(), attr.getAmount(), attr.getCurrency());
            }
            case KEY_VALUE: {
                OperationKeyValueFieldAttribute attr = (OperationKeyValueFieldAttribute) input;
                return new KeyValueAttribute(attr.getId(), attr.getLabel(), attr.getFormattedValue());
            }
            case NOTE: {
                OperationNoteFieldAttribute attr = (OperationNoteFieldAttribute) input;
                return new NoteAttribute(attr.getId(), attr.getLabel(), attr.getFormattedValue());
            }
            case HEADING: {
                OperationHeadingFieldAttribute attr = (OperationHeadingFieldAttribute) input;
                return new HeadingAttribute(attr.getId(), attr.getFormattedValue());
            }
            case PARTY_INFO: {
                OperationPartyInfoFieldAttribute attr = (OperationPartyInfoFieldAttribute) input;
                // TODO - we use KeyValueAttribute until mobile token supports PartyInfo
                if (attr.getPartyInfo() != null) {
                    return new KeyValueAttribute(attr.getId(), attr.getLabel(), attr.getPartyInfo().getName());
                }
                return new KeyValueAttribute(input.getId(), input.getLabel(), null);
            }
            default: {
                return new KeyValueAttribute(input.getId(), input.getLabel(), null);
            }
        }
    }

}
