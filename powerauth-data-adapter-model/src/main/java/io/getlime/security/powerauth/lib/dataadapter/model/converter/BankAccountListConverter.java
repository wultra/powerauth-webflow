/*
 * Copyright 2017 Wultra s.r.o.
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
package io.getlime.security.powerauth.lib.dataadapter.model.converter;

import io.getlime.security.powerauth.lib.dataadapter.model.entity.BankAccount;
import io.getlime.security.powerauth.lib.nextstep.model.entity.BankAccountDetail;

import java.util.ArrayList;
import java.util.List;

/**
 * Converter for bank account list.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class BankAccountListConverter {

    /**
     * Converter from BankAccountDetail list.
     * @param bankAccounts BankAccountDetail list.
     * @return BankAccount list.
     */
    public List<BankAccount> fromBankAccountDetailList(List<BankAccountDetail> bankAccounts) {
        List<BankAccount> result = new ArrayList<>();
        for (BankAccountDetail bankAccountDetail: bankAccounts) {
            BankAccount bankAccount = new BankAccount(bankAccountDetail.getNumber(), bankAccountDetail.getAccountId(),
                    bankAccountDetail.getName(), bankAccountDetail.getBalance(), bankAccountDetail.getCurrency(),
                    bankAccountDetail.isUsableForPayment(), bankAccountDetail.getUnusableForPaymentReason());
            result.add(bankAccount);
        }
        return result;
    }

    /**
     * Converter from BankAccount list.
     * @param bankAccounts BankAccount list.
     * @return BankAccountDetail list.
     */
    public List<BankAccountDetail> fromBankAccountList(List<BankAccount> bankAccounts) {
        List<BankAccountDetail> result = new ArrayList<>();
        for (BankAccount bankAccount: bankAccounts) {
            BankAccountDetail bankAccountDetail = new BankAccountDetail(bankAccount.getNumber(), bankAccount.getAccountId(),
                    bankAccount.getName(), bankAccount.getBalance(), bankAccount.getCurrency(),
                    bankAccount.isUsableForPayment(), bankAccount.getUnusableForPaymentReason());
            result.add(bankAccountDetail);
        }
        return result;
    }
}
