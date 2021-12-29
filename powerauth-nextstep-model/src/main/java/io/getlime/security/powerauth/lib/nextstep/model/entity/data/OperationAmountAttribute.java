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
package io.getlime.security.powerauth.lib.nextstep.model.entity.data;

import io.getlime.security.powerauth.lib.nextstep.model.entity.validator.AmountValidator;
import io.getlime.security.powerauth.lib.nextstep.model.exception.InvalidOperationDataException;

import java.math.BigDecimal;

/**
 * Amount and currency in operation data.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class OperationAmountAttribute extends OperationDataAttribute {

    private BigDecimal amount;
    private String currency;

    /**
     * Default constructor.
     */
    public OperationAmountAttribute() {
        this.type = Type.AMOUNT;
    }

    /**
     * Constructor with amount and currency.
     * @param amount Amount.
     * @param currency Currency.
     * @throws InvalidOperationDataException Thrown in case amount is invalid.
     */
    public OperationAmountAttribute(BigDecimal amount, String currency) throws InvalidOperationDataException {
        this.type = Type.AMOUNT;
        AmountValidator.validateAmount(amount);
        this.amount = amount;
        this.currency = currency;
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
     * @throws InvalidOperationDataException Thrown in case amount is invalid.
     */
    public void setAmount(BigDecimal amount) throws InvalidOperationDataException {
        AmountValidator.validateAmount(amount);
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

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public String formattedValue() {
        if (amount == null || currency == null) {
            return "";
        }
        return "A"+amount.toPlainString()+currency;
    }
}
