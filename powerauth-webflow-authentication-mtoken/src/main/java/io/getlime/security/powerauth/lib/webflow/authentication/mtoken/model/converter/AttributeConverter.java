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

import io.getlime.security.powerauth.lib.mtoken.model.entity.PartyInfo;
import io.getlime.security.powerauth.lib.mtoken.model.entity.attributes.*;
import io.getlime.security.powerauth.lib.nextstep.model.entity.attribute.*;

/**
 * Converter for various attribute types.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
public class AttributeConverter {

    /**
     * Convert form field attribute.
     * @param input Form field attribute.
     * @return Attribute.
     */
    public Attribute fromOperationFormFieldAttribute(OperationFormFieldAttribute input) {
        if (input == null) {
            return null;
        }
        switch (input.getType()) {
            case AMOUNT: {
                OperationAmountFieldAttribute attr = (OperationAmountFieldAttribute) input;
                return new AmountAttribute(attr.getId(), attr.getLabel(), attr.getAmount(), attr.getCurrency(),
                        attr.getFormattedValues().get("amount"), attr.getFormattedValues().get("currency"));
            }
            case KEY_VALUE: {
                OperationKeyValueFieldAttribute attr = (OperationKeyValueFieldAttribute) input;
                return new KeyValueAttribute(attr.getId(), attr.getLabel(), attr.getFormattedValues().get("value"));
            }
            case NOTE: {
                OperationNoteFieldAttribute attr = (OperationNoteFieldAttribute) input;
                return new NoteAttribute(attr.getId(), attr.getLabel(), attr.getFormattedValues().get("value"));
            }
            case HEADING: {
                OperationHeadingFieldAttribute attr = (OperationHeadingFieldAttribute) input;
                return new HeadingAttribute(attr.getId(), attr.getFormattedValues().get("value"));
            }
            case PARTY_INFO: {
                OperationPartyInfoFieldAttribute attr = (OperationPartyInfoFieldAttribute) input;
                return new PartyAttribute(attr.getId(), attr.getLabel(), fromPartyInfo(attr.getPartyInfo()));
            }
            default: {
                return new KeyValueAttribute(input.getId(), input.getLabel(), null);
            }
        }
    }

    /**
     * Convert PartyInfo from Next Step model to mToken model.
     * @param input Input PartyInfo.
     * @return Converted PartyInfo.
     */
    private PartyInfo fromPartyInfo(io.getlime.security.powerauth.lib.nextstep.model.entity.PartyInfo input) {
        if (input == null) {
            return null;
        }
        PartyInfo partyInfo = new PartyInfo();
        partyInfo.setLogoUrl(input.getLogoUrl());
        partyInfo.setName(input.getName());
        partyInfo.setDescription(input.getDescription());
        partyInfo.setWebsiteUrl(input.getWebsiteUrl());
        return partyInfo;
    }

}
