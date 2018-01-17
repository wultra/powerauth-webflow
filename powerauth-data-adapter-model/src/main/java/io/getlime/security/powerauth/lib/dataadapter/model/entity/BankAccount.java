/*
 * Copyright 2017 Lime - HighTech Solutions s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.getlime.security.powerauth.lib.dataadapter.model.entity;

import java.math.BigDecimal;

/**
 * Class representing details of a bank account.
 *
 * @author Roman Strobl, roman.strobl@lime-company.eu
 */
public class BankAccount {

    private String number;
    private String accountId;
    private String name;
    private BigDecimal balance;
    private String currency;
    private boolean usableForPayment;
    private String unusableForPaymentReason;

    public BankAccount() {
    }

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
