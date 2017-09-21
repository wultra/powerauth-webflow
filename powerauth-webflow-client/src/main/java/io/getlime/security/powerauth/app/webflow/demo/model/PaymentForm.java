package io.getlime.security.powerauth.app.webflow.demo.model;

import java.math.BigDecimal;

/**
 * @author Petr Dvorak, petr@lime-company.eu
 */
public class PaymentForm {

    private BigDecimal amount;
    private String currency;
    private String account;
    private String note;

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

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

}
