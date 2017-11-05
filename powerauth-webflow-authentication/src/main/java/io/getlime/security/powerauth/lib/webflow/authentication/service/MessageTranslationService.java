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
import io.getlime.security.powerauth.lib.nextstep.model.entity.*;
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
     * Translates formData strings.
     *
     * @param formData Form data.
     */
    public void translateFormData(OperationFormData formData) {
        if (formData==null) {
            return;
        }
        // Generate localization strings for payment message
        Map<String, String> valueMap = new HashMap<>();
        for (OperationFormAttribute attribute: formData.getParameters()) {
            switch (attribute.getType()) {
                case AMOUNT:
                    OperationAmountAttribute amountAttribute = (OperationAmountAttribute) attribute;
                    String amount = amountAttribute.getAmount().toPlainString();
                    String currency = amountAttribute.getCurrency();
                    valueMap.put(amountAttribute.getLabel(), amount);
                    valueMap.put(amountAttribute.getCurrencyLabel(), currency);
                    amountAttribute.setLabel(localize(amountAttribute.getLabel()));
                    amountAttribute.setCurrencyLabel(localize(amountAttribute.getCurrencyLabel()));
                    break;
                case KEY_VALUE:
                    OperationKeyValueAttribute keyValueAttribute = (OperationKeyValueAttribute) attribute;
                    valueMap.put(keyValueAttribute.getLabel(), keyValueAttribute.getValue());
                    keyValueAttribute.setLabel(localize(keyValueAttribute.getLabel()));
                    break;
                case MESSAGE:
                    OperationMessageAttribute messageAttribute = (OperationMessageAttribute) attribute;
                    valueMap.put(messageAttribute.getLabel(), messageAttribute.getMessage());
                    messageAttribute.setLabel(localize(messageAttribute.getLabel()));
                    break;
                case BANK_ACCOUNT_CHOICE:
                    OperationBankAccountChoiceAttribute bankAccountChoiceAttribute = (OperationBankAccountChoiceAttribute) attribute;
                    valueMap.put(bankAccountChoiceAttribute.getLabel(), formData.getUserInput().get(CHOSEN_BANK_ACCOUNT_NUMBER_INPUT));
                    bankAccountChoiceAttribute.setLabel(localize(bankAccountChoiceAttribute.getLabel()));
                    break;
            }
        }

        String messageToTranslate = localize(formData.getMessage());
        String translatedMessage = translatedMessage(messageToTranslate, valueMap);
        formData.setMessage(translatedMessage);

        String titleToTranslate = localize(formData.getTitle());
        String translatedTitle = translatedMessage(titleToTranslate, valueMap);
        formData.setTitle(translatedTitle);
    }

    /**
     * Localizes the text by i18n key.
     * @param i18nkey I18n key.
     * @return Localized text.
     */
    private String localize(String i18nkey) {
        final AbstractMessageSource messageSource = i18NService.getMessageSource();
        return messageSource.getMessage(i18nkey, null, LocaleContextHolder.getLocale());
    }

    /**
     * Parses the message and resolves strings using the {variableName} notation.
     * @param message Message to parse.
     * @param valueMap Key-value map for translation.
     * @return Translated message.
     */
    private String translatedMessage(String message, Map<String, String> valueMap) {
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

