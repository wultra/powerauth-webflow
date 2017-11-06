package io.getlime.security.powerauth.lib.nextstep.model.entity.attribute;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.Objects;

/**
 * Abstract class that represents an attribute used during the operation.
 *
 * This class requires JSON annotation since it is a base class that is extended with specific types.
 *
 * @author Petr Dvorak, petr@lime-company.eu
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = OperationTitleAttribute.class, name = "TITLE"),
        @JsonSubTypes.Type(value = OperationMessageAttribute.class, name = "MAIN_MESSAGE"),
        @JsonSubTypes.Type(value = OperationAmountAttribute.class, name = "AMOUNT"),
        @JsonSubTypes.Type(value = OperationNoteAttribute.class, name = "MESSAGE"),
        @JsonSubTypes.Type(value = OperationBankAccountChoiceAttribute.class, name = "BANK_ACCOUNT_CHOICE"),
        @JsonSubTypes.Type(value = OperationKeyValueAttribute.class, name = "KEY_VALUE")
})
public class OperationFormAttribute {

    // TODO - revise types MAIN_MESSAGE AND MESSAGE to avoid confusion
    public enum Type {
        TITLE,
        MAIN_MESSAGE,
        AMOUNT,
        MESSAGE,
        BANK_ACCOUNT_CHOICE,
        KEY_VALUE
    }

    // JsonIgnore added, otherwise type was serialized twice
    @JsonIgnore
    protected Type type;

    protected String id;

    protected String label;

    // JsonIgnore added, otherwise type was serialized twice
    /**
     * Get the type of OperationFormAttribute.
     */
    @JsonIgnore
    public Type getType() {
        return type;
    }

    /**
     * Get the attribute ID which is used as a unique identifier of the attribute and as its i18n key.
     * @return ID.
     */
    public String getId() {
        return id;
    }

    /**
     * Set the attribute ID which is used as a unique identifier of the attribute and as its i18n key.
     * @param id ID.
      */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Get the attribute label.
     * @return Label.
     */
    public String getLabel() {
        return label;
    }

    /**
     * Set the attribute label.
     * @param label Label.
     */
    public void setLabel(String label) {
        this.label = label;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OperationFormAttribute attribute = (OperationFormAttribute) o;
        return type == attribute.type &&
                Objects.equals(id, attribute.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, id);
    }
}
