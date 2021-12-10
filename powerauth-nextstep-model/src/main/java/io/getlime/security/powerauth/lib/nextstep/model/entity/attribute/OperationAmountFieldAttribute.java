/*
 * PowerAuth Web Flow and related software components
 * Copyright (C) 2018 Wultra s.r.o.
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
package io.getlime.security.powerauth.lib.nextstep.model.entity.attribute;

import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.ValueFormatType;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Class representing an operation display attribute for transaction amount and currency.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
public class OperationAmountFieldAttribute extends OperationFormFieldAttributeFormatted {

    private BigDecimal amount;
    private String currency;
    private String currencyId;

    /**
     * Default constructor.
     */
    public OperationAmountFieldAttribute() {
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
    public OperationAmountFieldAttribute(String id, String label, BigDecimal amount, String currency, String currencyId, Map<String, String> formattedValues) {
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