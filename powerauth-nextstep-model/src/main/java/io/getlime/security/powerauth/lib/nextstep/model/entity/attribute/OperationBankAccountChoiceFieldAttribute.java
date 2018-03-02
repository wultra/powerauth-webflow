/*
 * Copyright 2018 Lime - HighTech Solutions s.r.o.
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
package io.getlime.security.powerauth.lib.nextstep.model.entity.attribute;

import io.getlime.security.powerauth.lib.nextstep.model.entity.BankAccountDetail;

import java.util.List;

/**
 * Class representing a bank account choice form field attribute.
 *
 * @author Roman Strobl, roman.strobl@lime-company.eu
 */
public class OperationBankAccountChoiceFieldAttribute extends OperationFormFieldAttribute {

    private List<BankAccountDetail> bankAccounts;
    private boolean enabled;
    private String defaultValue;

    public OperationBankAccountChoiceFieldAttribute() {
        this.type = Type.BANK_ACCOUNT_CHOICE;
    }

    public List<BankAccountDetail> getBankAccounts() {
        return bankAccounts;
    }

    public void setBankAccounts(List<BankAccountDetail> bankAccounts) {
        this.bankAccounts = bankAccounts;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }
}
