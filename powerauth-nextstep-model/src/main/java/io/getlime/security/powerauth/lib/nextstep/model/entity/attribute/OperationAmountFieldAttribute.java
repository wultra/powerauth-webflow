package io.getlime.security.powerauth.lib.nextstep.model.entity.attribute;

import java.math.BigDecimal;

/**
 * Class representing an operation display attribute for transaction amount and currency.
 *
 * @author Petr Dvorak, petr@lime-company.eu
 */
public class OperationAmountFieldAttribute extends OperationFormFieldAttributeFormatted {

    private BigDecimal amount;
    private String currency;
    private String currencyId;

    public OperationAmountFieldAttribute() {
        this.type = Type.AMOUNT;
        this.valueFormatType = ValueFormatType.AMOUNT;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getCurrencyId() {
        return currencyId;
    }

    public void setCurrencyId(String currencyId) {
        this.currencyId = currencyId;
    }
}