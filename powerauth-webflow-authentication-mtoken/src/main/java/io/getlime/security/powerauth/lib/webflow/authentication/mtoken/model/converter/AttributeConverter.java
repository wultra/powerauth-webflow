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
            case AMOUNT -> {
                OperationAmountFieldAttribute attr = (OperationAmountFieldAttribute) input;
                return new AmountAttribute(attr.getId(), attr.getLabel(), attr.getAmount(), attr.getCurrency(),
                        attr.getFormattedValues().get("amount"), attr.getFormattedValues().get("currency"));
            }
            case KEY_VALUE -> {
                OperationKeyValueFieldAttribute attr = (OperationKeyValueFieldAttribute) input;
                return new KeyValueAttribute(attr.getId(), attr.getLabel(), attr.getFormattedValues().get("value"));
            }
            case NOTE -> {
                OperationNoteFieldAttribute attr = (OperationNoteFieldAttribute) input;
                return new NoteAttribute(attr.getId(), attr.getLabel(), attr.getFormattedValues().get("value"));
            }
            case HEADING -> {
                OperationHeadingFieldAttribute attr = (OperationHeadingFieldAttribute) input;
                return new HeadingAttribute(attr.getId(), attr.getFormattedValues().get("value"));
            }
            case PARTY_INFO -> {
                OperationPartyInfoFieldAttribute attr = (OperationPartyInfoFieldAttribute) input;
                return new PartyAttribute(attr.getId(), attr.getLabel(), fromPartyInfo(attr.getPartyInfo()));
            }
            default -> {
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
