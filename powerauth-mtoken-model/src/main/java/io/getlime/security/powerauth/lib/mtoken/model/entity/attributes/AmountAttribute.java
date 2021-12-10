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
package io.getlime.security.powerauth.lib.mtoken.model.entity.attributes;

import java.math.BigDecimal;

/**
 * Attribute representing a financial amount item, with attributes for amount
 * and currency, that can be rendered on a mobile application.
 *
 * @author Petr Dvorak, petr@wultra.com
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
