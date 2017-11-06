package io.getlime.security.powerauth.lib.nextstep.model.entity.attribute;

/**
 * Class representing an operation display attribute for the operation message.
 *
 * @author Roman Strobl, roman.strobl@lime-company.eu
 */
public class OperationMessageAttribute extends OperationFormAttribute {

    private String message;

    public OperationMessageAttribute() {
        this.type = Type.MAIN_MESSAGE;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
