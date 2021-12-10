/*
 * PowerAuth Web Flow and related software components
 * Copyright (C) 2018 Wultra s.r.o.
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
package io.getlime.security.powerauth.lib.nextstep.model.entity.attribute;

import io.getlime.security.powerauth.lib.nextstep.model.entity.BankAccountDetail;

import java.util.List;

/**
 * Class representing a bank account choice form field attribute.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class OperationBankAccountChoiceFieldAttribute extends OperationFormFieldAttribute {

    private List<BankAccountDetail> bankAccounts;
    private boolean enabled;
    private String defaultValue;

    /**
     * Default constructor.
     */
    public OperationBankAccountChoiceFieldAttribute() {
        this.type = Type.BANK_ACCOUNT_CHOICE;
    }

    /**
     * Constructor with all details.
     * @param id ID.
     * @param label Label.
     * @param bankAccounts List of bank accounts.
     * @param enabled Whether choice is enabled.
     * @param defaultValue Default chosen value.
     */
    public OperationBankAccountChoiceFieldAttribute(String id, String label, List<BankAccountDetail> bankAccounts, boolean enabled, String defaultValue) {
        this.type = Type.BANK_ACCOUNT_CHOICE;
        this.id = id;
        this.label = label;
        this.bankAccounts = bankAccounts;
        this.enabled = enabled;
        this.defaultValue = defaultValue;
    }

    /**
     * Get bank account list.
     * @return Bank account list.
     */
    public List<BankAccountDetail> getBankAccounts() {
        return bankAccounts;
    }

    /**
     * Set bank account list.
     * @param bankAccounts Bank account list.
     */
    public void setBankAccounts(List<BankAccountDetail> bankAccounts) {
        this.bankAccounts = bankAccounts;
    }

    /**
     * Whether choice is enabled.
     * @return Whether choice is enabled.
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Set whether choice is enabled.
     * @param enabled Whether choice is enabled.
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Get default chosen value.
     * @return Default chosen value.
     */
    public String getDefaultValue() {
        return defaultValue;
    }

    /**
     * Set default chosen value.
     * @param defaultValue Default chosen value.
     */
    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }
}
