package io.getlime.security.powerauth.lib.nextstep.model.entity;

/**
 * Class representing an operation display attribute for generic key-value pair.
 *
 * @author Petr Dvorak, petr@lime-company.eu
 */
public class OperationKeyValueAttribute extends OperationFormAttribute {

    private String label;
    private String value;

    public OperationKeyValueAttribute() {
        this.type = Type.KEY_VALUE;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
