package io.getlime.security.powerauth.lib.dataadapter.model.entity;


/**
 * Class representing choice of a bank account.
 *
 * @author Roman Strobl, roman.strobl@lime-company.eu
 */
public class BankAccountChoice extends FormDataChange {

    private String bankAccountId;

    public BankAccountChoice() {
        this.type = Type.BANK_ACCOUNT_CHOICE;
    }

    public String getBankAccountId() {
        return bankAccountId;
    }

    public void setBankAccountId(String bankAccountId) {
        this.bankAccountId = bankAccountId;
    }

}
