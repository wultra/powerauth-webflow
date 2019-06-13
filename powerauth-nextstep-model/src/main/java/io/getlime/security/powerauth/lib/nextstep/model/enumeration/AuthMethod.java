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
package io.getlime.security.powerauth.lib.nextstep.model.enumeration;

/**
 * Enum representing an authentication method.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
public enum AuthMethod {

    /**
     * Initialize authentication dance, anonymous auth.
     */
    INIT,

    /**
     * Directly assign a user ID with given operation.
     */
    USER_ID_ASSIGN,

    /**
     * Show operation details so that user can confirm them.
     */
    SHOW_OPERATION_DETAIL,

    /**
     * Log user in using username and password form.
     */
    USERNAME_PASSWORD_AUTH,

    /**
     * Authenticate user using PowerAuth 2.0 mobile token.
     */
    POWERAUTH_TOKEN,

    /**
     * Authenticate user using an SMS key.
     */
    SMS_KEY,

    /**
     * Confirm user consent.
     */
    CONSENT

}
