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
