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
package io.getlime.security.powerauth.lib.webflow.authentication.service;

import io.getlime.security.powerauth.app.webflow.i18n.I18NService;
import io.getlime.security.powerauth.lib.nextstep.model.entity.OperationFormData;
import io.getlime.security.powerauth.lib.nextstep.model.entity.attribute.*;
import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.ValueFormatType;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.AbstractMessageSource;
import org.springframework.stereotype.Service;

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service which localizes and translates form data messages.
 *
 * @author Roman Strobl, roman.strobl@lime-company.eu
 */
@Service
public class MessageTranslationService {

    private final I18NService i18NService;
    private final ValueFormatterService valueFormatterService;

    private static final String CHOSEN_BANK_ACCOUNT_NUMBER_INPUT = "operation.bankAccountChoice";
    private static final String MISSING_KEY_MESSAGE = "MISSING_LOCALIZATION_FOR_FIELD";
    private static final String MISSING_VALUE_MESSAGE = "MISSING_VALUE_FOR_FIELD";

    /**
     * Service constructor
     * @param i18NService I18N service.
     * @param valueFormatterService Value formatter service.
     */
    public MessageTranslationService(I18NService i18NService, ValueFormatterService valueFormatterService) {
        this.i18NService = i18NService;
        this.valueFormatterService = valueFormatterService;
    }

    /**
     * Translate form data strings.
     *
     * @param formData Form data.
     */
    public void translateFormData(OperationFormData formData) {
        if (formData==null) {
            return;
        }

        // Localize labels for form fields
        localizeFormFieldLabels(formData);

        // Create id -> value map for translation using the {id} notation
        Map<String, String> idValueMap = createIdValueMap(formData);

        // Localize and translate title using the {id} notation
        OperationFormMessageAttribute title = formData.getTitle();
        localizeAndTranslateFormAttributeValue(title, idValueMap);

        // Localize and translate greeting using the {id} notation
        OperationFormMessageAttribute greeting = formData.getGreeting();
        localizeAndTranslateFormAttributeValue(greeting, idValueMap);

        // Localize and translate summary using the {id} notation
        OperationFormMessageAttribute summary = formData.getSummary();
        localizeAndTranslateFormAttributeValue(summary, idValueMap);

        // Localize and translate banners using the {id} notation
        for (OperationFormBanner banner: formData.getBanners()) {
            localizeAndTranslateFormAttributeValue(banner, idValueMap);
        }

        // Format form field attributes
        formatFormFieldAttributes(formData.getParameters());
    }

    /**
     * Localize value of operation form attribute.
     * @param attribute Operation form attribute.
     * @param idValueMap Id -> value map for translation using the {id} notation.
     */
    private void localizeAndTranslateFormAttributeValue(OperationFormMessageAttribute attribute, Map<String, String> idValueMap) {
        String message = localize(attribute.getId());
        String translatedMessage = translateMessage(message, idValueMap);
        attribute.setMessage(translatedMessage);
    }

    /**
     * Format form field attributes.
     * @param attributes Form field attributes
     */
    private void formatFormFieldAttributes(List<OperationFormFieldAttribute> attributes) {
        if (attributes == null) {
            return;
        }
        for (OperationFormFieldAttribute attribute : attributes) {
            // Formatting of attributes with specified format
            if (attribute instanceof OperationFormFieldAttributeFormatted) {
                OperationFormFieldAttributeFormatted formattedAttribute = (OperationFormFieldAttributeFormatted) attribute;
                ValueFormatType valueFormatType = formattedAttribute.getValueFormatType();
                if (valueFormatType == ValueFormatType.LOCALIZED_TEXT) {
                    String formattedValue = localize(valueFormatterService.getValue(attribute));
                    formattedAttribute.addFormattedValue("value", formattedValue);
                } else {
                    valueFormatterService.addFormattedValue(formattedAttribute, LocaleContextHolder.getLocale());
                }
                continue;
            }
            // Localization of banners
            if (attribute.getType() == OperationFormFieldAttribute.Type.BANNER) {
                OperationBannerFieldAttribute banner = (OperationBannerFieldAttribute) attribute;
                String localizedMessage = localize(banner.getId());
                banner.setMessage(localizedMessage);
            }
        }
    }

    /**
     * Localize the form field labels.
     * @param formData Operation form data.
     */
    private void localizeFormFieldLabels(OperationFormData formData) {
        for (OperationFormFieldAttribute attribute: formData.getParameters()) {
            // Banners do not have labels, skip localization
            if (attribute.getType() == OperationFormFieldAttribute.Type.BANNER) {
                continue;
            }
            // Headings do not have labels, skip localization
            if (attribute.getType() == OperationFormFieldAttribute.Type.HEADING) {
                continue;
            }
            String localizedValue = localize(attribute.getId());
            attribute.setLabel(localizedValue);
        }
    }

    /**
     * Create ID-Value map for operation form fields.
     * @param formData Form data
     * @return ID-Value map.
     */
    private Map<String, String> createIdValueMap(OperationFormData formData) {
        Map<String, String> idValueMap = new HashMap<>();
        for (OperationFormFieldAttribute attribute: formData.getParameters()) {
            String value = null;
            switch (attribute.getType()) {
                case AMOUNT:
                    OperationAmountFieldAttribute amountAttribute = (OperationAmountFieldAttribute) attribute;
                    value = amountAttribute.getAmount().toPlainString();
                    // special handling for translation of currency value
                    idValueMap.put(amountAttribute.getCurrencyId(), amountAttribute.getCurrency());
                    break;
                case NOTE:
                    OperationNoteFieldAttribute messageAttribute = (OperationNoteFieldAttribute) attribute;
                    value = messageAttribute.getNote();
                    break;
                case BANK_ACCOUNT_CHOICE:
                    value = formData.getUserInput().get(CHOSEN_BANK_ACCOUNT_NUMBER_INPUT);
                    break;
                case KEY_VALUE:
                    OperationKeyValueFieldAttribute keyValueAttribute = (OperationKeyValueFieldAttribute) attribute;
                    value = keyValueAttribute.getValue();
                    break;
                case HEADING:
                    OperationHeadingFieldAttribute headingAttribute = (OperationHeadingFieldAttribute) attribute;
                    value = headingAttribute.getValue();
                    break;
                case PARTY_INFO:
                    OperationPartyInfoFieldAttribute partyInfoAttribute = (OperationPartyInfoFieldAttribute) attribute;
                    if (partyInfoAttribute.getPartyInfo() != null) {
                        value = partyInfoAttribute.getPartyInfo().getName();
                    }
                    break;
            }
            idValueMap.put(attribute.getId(), value);
        }
        return idValueMap;
    }

    /**
     * Localize the text by i18n key.
     * @param i18nKey I18n key.
     * @return Localized text.
     */
    private String localize(String i18nKey) {
        if (i18nKey == null) {
            throw new IllegalArgumentException("Missing i18n key");
        }
        final AbstractMessageSource messageSource = i18NService.getMessageSource();
        try {
            return messageSource.getMessage(i18nKey, null, LocaleContextHolder.getLocale());
        } catch (NoSuchMessageException ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.FINE, "Localization key is missing: "+i18nKey);
            return MISSING_KEY_MESSAGE+": "+i18nKey;
        }
    }

    /**
     * Parse the message and resolve strings using the {id} notation.
     * @param message Message to parse.
     * @param valueMap Key-value map for translation.
     * @return Translated message.
     */
    private String translateMessage(String message, Map<String, String> valueMap) {
        if (message == null) {
            return null;
        }
        CharacterIterator iterator = new StringCharacterIterator(message);
        StringBuilder messageBuilder = new StringBuilder();
        StringBuilder keyBuilder = new StringBuilder();
        boolean betweenBrackets = false;
        char c = iterator.first();
        while (c != CharacterIterator.DONE) {
            if (c == '{') {
                betweenBrackets = true;
                keyBuilder = new StringBuilder();
            } else if (c == '}') {
                String key = keyBuilder.toString();
                String value = valueMap.get(key);
                if (value == null) {
                    value = MISSING_VALUE_MESSAGE+": "+key;
                }
                messageBuilder.append(value);
                betweenBrackets = false;
            } else {
                if (betweenBrackets) {
                    keyBuilder.append(c);
                } else {
                    messageBuilder.append(c);
                }
            }
            c = iterator.next();
        }
        return messageBuilder.toString();
    }
}

