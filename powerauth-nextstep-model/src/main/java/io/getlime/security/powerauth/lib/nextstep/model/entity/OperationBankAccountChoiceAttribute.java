package io.getlime.security.powerauth.lib.nextstep.model.entity;

import java.util.List;

/**
 * Class representing a bank account choice form attribute.
 *
 * @author Roman Strobl, roman.strobl@lime-company.eu
 */
public class OperationBankAccountChoiceAttribute extends OperationFormAttribute {

    private String label;
    private List<BankAccountDetail> bankAccounts;
    private String chosenBankAccountNumber;
    private boolean choiceDisabled;

    public OperationBankAccountChoiceAttribute() {
        this.type = Type.BANK_ACCOUNT_CHOICE;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public List<BankAccountDetail> getBankAccounts() {
        return bankAccounts;
    }

    public void setBankAccounts(List<BankAccountDetail> bankAccounts) {
        this.bankAccounts = bankAccounts;
    }

    public String getChosenBankAccountNumber() {
        return chosenBankAccountNumber;
    }

    public void setChosenBankAccountNumber(String chosenBankAccountNumber) {
        this.chosenBankAccountNumber = chosenBankAccountNumber;
    }

    public boolean isChoiceDisabled() {
        return choiceDisabled;
    }

    public void setChoiceDisabled(boolean choiceDisabled) {
        this.choiceDisabled = choiceDisabled;
    }
}
