/*
 * Copyright 2018 Lime - HighTech Solutions s.r.o.
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
import io.getlime.security.powerauth.lib.nextstep.model.entity.attribute.*;
import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.ValueFormatType;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.support.AbstractMessageSource;
import org.springframework.stereotype.Service;

import javax.money.CurrencyUnit;
import javax.money.Monetary;
import javax.money.MonetaryAmount;
import javax.money.UnknownCurrencyException;
import javax.money.format.AmountFormatQueryBuilder;
import javax.money.format.MonetaryAmountFormat;
import javax.money.format.MonetaryFormats;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;

/**
 * Service used for formatting form field attributes.
 *
 * @author Roman Strobl, roman.strobl@lime-company.eu
 */
@Service
public class ValueFormatterService {

    private final I18NService i18NService;

    /**
     * Constructor.
     * @param i18NService I18N service.
     */
    public ValueFormatterService(I18NService i18NService) {
        this.i18NService = i18NService;
    }

    /**
     * Add formatted value(s) based on value format type.
     * @param attribute Form field attribute.
     * @param locale Locale used for formatting.
     */
    public void addFormattedValue(OperationFormFieldAttributeFormatted attribute, Locale locale) {
        if (attribute == null) {
            return;
        }

        ValueFormatType valueFormatType = attribute.getValueFormatType();

        switch (valueFormatType) {

            case TEXT: {
                attribute.addFormattedValue("value", getValue(attribute));
                return;
            }

            case AMOUNT: {
                if (attribute instanceof OperationAmountFieldAttribute) {
                    OperationAmountFieldAttribute amountAttribute = (OperationAmountFieldAttribute) attribute;
                    addFormattedAmount(amountAttribute, locale);
                    return;
                }
                addNumericValue(attribute, locale);
                return;
            }

            case NUMBER: {
                addNumericValue(attribute, locale);
                return;
            }

            case DATE:
                attribute.addFormattedValue("value", formatDate(getValue(attribute), locale));
                return;

            case ACCOUNT:
                attribute.addFormattedValue("value", formatAccount(getValue(attribute), locale));
                return;

            default:
                throw new IllegalStateException("Unexpected value format type: "+attribute.getValueFormatType());

        }
    }

    /**
     * Add a formatted numeric value to attribute.
     * @param attribute Form field attribute.
     * @param locale Locale used for formatting.
     */
    private void addNumericValue(OperationFormFieldAttributeFormatted attribute, Locale locale) {
        String value = getValue(attribute);
        try {
            BigDecimal numericValue = new BigDecimal(value);
            attribute.addFormattedValue("value", formatNumber(numericValue, locale));
            return;
        } catch (NumberFormatException ex) {
            // do not interrupt operation by exception due to broken formatting
            attribute.addFormattedValue("value", value);
            return;
        }
    }

    /**
     * Get attribute value as String (without formatting).
     * @param attribute Operation form field attribute.
     * @return Attribute value as String.
     */
    public String getValue(OperationFormFieldAttribute attribute) {
        if (attribute == null) {
            return "";
        }
        if (attribute instanceof OperationKeyValueFieldAttribute) {
            OperationKeyValueFieldAttribute keyValueAttribute = (OperationKeyValueFieldAttribute) attribute;
            return keyValueAttribute.getValue();
        } else if (attribute instanceof OperationNoteFieldAttribute) {
            OperationNoteFieldAttribute noteAttribute = (OperationNoteFieldAttribute) attribute;
            return noteAttribute.getNote();
        } else if (attribute instanceof OperationAmountFieldAttribute) {
            OperationAmountFieldAttribute amountAttribute = (OperationAmountFieldAttribute) attribute;
            return amountAttribute.getAmount().toPlainString() + " " + amountAttribute.getCurrency();
        }
        throw new IllegalArgumentException("Attribute does not support formatting: "+attribute.getClass().getName());
    }

    /**
     * Formats the amount for given locale using configured pattern and appends localized currency name.
     * @param amountAttribute Amount form field attribute.
     * @param locale Used locale.
     */
    private void addFormattedAmount(OperationAmountFieldAttribute amountAttribute, Locale locale) {
        if (amountAttribute.getAmount() == null) {
            return;
        }
        if (amountAttribute.getCurrency() == null) {
            amountAttribute.addFormattedValue("amount", formatNumber(amountAttribute.getAmount(), locale));
            // set empty currency to avoid displaying null value
            amountAttribute.addFormattedValue("currency", "");
            return;
        }
        final CurrencyUnit currency;
        try {
            currency = Monetary.getCurrency(amountAttribute.getCurrency());
        } catch (UnknownCurrencyException ex) {
            // ignore errors for unsupported currencies, perform only basic formatting
            amountAttribute.addFormattedValue("amount", formatNumber(amountAttribute.getAmount(), locale));
            amountAttribute.addFormattedValue("currency", amountAttribute.getCurrency());
            return;
        }
        final MonetaryAmount amount = Monetary.getDefaultAmountFactory().setCurrency(currency).setNumber(amountAttribute.getAmount()).create();
        final AbstractMessageSource messageSource = i18NService.getMessageSource();
        String pattern = "###0.00";
        try {
            pattern = messageSource.getMessage("currency.pattern", null, locale);
        } catch (NoSuchMessageException ex) {
            // pattern is not specified - use default pattern
        }
        final MonetaryAmountFormat format;
        String localizedCurrencyName = amountAttribute.getCurrency();
        try {
            localizedCurrencyName = messageSource.getMessage("currency." + amountAttribute.getCurrency() + ".name", null, locale);
        } catch (NoSuchMessageException ex) {
            // currency is not localized - display it as it was sent in the operation
        }
        format = MonetaryFormats.getAmountFormat(
                AmountFormatQueryBuilder.of(locale)
                        .set("pattern", pattern)
                        .build());
        // append localized currency name
        amountAttribute.addFormattedValue("amount", format.format(amount));
        amountAttribute.addFormattedValue("currency", localizedCurrencyName);
    }

    /**
     * Formats the account number for given locale.
     * @param account Account number.
     * @param locale Used locale.
     * @return Formatted account number.
     */
    private String formatAccount(String account, Locale locale) {
        // account formatting is not implemented because account formats differ widely per locale/area, formatting should be handled before sending data
        return account;
    }

    /**
     * Formats the number for given locale.
     * @param number Number.
     * @param locale Used locale.
     * @return Formatted number.
     */
    private String formatNumber(BigDecimal number, Locale locale) {
        if (number==null) {
            return "";
        }
        NumberFormat numberFormat = NumberFormat.getNumberInstance(locale);
        return numberFormat.format(number.doubleValue());
    }

    /**
     * Formats the date for given locale. Expected date syntax is YYYY-MM-DD.
     * @param date Date.
     * @param locale Used locale.
     * @return Formatted date.
     */
    private String formatDate(String date, Locale locale) {
        if (date == null) {
            return "";
        }
        if (date.matches("[0-9]{4}-[0-9]{2}-[0-9]{2}")) {
            // supported date format for localization
            int year = Integer.parseInt(date.substring(0, 4));
            int month = Integer.parseInt(date.substring(5, 7));
            int day = Integer.parseInt(date.substring(8, 10));
            LocalDate localDate = LocalDate.of(year, month, day);
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale);
            return localDate.format(dateFormatter);
        }
        // in case format is not supported, keep date as is, this is not an error
        return date;
    }

}
