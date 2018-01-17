package io.getlime.security.powerauth.lib.nextstep.model.entity.attribute;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.Objects;

/**
 * Abstract class representing a form configuration field.
 *
 * This class requires JSON annotation since it is a base class that is extended with specific types.
 *
 * @author Roman Strobl, roman.strobl@lime-company.eu
 *
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = OperationBankAccountChoiceFieldConfig.class, name = "BANK_ACCOUNT_CHOICE"),
})
public class OperationFormFieldConfig {

    protected String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OperationFormFieldConfig fieldConfig = (OperationFormFieldConfig) o;
        return Objects.equals(id, fieldConfig.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
