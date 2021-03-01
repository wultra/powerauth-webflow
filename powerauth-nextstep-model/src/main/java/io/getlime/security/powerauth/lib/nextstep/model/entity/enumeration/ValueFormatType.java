package io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration;

/**
 * Enumeration with value format types.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public enum ValueFormatType {

    /**
     * Value formatted as text.
     */
    TEXT,

    /**
     * Value formatted as localized text.
     */
    LOCALIZED_TEXT,

    /**
     * Value formatted as date.
     */
    DATE,

    /**
     * Value formatted as a number.
     */
    NUMBER,

    /**
     * Value formatted as a monetary amount.
     */
    AMOUNT,

    /**
     * Value formatted as a bank account.
     */
    ACCOUNT
}
