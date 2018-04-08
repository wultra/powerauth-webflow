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
 * Attribute representing a key-value item, where key and value are displayed
 * next to each other.
 *
 * @author Petr Dvorak, petr@lime-company.eu
 */
public class KeyValueAttribute extends Attribute {

    private String value;

    /**
     * Default constructor.
     */
    public KeyValueAttribute() {
        super();
        this.setType(Type.KEY_VALUE);
    }

    /**
     * Constructor with all details.
     * @param id Attribute ID.
     * @param label Attribute label.
     * @param value Value.
     */
    public KeyValueAttribute(String id, String label, String value) {
        this();
        this.id = id;
        this.label = label;
        this.value = value;
    }

    /**
     * Get value.
     * @return Value.
     */
    public String getValue() {
        return value;
    }

    /**
     * Set value.
     * @param value Value.
     */
    public void setValue(String value) {
        this.value = value;
    }
}
