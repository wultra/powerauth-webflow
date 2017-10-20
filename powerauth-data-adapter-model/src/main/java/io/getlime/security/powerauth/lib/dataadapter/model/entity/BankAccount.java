package io.getlime.security.powerauth.lib.dataadapter.model.entity;

import java.math.BigDecimal;

/**
 * Class representing details of a bank account.
 *
 * @author Roman Strobl, roman.strobl@lime-company.eu
 */
public class BankAccount {

    private String number;
    private String name;
    private BigDecimal balance;
    private String currency;
    private boolean usableForPayment;
    private String unusableForPaymentReason;

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public boolean isUsableForPayment() {
        return usableForPayment;
    }

    public void setUsableForPayment(boolean usableForPayment) {
        this.usableForPayment = usableForPayment;
    }

    public String getUnusableForPaymentReason() {
        return unusableForPaymentReason;
    }

    public void setUnusableForPaymentReason(String unusableForPaymentReason) {
        this.unusableForPaymentReason = unusableForPaymentReason;
    }
}
