package io.getlime.security.powerauth.lib.nextstep.model.entity;

import java.math.BigDecimal;

/**
 * Class representing details of a bank account.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class BankAccountDetail {

    private String number;
    private String accountId;
    private String name;
    private BigDecimal balance;
    private String currency;
    private boolean usableForPayment;
    private String unusableForPaymentReason;

    public BankAccountDetail() {
    }

    public BankAccountDetail(String number, String accountId, String name, BigDecimal balance, String currency,
                             boolean usableForPayment, String unusableForPaymentReason) {
        this.number = number;
        this.accountId = accountId;
        this.name = name;
        this.balance = balance;
        this.currency = currency;
        this.usableForPayment = usableForPayment;
        this.unusableForPaymentReason = unusableForPaymentReason;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
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
