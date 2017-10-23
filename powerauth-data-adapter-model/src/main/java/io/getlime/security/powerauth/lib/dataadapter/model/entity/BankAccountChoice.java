package io.getlime.security.powerauth.lib.dataadapter.model.entity;


/**
 * Class representing choice of a bank account.
 *
 * @author Roman Strobl, roman.strobl@lime-company.eu
 */
public class BankAccountChoice extends FormDataChange {

    private String bankAccountNumber;

    public BankAccountChoice() {
        this.type = Type.BANK_ACCOUNT_CHOICE;
    }

    public String getBankAccountNumber() {
        return bankAccountNumber;
    }

    public void setBankAccountNumber(String bankAccountNumber) {
        this.bankAccountNumber = bankAccountNumber;
    }

}
