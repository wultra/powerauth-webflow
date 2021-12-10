/*
 * PowerAuth Web Flow and related software components
 * Copyright (C) 2019 Wultra s.r.o.
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
package io.getlime.security.powerauth.lib.dataadapter.model.enumeration;

/**
 * Supported password protection types.
 *
 * <ul>
 * <li>NO_PROTECTION - User ID and password sent via plaintext in the request.</li>
 * <li>PASSWORD_ENCRYPTION_AES - User ID is sent in plain text and password sent encrypted by AES algorithm in the request.
 *     The encrypted password format is following: [ivBase64]:[encryptedDataBase64], without square brackets.
 *     <ul>
 *         <li>ivBase64 - Base64 encoded initialization vector bytes.</li>
 *         <li>encryptedDataBase64 - Base64 encoded encrypted password data bytes.</li>
 *     </ul>
 * </li>
 * </ul>
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public enum PasswordProtectionType {

    /**
     * No password protection, plain text password format is used.
     */
    NO_PROTECTION,

    /**
     * Password is encrypted using AES.
     */
    PASSWORD_ENCRYPTION_AES,
}
