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
package io.getlime.security.powerauth.lib.nextstep.model.entity.attribute;

/**
 * Class representing the operation form attribute.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class OperationFormMessageAttribute {

    /**
     * Field identifier.
     */
    protected String id;

    /**
     * Message.
     */
    protected String message;

    /**
     * Default constructor.
     */
    public OperationFormMessageAttribute() {
    }

    /**
     * Constructor with attribute ID and message.
     * @param id Attribute ID.
     * @param message Message.
     */
    public OperationFormMessageAttribute(String id, String message) {
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
