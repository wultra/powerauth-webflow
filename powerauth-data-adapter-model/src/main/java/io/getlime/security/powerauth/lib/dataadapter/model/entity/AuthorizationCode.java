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
package io.getlime.security.powerauth.lib.dataadapter.model.entity;

/**
 * Authorization code including salt used when generating the code.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class AuthorizationCode {

    private final String code;
    private final byte[] salt;

    /**
     * Authorization code constructor.
     * @param code Authorization code.
     * @param salt Salt used for creating the authorization code or null for unknown salt.
     */
    public AuthorizationCode(String code, byte[] salt) {
        this.code = code;
        this.salt = salt;
    }

    /**
     * Get authorization code.
     * @return Authorization code.
     */
    public String getCode() {
        return code;
    }

    /**
     * Get salt.
     * @return Salt.
     */
    public byte[] getSalt() {
        return salt;
    }
}
