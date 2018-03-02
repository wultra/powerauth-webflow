package io.getlime.security.powerauth.lib.nextstep.model.entity.attribute;

/**
 * Class representing an operation form field attribute for generic key-value pair.
 *
 * @author Petr Dvorak, petr@lime-company.eu
 */
public class OperationKeyValueFieldAttribute extends OperationFormFieldAttributeFormatted {

    private String value;

    public OperationKeyValueFieldAttribute() {
        this.type = Type.KEY_VALUE;
        this.valueFormatType = ValueFormatType.TEXT;
    }

    public OperationKeyValueFieldAttribute(ValueFormatType valueFormatType) {
        this.type = Type.KEY_VALUE;
        this.valueFormatType = valueFormatType;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}