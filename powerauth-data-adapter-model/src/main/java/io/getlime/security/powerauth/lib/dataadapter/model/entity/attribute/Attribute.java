/*
 * PowerAuth Web Flow and related software components
 * Copyright (C) 2018 Wultra s.r.o.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package io.getlime.security.powerauth.lib.dataadapter.model.entity.attribute;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.Objects;

/**
 * Abstract class that represents a form field attribute.
 * <p>
 * This class requires JSON annotation since it is a base class that is extended with specific types.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = AmountAttribute.class, name = "AMOUNT"),
        @JsonSubTypes.Type(value = NoteAttribute.class, name = "NOTE"),
        @JsonSubTypes.Type(value = BankAccountChoiceAttribute.class, name = "BANK_ACCOUNT_CHOICE"),
        @JsonSubTypes.Type(value = KeyValueAttribute.class, name = "KEY_VALUE"),
        @JsonSubTypes.Type(value = BannerAttribute.class, name = "BANNER"),
        @JsonSubTypes.Type(value = HeadingAttribute.class, name = "HEADING"),
        @JsonSubTypes.Type(value = PartyInfoAttribute.class, name = "PARTY_INFO"),
})
public class Attribute {

    /**
     * Attribute type.
     */
    public enum Type {
        /**
         * Monetary amount.
         */
        AMOUNT,

        /**
         * Text note.
         */
        NOTE,

        /**
         * Bank account choice.
         */
        BANK_ACCOUNT_CHOICE,

        /**
         * Key-value attribute.
         */
        KEY_VALUE,

        /**
         * Banner with text.
         */
        BANNER,

        /**
         * Heading element.
         */
        HEADING,

        /**
         * Third party info.
         */
        PARTY_INFO
    }

    /**
     * Attribute type.
     */
    // JsonIgnore added, otherwise type was serialized twice
    @JsonIgnore
    protected Type type;

    /**
     * Attribute identifier.
     */
    protected String id;

    /**
     * Attribute label.
     */
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
        Attribute attribute = (Attribute) o;
        return type == attribute.type &&
                Objects.equals(id, attribute.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, id);
    }
}
