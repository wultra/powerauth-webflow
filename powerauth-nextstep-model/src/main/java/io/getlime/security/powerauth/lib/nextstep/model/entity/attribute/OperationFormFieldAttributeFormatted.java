package io.getlime.security.powerauth.lib.nextstep.model.entity.attribute;

/**
 * Class that represents a formatted form field attribute. Formatting is done based on value format type.
 *
 * @author Roman Strobl, roman.strobl@lime-company.eu
 */
public class OperationFormFieldAttributeFormatted extends OperationFormFieldAttribute {

    /**
     * Supported value format types.
     */
    public enum ValueFormatType {
        TEXT,
        LOCALIZED_TEXT,
        DATE,
        NUMBER,
        AMOUNT,
        ACCOUNT
    }

    protected ValueFormatType valueFormatType;

    protected String formattedValue;

    /**
     * Get value format type of this attribute.
     * @return Value format type.
     */
    public ValueFormatType getValueFormatType() {
        return valueFormatType;
    }

    /**
     * Get formatted value of this attribute.
     * @return Formatted value.
     */
    public String getFormattedValue() {
        return formattedValue;
    }

    /**
     * Set formatted value of this attribute.
     * @param formattedValue Formatted value.
     */
    public void setFormattedValue(String formattedValue) {
        this.formattedValue = formattedValue;
    }

}
