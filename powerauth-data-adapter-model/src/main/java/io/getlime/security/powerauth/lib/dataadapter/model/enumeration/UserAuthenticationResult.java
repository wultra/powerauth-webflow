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
 * Result of username and password verification.
 *
 * <ul>
 * <li>SUCCEEDED - user ID and password have been verified and verification succeeded.</li>
 * <li>FAILED - user ID and password have been verified and verification failed.</li>
 * <li>SKIPPED - verification of user ID and password has not been performed at all.</li>
 * </ul>
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public enum UserAuthenticationResult {

    /**
     * Authentication succeeded.
     */
    SUCCEEDED,

    /**
     * Authentication failed.
     */
    FAILED,

    /**
     * Authentication was not performed.
     */
    SKIPPED
}
