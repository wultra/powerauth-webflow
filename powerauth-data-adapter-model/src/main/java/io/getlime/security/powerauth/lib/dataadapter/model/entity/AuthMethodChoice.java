/*
 * Copyright 2017 Wultra s.r.o.
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
