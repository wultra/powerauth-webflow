package io.getlime.security.powerauth.app.webflow.demo.model;

import java.math.BigDecimal;

/**
 * @author Petr Dvorak, petr@wultra.com
 */
public class PaymentForm {

    private BigDecimal amount;
    private String currency;
    private String account;
    private String note;
    private String dueDate;
    private String appContext;

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

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public String getAppContext() { return appContext; }

    public void setAppContext(String appContext) { this.appContext = appContext; }
}
