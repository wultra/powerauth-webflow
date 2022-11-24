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
package io.getlime.security.powerauth.lib.nextstep.model.enumeration;

/**
 * Authentication instruments used for authentication / authorization during authentication steps.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public enum AuthInstrument {

    /**
     * Credential is used for step authentication / authorization.
     */
    CREDENTIAL,

    /**
     * OTP authorization code is used for step authentication / authorization.
     */
    OTP_KEY,

    /**
     * PowerAuth mobile token application is used for step authentication / authorization.
     */
    POWERAUTH_TOKEN,

    /**
     * Hardware token is used for step authentication / authorization.
     */
    HW_TOKEN,

    /**
     * TLS client certificate is used for authentication / authorization.
     */
    CLIENT_CERTIFICATE,

    /**
     * Qualified certificate is used for authentication / authorization.
     */
    QUALIFIED_CERTIFICATE

}
