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
package io.getlime.security.powerauth.lib.dataadapter.model.entity;


/**
 * Class representing choice of a bank account.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class BankAccountChoice extends FormDataChange {

    private String bankAccountId;

    /**
     * Default constructor.
     */
    public BankAccountChoice() {
        this.type = Type.BANK_ACCOUNT_CHOICE;
    }

    /**
     * Get chosen bank account ID (e.g. IBAN).
     * @return Chosen bank account ID.
     */
    public String getBankAccountId() {
        return bankAccountId;
    }

    /**
     * Set chosen bank account ID (e.g. IBAN).
     * @param bankAccountId Chosen bank account id.
     */
    public void setBankAccountId(String bankAccountId) {
        this.bankAccountId = bankAccountId;
    }

}
