/*
 * Copyright 2021 Wultra s.r.o.
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
