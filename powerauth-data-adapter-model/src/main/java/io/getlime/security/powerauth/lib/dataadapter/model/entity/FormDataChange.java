/*
 * Copyright 2017 Lime - HighTech Solutions s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.getlime.security.powerauth.lib.dataadapter.model.entity;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Class representing change of operation form data.
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
