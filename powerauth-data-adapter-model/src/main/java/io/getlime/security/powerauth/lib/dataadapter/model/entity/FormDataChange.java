package io.getlime.security.powerauth.lib.dataadapter.model.entity;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Class representing change of operation formData.
 *
 * @author Roman Strobl, roman.strobl@lime-company.eu
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = BankAccountChoice.class, name = "BANK_ACCOUNT_CHOICE"),
        @JsonSubTypes.Type(value = AuthMethodChoice.class, name = "AUTH_METHOD_CHOICE")
})
public class FormDataChange {

    public enum Type {
        BANK_ACCOUNT_CHOICE,
        AUTH_METHOD_CHOICE
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
