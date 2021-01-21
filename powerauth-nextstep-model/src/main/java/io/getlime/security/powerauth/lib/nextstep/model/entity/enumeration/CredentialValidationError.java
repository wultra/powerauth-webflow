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
 * Enum representing a credential validation error.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public enum CredentialValidationError {

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
     * Credential has invalid characters.
     */
    CREDENTIAL_CONTAINS_INVALID_CHARACTERS,

    /**
     * Credential history check failed.
     */
    CREDENTIAL_HISTORY_CHECK_FAILED

}
