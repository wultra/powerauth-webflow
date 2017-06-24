package io.getlime.security.powerauth.lib.nextstep.model.entity;

/**
 * Class representing an operation display attribute for a message.
 *
 * @author Petr Dvorak, petr@lime-company.eu
 */
public class OperationMessageDisplayAttribute extends OperationDisplayAttribute {

    private String label;
    private String message;

    public OperationMessageDisplayAttribute() {
        this.type = Type.MESSAGE;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
