package io.getlime.security.powerauth.lib.nextstep.model.entity.data;

import java.math.BigDecimal;

/**
 * Amount and currency in operation data.
 *
 * @author Roman Strobl, roman.strobl@lime-company.eu
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
     */
    public OperationAmountAttribute(BigDecimal amount, String currency) {
        this.type = Type.AMOUNT;
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
