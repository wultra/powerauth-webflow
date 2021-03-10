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
package io.getlime.security.powerauth.app.nextstep.converter.dataadapter;

import io.getlime.security.powerauth.lib.dataadapter.model.entity.attribute.*;
import io.getlime.security.powerauth.lib.nextstep.model.entity.attribute.*;

/**
 * Converter for various attribute types.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
public class AttributeConverter {

    private ValueFormatTypeConverter valueFormatTypeConverter = new ValueFormatTypeConverter();
    private BannerTypeConverter bannerTypeConverter = new BannerTypeConverter();
    private BankAccountListConverter bankAccountListConverter = new BankAccountListConverter();
    private PartyInfoConverter partyInfoConverter = new PartyInfoConverter();

    /**
     * Converter from OperationFormFieldAttribute.
     * @param input OperationFormFieldAttribute.
     * @return Attribute.
     */
    public Attribute fromOperationFormFieldAttribute(OperationFormFieldAttribute input) {
        if (input == null) {
            return null;
        }
        switch (input.getType()) {
            case AMOUNT: {
                OperationAmountFieldAttribute attr = (OperationAmountFieldAttribute) input;
                return new AmountAttribute(attr.getId(), attr.getLabel(), attr.getAmount(), attr.getCurrency(), attr.getCurrencyId(), attr.getFormattedValues());
            }
            case KEY_VALUE: {
                OperationKeyValueFieldAttribute attr = (OperationKeyValueFieldAttribute) input;
                return new KeyValueAttribute(attr.getId(), attr.getLabel(), attr.getValue(), valueFormatTypeConverter.fromOperationValueFormatType(attr.getValueFormatType()), attr.getFormattedValues());
            }
            case NOTE: {
                OperationNoteFieldAttribute attr = (OperationNoteFieldAttribute) input;
                return new NoteAttribute(attr.getId(), attr.getLabel(), attr.getNote(), valueFormatTypeConverter.fromOperationValueFormatType(attr.getValueFormatType()), attr.getFormattedValues());
            }
            case BANK_ACCOUNT_CHOICE: {
                OperationBankAccountChoiceFieldAttribute attr = (OperationBankAccountChoiceFieldAttribute) input;
                return new BankAccountChoiceAttribute(attr.getId(), attr.getLabel(), bankAccountListConverter.fromBankAccountDetailList(attr.getBankAccounts()), attr.isEnabled(), attr.getDefaultValue());
            }
            case BANNER: {
                OperationBannerFieldAttribute attr = (OperationBannerFieldAttribute) input;
                return new BannerAttribute(attr.getId(), attr.getLabel(), bannerTypeConverter.fromOperationBannerType(attr.getBannerType()), attr.getMessage());
            }
            case HEADING: {
                OperationHeadingFieldAttribute attr = (OperationHeadingFieldAttribute) input;
                return new HeadingAttribute(attr.getId(), attr.getLabel(), attr.getValue(), valueFormatTypeConverter.fromOperationValueFormatType(attr.getValueFormatType()), attr.getFormattedValues());
            }
            case PARTY_INFO: {
                OperationPartyInfoFieldAttribute attr = (OperationPartyInfoFieldAttribute) input;
                return new PartyInfoAttribute(attr.getId(), partyInfoConverter.fromOperationPartyInfo(attr.getPartyInfo()));
            }
            default: {
                throw new IllegalStateException("Unsupported attribute type: "+input.getType());
            }
        }
    }

    /**
     * Converter from Attribute.
     * @param input Attribute.
     * @return OperationFormFieldAttribute.
     */
    public OperationFormFieldAttribute fromAttribute(Attribute input) {
        if (input == null) {
            return null;
        }
        switch (input.getType()) {
            case AMOUNT: {
                AmountAttribute attr = (AmountAttribute) input;
                return new OperationAmountFieldAttribute(attr.getId(), attr.getLabel(), attr.getAmount(), attr.getCurrency(), attr.getCurrencyId(), attr.getFormattedValues());
            }
            case KEY_VALUE: {
                KeyValueAttribute attr = (KeyValueAttribute) input;
                return new OperationKeyValueFieldAttribute(attr.getId(), attr.getLabel(), attr.getValue(), valueFormatTypeConverter.fromValueFormatType(attr.getValueFormatType()), attr.getFormattedValues());
            }
            case NOTE: {
                NoteAttribute attr = (NoteAttribute) input;
                return new OperationNoteFieldAttribute(attr.getId(), attr.getLabel(), attr.getNote(), valueFormatTypeConverter.fromValueFormatType(attr.getValueFormatType()), attr.getFormattedValues());
            }
            case BANK_ACCOUNT_CHOICE: {
                BankAccountChoiceAttribute attr = (BankAccountChoiceAttribute) input;
                return new OperationBankAccountChoiceFieldAttribute(attr.getId(), attr.getLabel(), bankAccountListConverter.fromBankAccountList(attr.getBankAccounts()), attr.isEnabled(), attr.getDefaultValue());
            }
            case BANNER: {
                BannerAttribute attr = (BannerAttribute) input;
                return new OperationBannerFieldAttribute(attr.getId(), attr.getLabel(), bannerTypeConverter.fromBannerType(attr.getBannerType()), attr.getMessage());
            }
            case HEADING: {
                HeadingAttribute attr = (HeadingAttribute) input;
                return new OperationHeadingFieldAttribute(attr.getId(), attr.getLabel(), attr.getValue(), valueFormatTypeConverter.fromValueFormatType(attr.getValueFormatType()), attr.getFormattedValues());
            }
            case PARTY_INFO: {
                PartyInfoAttribute attr = (PartyInfoAttribute) input;
                return new OperationPartyInfoFieldAttribute(attr.getId(), partyInfoConverter.fromPartyInfo(attr.getPartyInfo()));
            }
            default: {
                throw new IllegalStateException("Unsupported attribute type: "+input.getType());
            }
        }
    }

    /**
     * Converter from OperationFormMessageAttribute.
     * @param input OperationFormMessageAttribute.
     * @return MessageAttribute.
     */
    public MessageAttribute fromOperationFormMessageAttribute(OperationFormMessageAttribute input) {
        return new MessageAttribute(input.getId(), input.getMessage());
    }

    /**
     * Converter from MessageAttribute.
     * @param input MessageAttribute.
     * @return OperationFormMessageAttribute.
     */
    public OperationFormMessageAttribute fromMessageAttribute(MessageAttribute input) {
        return new OperationFormMessageAttribute(input.getId(), input.getMessage());
    }

    /**
     * Converter from OperationFormFieldConfig.
     * @param config OperationFormFieldConfig.
     * @return FormFieldConfig.
     */
    public FormFieldConfig fromOperationFormFieldConfig(OperationFormFieldConfig config) {
        return new FormFieldConfig(config.getId(), config.isEnabled(), config.getDefaultValue());
    }

    /**
     * Converter from FormFieldConfig.
     * @param config FormFieldConfig.
     * @return OperationFormFieldConfig.
     */
    public OperationFormFieldConfig fromFormFieldConfig(FormFieldConfig config) {
        return new OperationFormFieldConfig(config.getId(), config.isEnabled(), config.getDefaultValue());
    }

    /**
     * Converter from OperationFormBanner.
     * @param banner OperationFormBanner.
     * @return FormBanner.
     */
    public FormBanner fromOperationFormBanner(OperationFormBanner banner) {
        return new FormBanner(banner.getId(), bannerTypeConverter.fromOperationBannerType(banner.getBannerType()), banner.getMessage());
    }

    /**
     * Converter from FormBanner.
     * @param banner FormBanner.
     * @return OperationFormBanner.
     */
    public OperationFormBanner fromFormBanner(FormBanner banner) {
        return new OperationFormBanner(banner.getId(), bannerTypeConverter.fromBannerType(banner.getBannerType()), banner.getMessage());
    }
}
