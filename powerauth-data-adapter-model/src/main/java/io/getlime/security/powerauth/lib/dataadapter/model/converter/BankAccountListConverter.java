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
