package io.getlime.security.powerauth.lib.nextstep.model.entity.attribute;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.Objects;

/**
 * Abstract class that represents a form field attribute.
 *
 * This class requires JSON annotation since it is a base class that is extended with specific types.
 *
 * @author Petr Dvorak, petr@lime-company.eu
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = OperationAmountFieldAttribute.class, name = "AMOUNT"),
        @JsonSubTypes.Type(value = OperationNoteFieldAttribute.class, name = "NOTE"),
        @JsonSubTypes.Type(value = OperationBankAccountChoiceFieldAttribute.class, name = "BANK_ACCOUNT_CHOICE"),
        @JsonSubTypes.Type(value = OperationKeyValueFieldAttribute.class, name = "KEY_VALUE")
})
public class OperationFormFieldAttribute {

    public enum Type {
        AMOUNT,
        NOTE,
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
     * Get the type of OperationFormFieldAttribute.
     * @return Atribute type.
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
        OperationFormFieldAttribute attribute = (OperationFormFieldAttribute) o;
        return type == attribute.type &&
                Objects.equals(id, attribute.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, id);
    }
}
