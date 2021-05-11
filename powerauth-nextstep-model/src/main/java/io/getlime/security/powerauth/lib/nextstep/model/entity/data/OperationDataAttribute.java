package io.getlime.security.powerauth.lib.nextstep.model.entity.data;

/**
 * Abstract operation data field.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public abstract class OperationDataAttribute {

    /**
     * Operation data attribute type.
     */
    public enum Type {
        /**
         * Monetary amount.
         */
        AMOUNT,

        /**
         * Generic account format.
         */
        ACCOUNT_GENERIC,

        /**
         * Account in IBAN format.
         */
        ACCOUNT_IBAN,

        /**
         * Payment date.
         */
        DATE,

        /**
         * Payment reference.
         */
        REFERENCE,

        /**
         * Payment note.
         */
        NOTE,

        /**
         * Text element.
         */
        TEXT
    }

    /**
     * Data type.
     */
    protected Type type;

    /**
     * Get operation data attribute type.
     * @return Attribute type.
     */
    public abstract Type getType();

    /**
     * Generate formatted value for attribute.
     * @return Formatted value.
     */
    public abstract String formattedValue();

}
