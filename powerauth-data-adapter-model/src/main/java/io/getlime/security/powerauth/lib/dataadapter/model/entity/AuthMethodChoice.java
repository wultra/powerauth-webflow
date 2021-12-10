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


/**
 * Class representing choice of the authorization method.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class AuthMethodChoice extends FormDataChange {

    /**
     * Enumeration for authentication method choice.
     */
    public enum ChosenAuthMethod {
        /**
         * Authentication using PowerAuth token.
         */
        POWERAUTH_TOKEN,

        /**
         * Authentication using SMS key.
         */
        SMS_KEY
    }

    private ChosenAuthMethod chosenAuthMethod;

    /**
     * Default constructor.
     */
    public AuthMethodChoice() {
        this.type = Type.AUTH_METHOD_CHOICE;
    }

    /**
     * Get chosen authentication method.
     * @return Chosen authentication method.
     */
    public ChosenAuthMethod getChosenAuthMethod() {
        return chosenAuthMethod;
    }

    /**
     * Set chosen authentication method.
     * @param chosenAuthMethod Chosen authentication method.
     */
    public void setChosenAuthMethod(ChosenAuthMethod chosenAuthMethod) {
        this.chosenAuthMethod = chosenAuthMethod;
    }

}
