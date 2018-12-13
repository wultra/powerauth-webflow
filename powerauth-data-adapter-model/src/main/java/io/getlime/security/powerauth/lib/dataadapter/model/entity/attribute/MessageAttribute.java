/*
 * Copyright 2018 Wultra s.r.o.
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
package io.getlime.security.powerauth.lib.dataadapter.model.entity.attribute;

/**
 * Class representing the operation form attribute.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class MessageAttribute {

    protected String id;
    protected String message;

    /**
     * Default constructor.
     */
    public MessageAttribute() {
    }

    /**
     * Constructor with attribute ID and message.
     * @param id Attribute ID.
     * @param message Message.
     */
    public MessageAttribute(String id, String message) {
        this.id = id;
        this.message = message;
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
     * Get message.
     * @return Message.
     */
    public String getMessage() {
        return message;
    }

    /**
     * Set message
     * @param message Message.
     */
    public void setMessage(String message) {
        this.message = message;
    }
}
