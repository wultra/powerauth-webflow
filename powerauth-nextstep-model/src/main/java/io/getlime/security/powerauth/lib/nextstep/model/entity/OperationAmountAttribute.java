package io.getlime.security.powerauth.lib.nextstep.model.entity;

import java.math.BigDecimal;

/**
 * Class representing an operation display attribute for transaction amount.
 * It contains holders for amount and currency.
 *
 * @author Petr Dvorak, petr@lime-company.eu
 */
public class OperationAmountAttribute extends OperationFormAttribute {

    private String label;
    private BigDecimal amount;
    private String currency;

    public OperationAmountAttribute() {
        this.type = Type.AMOUNT;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
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
}
