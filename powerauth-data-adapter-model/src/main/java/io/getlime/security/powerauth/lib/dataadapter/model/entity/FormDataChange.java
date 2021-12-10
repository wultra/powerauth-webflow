/*
 * PowerAuth Web Flow and related software components
 * Copyright (C) 2017 Wultra s.r.o.
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
package io.getlime.security.powerauth.lib.dataadapter.model.entity;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Class representing change of operation form data.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = BankAccountChoice.class, name = "BANK_ACCOUNT_CHOICE"),
        @JsonSubTypes.Type(value = AuthMethodChoice.class, name = "AUTH_METHOD_CHOICE")
})
public class FormDataChange {

    /**
     * Enumeration representing form data change type.
     */
    public enum Type {
        /**
         * Bank account was chosen.
         */
        BANK_ACCOUNT_CHOICE,
        /**
         * Authenticatio  method was chosen.
         */
        AUTH_METHOD_CHOICE
    }

    /**
     * Form data change type.
     */
    @JsonIgnore
    // JsonIgnore added, otherwise type was serialized twice
    protected Type type;

    /**
     * Get form data change type.
     * @return Form data change type.
     */
    // JsonIgnore added, otherwise type was serialized twice
    @JsonIgnore
    public Type getType() {
        return type;
    }

}
