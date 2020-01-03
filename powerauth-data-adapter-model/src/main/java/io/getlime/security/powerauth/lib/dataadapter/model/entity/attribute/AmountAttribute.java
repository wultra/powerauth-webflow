/*
 * Copyright 2018 Wultra s.r.o.
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
package io.getlime.security.powerauth.lib.dataadapter.model.entity.attribute;

import io.getlime.security.powerauth.lib.dataadapter.model.enumeration.ValueFormatType;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Class representing an operation display attribute for transaction amount and currency.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
public class AmountAttribute extends AttributeFormatted {

    private BigDecimal amount;
    private String currency;
    private String currencyId;

    /**
     * Default constructor.
     */
    public AmountAttribute() {
        this.type = Type.AMOUNT;
        this.valueFormatType = ValueFormatType.AMOUNT;
    }

    /**
     * Constructor with all details.
     * @param id Attribute ID.
     * @param label Label.
     * @param amount Amount.
     * @param currency Currency.
     * @param currencyId Currency localization ID.
     * @param formattedValues Formatted values.
     */
    public AmountAttribute(String id, String label, BigDecimal amount, String currency, String currencyId, Map<String, String> formattedValues) {
        this.type = Type.AMOUNT;
        this.valueFormatType = ValueFormatType.AMOUNT;
        this.id = id;
        this.label = label;
        this.amount = amount;
        this.currency = currency;
        this.currencyId = currencyId;
        if (formattedValues != null) {
            addFormattedValues(formattedValues);
        }
    }

    /**
     * Get transaction amount.
     * @return Transaction amount.
     */
    public BigDecimal getAmount() {
        return amount;
    }

    /**
     * Set transaction amount.
     * @param amount Transaction amount.
     */
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    /**
     * Get transaction currency.
     * @return Transaction currency.
     */
    public String getCurrency() {
        return currency;
    }

    /**
     * Set transaction currency.
     * @param currency Transaction currency.
     */
    public void setCurrency(String currency) {
        this.currency = currency;
    }

    /**
     * Get currency localization ID.
     * @return Currency localization ID.
     */
    public String getCurrencyId() {
        return currencyId;
    }

    /**
     * Set currency localization ID.
     * @param currencyId Currency localization ID.
     */
    public void setCurrencyId(String currencyId) {
        this.currencyId = currencyId;
    }

}