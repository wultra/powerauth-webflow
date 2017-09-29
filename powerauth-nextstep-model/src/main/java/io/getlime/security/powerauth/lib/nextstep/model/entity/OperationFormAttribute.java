package io.getlime.security.powerauth.lib.nextstep.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Abstract class that represents an attribute used during the operation.
 *
 * This class requires JSON annotation since it is a base class that is extended with specific types.
 *
 * @author Petr Dvorak, petr@lime-company.eu
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = OperationAmountAttribute.class, name = "AMOUNT"),
        @JsonSubTypes.Type(value = OperationKeyValueAttribute.class, name = "KEY_VALUE"),
        @JsonSubTypes.Type(value = OperationMessageAttribute.class, name = "MESSAGE"),
        @JsonSubTypes.Type(value = OperationBankAccountChoiceAttribute.class, name = "BANK_ACCOUNT_CHOICE")
})
public class OperationFormAttribute {

    public enum Type {
        AMOUNT,
        KEY_VALUE,
        MESSAGE,
        BANK_ACCOUNT_CHOICE
    }

    // JsonIgnore added, otherwise type was serialized twice
    @JsonIgnore
    protected Type type;

    // JsonIgnore added, otherwise type was serialized twice
    @JsonIgnore
    public Type getType() {
        return type;
    }

}
