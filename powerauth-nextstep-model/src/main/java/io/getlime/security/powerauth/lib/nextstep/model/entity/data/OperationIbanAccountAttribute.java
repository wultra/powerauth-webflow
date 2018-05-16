package io.getlime.security.powerauth.lib.nextstep.model.entity.data;

/**
 * IBAN account in operation data.
 *
 * @author Roman Strobl, roman.strobl@lime-company.eu
 */
public class OperationIbanAccountAttribute extends OperationDataAttribute {

    private String iban;
    private String bic;

    /**
     * Default constructor.
     */
    public OperationIbanAccountAttribute() {
        this.type = Type.ACCOUNT_IBAN;
    }

    /**
     * Constructor with IBAN.
     * @param iban IBAN.
     */
    public OperationIbanAccountAttribute(String iban) {
        this.type = Type.ACCOUNT_IBAN;
        this.iban = iban;
    }

    /**
     * Construtor with IBAN and BIC.
     * @param iban IBAN.
     * @param bic BIC.
     */
    public OperationIbanAccountAttribute(String iban, String bic) {
        this.type = Type.ACCOUNT_IBAN;
        this.iban = iban;
        this.bic = bic;
    }

    /**
     * Get IBAN.
     * @return IBAN.
     */
    public String getIban() {
        return iban;
    }

    /**
     * Set IBAN.
     * @param iban IBAN.
     */
    public void setIban(String iban) {
        this.iban = iban;
    }

    /**
     * Get BIC.
     * @return BIC.
     */
    public String getBic() {
        return bic;
    }

    /**
     * Set BIC.
     * @param bic BIC.
     */
    public void setBic(String bic) {
        this.bic = bic;
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public String formattedValue() {
        if (iban == null) {
            return "";
        }
        if (bic == null) {
            return "I"+iban;
        }
        return "I"+iban+","+bic;
    }
}
