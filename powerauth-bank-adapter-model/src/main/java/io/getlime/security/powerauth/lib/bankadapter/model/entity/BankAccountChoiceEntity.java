package io.getlime.security.powerauth.lib.bankadapter.model.entity;


/**
 * Class representing choice of a bank account.
 *
 * @author Roman Strobl, roman.strobl@lime-company.eu
 */
public class BankAccountChoiceEntity extends FormDataChangeEntity {

    private String bankAccountNumber;

    public BankAccountChoiceEntity() {
        this.type = Type.BANK_ACCOUNT_CHOICE;
    }

    public String getBankAccountNumber() {
        return bankAccountNumber;
    }

    public void setBankAccountNumber(String bankAccountNumber) {
        this.bankAccountNumber = bankAccountNumber;
    }

}
