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
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.AbstractMessageSource;
import org.springframework.stereotype.Service;

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.HashMap;
import java.util.Map;

/**
 * Service which translates formData strings.
 *
 * @author Roman Strobl, roman.strobl@lime-company.eu
 */
@Service
public class MessageTranslationService {

    private final I18NService i18NService;
    private static final String CHOSEN_BANK_ACCOUNT_NUMBER_INPUT = "chosenBankAccountNumber";

    public MessageTranslationService(I18NService i18NService) {
        this.i18NService = i18NService;
    }

    /**
     * Translate formData strings.
     *
     * @param formData Form data.
     */
    public void translateFormData(OperationFormData formData) {
        if (formData==null) {
            return;
        }
        OperationTitleAttribute title = null;
        OperationMessageAttribute message = null;
        Map<String, String> valueMap = new HashMap<>();
        // Set localized labels and find title and message attributes
        for (OperationFormAttribute attribute: formData.getParameters()) {
            String id = null;
            String value = null;
            switch (attribute.getType()) {
                case TITLE:
                    OperationTitleAttribute titleAttribute = (OperationTitleAttribute) attribute;
                    id = titleAttribute.getId();
                    value = titleAttribute.getTitle();
                    break;
                case MAIN_MESSAGE:
                    OperationMessageAttribute noteAttribute = (OperationMessageAttribute) attribute;
                    id = noteAttribute.getId();
                    value = noteAttribute.getMessage();
                    break;
                case AMOUNT:
                    OperationAmountAttribute amountAttribute = (OperationAmountAttribute) attribute;
                    id = amountAttribute.getId();
                    value = amountAttribute.getAmount().toPlainString();
                    // special handling for translation of currency value
                    valueMap.put(amountAttribute.getCurrencyId(), amountAttribute.getCurrency());
                    break;
                case MESSAGE:
                    OperationNoteAttribute messageAttribute = (OperationNoteAttribute) attribute;
                    id = messageAttribute.getId();
                    value = messageAttribute.getMessage();
                    break;
                case BANK_ACCOUNT_CHOICE:
                    OperationBankAccountChoiceAttribute bankAccountChoiceAttribute = (OperationBankAccountChoiceAttribute) attribute;
                    id = bankAccountChoiceAttribute.getId();
                    value = formData.getUserInput().get(CHOSEN_BANK_ACCOUNT_NUMBER_INPUT);
                    break;
                case KEY_VALUE:
                    OperationKeyValueAttribute keyValueAttribute = (OperationKeyValueAttribute) attribute;
                    id = keyValueAttribute.getId();
                    value = keyValueAttribute.getValue();
                    break;
            }
            if (id != null) {
                valueMap.put(id, value);
                String localizedValue = localize(id);
                attribute.setLabel(localizedValue);
            }
            switch (attribute.getType()) {
                case TITLE:
                    title = (OperationTitleAttribute) attribute;
                    break;
                case MAIN_MESSAGE:
                    message = (OperationMessageAttribute) attribute;
                    break;
            }
        }

        // Attributes title and message support substitution using the {id} notation
        if (title != null && title.getLabel() != null) {
            String translatedTitle = translateMessage(title.getLabel(), valueMap);
            formData.setTitle(title.getId(), translatedTitle);
        }

        if (message != null && message.getLabel() != null) {
            String translatedMessage = translateMessage(message.getLabel(), valueMap);
            formData.setMessage(message.getId(), translatedMessage);
        }
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
        return messageSource.getMessage(i18nKey, null, LocaleContextHolder.getLocale());
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
                messageBuilder.append(valueMap.get(key));
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

