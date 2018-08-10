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
package io.getlime.security.powerauth.lib.mtoken.model.entity.attributes;

/**
 * Base class for generic attribute of mobile token form data.
 *
 * @author Petr Dvorak, petr@lime-company.eu
 */
public class Attribute {

    /**
     * Attribute type.
     */
    public enum Type {
        AMOUNT,
        KEY_VALUE,
        NOTE,
        HEADING,
        PARTY_INFO
    }

    protected Type type;
    protected String id;
    protected String label;

    /**
     * Get attribute type.
     * @return Attribute type.
     */
    public Type getType() {
        return type;
    }

    /**
     * Set attribute type.
     * @param type Attribute type.
     */
    public void setType(Type type) {
        this.type = type;
    }

    /**
     * Get attribute ID.
     * @return Attribute ID.
     */
    public String getId() {
        return id;
    }

    /**
     * Set attribute ID.
     * @param id Attribute ID.
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Get attribute label.
     * @return Attribute label.
     */
    public String getLabel() {
        return label;
    }

    /**
     * Set attribute label.
     * @param label Attribute label.
     */
    public void setLabel(String label) {
        this.label = label;
    }
}
