package io.getlime.security.powerauth.lib.nextstep.model.entity.attribute;

/**
 * Class representing the operation form attribute.
 *
 * @author Roman Strobl, roman.strobl@lime-company.eu
 */
public class OperationFormAttribute {

    private String id;
    private String value;

    public OperationFormAttribute() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
