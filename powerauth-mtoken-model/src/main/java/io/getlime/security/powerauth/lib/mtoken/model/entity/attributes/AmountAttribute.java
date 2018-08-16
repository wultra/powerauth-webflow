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
package io.getlime.security.powerauth.lib.mtoken.model.entity.attributes;

import java.math.BigDecimal;

/**
 * Attribute representing a financial amount item, with attributes for amount
 * and currency, that can be rendered on a mobile application.
 *
 * @author Petr Dvorak, petr@lime-company.eu
 */
public class AmountAttribute extends Attribute {

    private BigDecimal amount;
    private String currency;
    private String amountFormatted;
    private String currencyFormatted;

    /**
     * Default constructor.
     */
    public AmountAttribute() {
        super();
        this.setType(Type.AMOUNT);
    }

    /**
     * Constructor with all details.
     * @param id Attribute ID.
     * @param label Attribute label.
     * @param amount Amount.
     * @param currency Currency.
     * @param amountFormatted Formatted amount.
     * @param currencyFormatted  Formatted currency.
     */
    public AmountAttribute(String id, String label, BigDecimal amount, String currency, String amountFormatted, String currencyFormatted) {
        this();
        this.id = id;
        this.label = label;
        this.amount = amount;
        this.currency = currency;
        this.amountFormatted = amountFormatted;
        this.currencyFormatted = currencyFormatted;
    }

    /**
     * Get amount.
     * @return Amount.
     */
    public BigDecimal getAmount() {
        return amount;
    }

    /**
     * Set amount.
     * @param amount Amount.
     */
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    /**
     * Get currency.
     * @return Currency.
     */
    public String getCurrency() {
        return currency;
    }

    /**
     * Set currency.
     * @param currency Currency.
     */
    public void setCurrency(String currency) {
        this.currency = currency;
    }

    /**
     * Get formatted amount.
     * @return Formatted amount.
     */
    public String getAmountFormatted() {
        return amountFormatted;
    }

    /**
     * Set formatted amount.
     * @param amountFormatted Formatted amount.
     */
    public void setAmountFormatted(String amountFormatted) {
        this.amountFormatted = amountFormatted;
    }

    /**
     * Get formatted currency.
     * @return Formatted currency.
     */
    public String getCurrencyFormatted() {
        return currencyFormatted;
    }

    /**
     * Set formatted currency.
     * @param currencyFormatted Formatted currency.
     */
    public void setCurrencyFormatted(String currencyFormatted) {
        this.currencyFormatted = currencyFormatted;
    }
}
