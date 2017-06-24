package io.getlime.security.powerauth.lib.nextstep.model.entity;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Abstract class that represents an attribute displayed by the operation.
 *
 * This class requires JSON annotation since it is a base class that is extended with specific types.
 *
 * @author Petr Dvorak, petr@lime-company.eu
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = OperationAmountDisplayAttribute.class, name = "AMOUNT"),
        @JsonSubTypes.Type(value = OperationKeyValueDisplayAttribute.class, name = "KEY_VALUE"),
        @JsonSubTypes.Type(value = OperationMessageDisplayAttribute.class, name = "MESSAGE")
})
public class OperationDisplayAttribute {

    public enum Type {
        AMOUNT,
        KEY_VALUE,
        MESSAGE
    }

    protected Type type;

    public Type getType() {
        return type;
    }

}
