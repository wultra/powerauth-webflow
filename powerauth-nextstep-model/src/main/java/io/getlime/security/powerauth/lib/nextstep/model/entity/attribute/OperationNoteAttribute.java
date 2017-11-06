package io.getlime.security.powerauth.lib.nextstep.model.entity.attribute;

/**
 * Class representing an operation display attribute for the operation message.
 *
 * @author Petr Dvorak, petr@lime-company.eu
 */
public class OperationNoteAttribute extends OperationFormAttribute {

    private String message;

    public OperationNoteAttribute() {
        this.type = Type.MESSAGE;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
