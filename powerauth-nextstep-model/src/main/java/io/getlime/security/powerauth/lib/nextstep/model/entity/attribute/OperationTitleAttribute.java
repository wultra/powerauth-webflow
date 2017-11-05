package io.getlime.security.powerauth.lib.nextstep.model.entity.attribute;

/**
 * Class representing an operation display attribute for the operation title.
 *
 * @author Roman Strobl, roman.strobl@lime-company.eu
 */
public class OperationTitleAttribute extends OperationFormAttribute {

    private String title;

    public OperationTitleAttribute() {
        this.type = Type.TITLE;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
