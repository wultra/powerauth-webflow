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
package io.getlime.security.powerauth.lib.dataadapter.model.entity;

import java.math.BigDecimal;

/**
 * Class representing details of a bank account.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class BankAccount {

    private String number;
    private String accountId;
    private String name;
    private BigDecimal balance;
    private String currency;
    private boolean usableForPayment;
    private String unusableForPaymentReason;

    /**
     * Default constructor.
     */
    public BankAccount() {
    }

    /**
     * Constructor with bank account details.
     * @param number User readable account number.
     * @param accountId Account ID (e.g. IBAN).
     * @param name Account name.
     * @param balance Account balance.
     * @param currency Account currency.
     * @param usableForPayment Whether account is usable for payment.
     * @param unusableForPaymentReason Reason why account is unusable for payment.
     */
    public BankAccount(String number, String accountId, String name, BigDecimal balance, String currency,
                             boolean usableForPayment, String unusableForPaymentReason) {
        this.number = number;
        this.accountId = accountId;
        this.name = name;
        this.balance = balance;
        this.currency = currency;
        this.usableForPayment = usableForPayment;
        this.unusableForPaymentReason = unusableForPaymentReason;
    }

    /**
     * Get readable account number.
     * @return Readable account number.
     */
    public String getNumber() {
        return number;
    }

    /**
     * Set readable account number.
     * @param number Readable account number.
     */
    public void setNumber(String number) {
        this.number = number;
    }

    /**
     * Get account ID (e.g. IBAN).
     * @return Account ID (e.g. IBAN).
     */
    public String getAccountId() {
        return accountId;
    }

    /**
     * Set account ID (e.g. IBAN).
     * @param accountId Account ID (e.g. IBAN).
     */
    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    /**
     * Get account name.
     * @return Account name.
     */
    public String getName() {
        return name;
    }

    /**
     * Set account name.
     * @param name Account name.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get account balance.
     * @return Account balance.
     */
    public BigDecimal getBalance() {
        return balance;
    }

    /**
     * Set account balance.
     * @param balance Account balance.
     */
    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    /**
     * Get account currency.
     * @return Account currency.
     */
    public String getCurrency() {
        return currency;
    }

    /**
     * Set account currency.
     * @param currency Account currency.
     */
    public void setCurrency(String currency) {
        this.currency = currency;
    }

    /**
     * Get whether account is usable for payment.
     * @return Whether account is usable for payment.
     */
    public boolean isUsableForPayment() {
        return usableForPayment;
    }

    /**
     * Set whether account is usable for payment.
     * @param usableForPayment Whether account is usable for payment.
     */
    public void setUsableForPayment(boolean usableForPayment) {
        this.usableForPayment = usableForPayment;
    }

    /**
     * Get reason why account is not usable for payment.
     * @return Reason why account is not usable for payment.
     */
    public String getUnusableForPaymentReason() {
        return unusableForPaymentReason;
    }

    /**
     * Set reason why account is not usable for payment.
     * @param unusableForPaymentReason Reason why account is not usable for payment.
     */
    public void setUnusableForPaymentReason(String unusableForPaymentReason) {
        this.unusableForPaymentReason = unusableForPaymentReason;
    }
}
