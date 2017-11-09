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

    public OperationBankAccountChoiceFieldAttribute() {
        this.type = Type.BANK_ACCOUNT_CHOICE;
    }

    public List<BankAccountDetail> getBankAccounts() {
        return bankAccounts;
    }

    public void setBankAccounts(List<BankAccountDetail> bankAccounts) {
        this.bankAccounts = bankAccounts;
    }

}
