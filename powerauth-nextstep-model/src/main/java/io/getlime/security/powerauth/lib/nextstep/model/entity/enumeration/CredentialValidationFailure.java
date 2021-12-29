/*
 * PowerAuth Web Flow and related software components
 * Copyright (C) 2021 Wultra s.r.o.
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
package io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration;

/**
 * Enum representing a credential validation failure.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public enum CredentialValidationFailure {

    /**
     * Credential is empty.
     */
    CREDENTIAL_EMPTY,

    /**
     * Credential is too short.
     */
    CREDENTIAL_TOO_SHORT,

    /**
     * Credential is too long.
     */
    CREDENTIAL_TOO_LONG,

    /**
     * Credential history check failed.
     */
    CREDENTIAL_HISTORY_CHECK_FAILED,

    /**
     * Credential contains illegal whitespace.
     */
    CREDENTIAL_ILLEGAL_WHITESPACE,

    /**
     * Credential contains username.
     */
    CREDENTIAL_ILLEGAL_USERNAME,

    /**
     * Credential contains username in reverse.
     */
    CREDENTIAL_ILLEGAL_USERNAME_REVERSED,

    /**
     * Credential is missing an allowed character.
     */
    CREDENTIAL_ALLOWED_CHAR_FAILED,

    /**
     * Credential allowed match using regular expresion failed.
     */
    CREDENTIAL_ALLOWED_MATCH_FAILED,

    /**
     * Credential contains an illegal character.
     */
    CREDENTIAL_ILLEGAL_CHAR,

    /**
     * Credential illegal match using regular expresion failed.
     */
    CREDENTIAL_ILLEGAL_MATCH,

    /**
     * Credential contains insufficient number of uppercase characters.
     */
    CREDENTIAL_INSUFFICIENT_UPPERCASE,

    /**
     * Credential contains insufficient number of lowercase characters.
     */
    CREDENTIAL_INSUFFICIENT_LOWERCASE,

    /**
     * Credential contains insufficient number of alphabetical characters.
     */
    CREDENTIAL_INSUFFICIENT_ALPHABETICAL,

    /**
     * Credential contains insufficient number of digits.
     */
    CREDENTIAL_INSUFFICIENT_DIGIT,

    /**
     * Credential contains insufficient number of special characters.
     */
    CREDENTIAL_INSUFFICIENT_SPECIAL,

    /**
     * Username is empty.
     */
    USERNAME_EMPTY,

    /**
     * Username is too short.
     */
    USERNAME_TOO_SHORT,

    /**
     * Username is too long.
     */
    USERNAME_TOO_LONG,

    /**
     * Username contains whitespace.
     */
    USERNAME_ILLEGAL_WHITESPACE,

    /**
     * Username pattern match failed.
     */
    USERNAME_ALLOWED_MATCH_FAILED,

    /**
     * Username already exists.
     */
    USERNAME_ALREADY_EXISTS

}
