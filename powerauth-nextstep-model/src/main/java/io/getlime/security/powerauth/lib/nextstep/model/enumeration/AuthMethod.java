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
     * Authorize step using PowerAuth mobile token.
     */
    POWERAUTH_TOKEN,

    /**
     * Authorize step using an SMS key.
     */
    SMS_KEY,

    /**
     * Authenticate step using an OTP code.
     */
    OTP_CODE,

    /**
     * Confirm user consent.
     */
    CONSENT,

    /**
     * SCA login.
     */
    LOGIN_SCA,

    /**
     * SCA approval.
     */
    APPROVAL_SCA

}
