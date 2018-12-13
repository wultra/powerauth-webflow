package io.getlime.security.powerauth.lib.nextstep.model.entity.data;

/**
 * Generic (non-IBAN) account in operation data.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class OperationGenericAccountAttribute extends OperationDataAttribute {

    private String account;

    /**
     * Default constructor.
     */
    public OperationGenericAccountAttribute() {
        this.type = Type.ACCOUNT_GENERIC;
    }

    /**
     * Constructor with account.
     * @param account Account.
     */
    public OperationGenericAccountAttribute(String account) {
        this.type = Type.ACCOUNT_GENERIC;
        this.account = account;
    }

    /**
     * Get account.
     * @return Account.
     */
    public String getAccount() {
        return account;
    }

    /**
     * Set account.
     * @param account Account.
     */
    public void setAccount(String account) {
        this.account = account;
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public String formattedValue() {
        if (account == null) {
            return "";
        }
        return "Q"+ account;
    }
}
