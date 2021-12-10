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
 * Result of SMS authorization code verification.
 *
 * <ul>
 * <li>SUCCEEDED - SMS authorization code has been verified and verification succeeded.</li>
 * <li>FAILED - SMS authorization code has been verified and verification failed.</li>
 * <li>SKIPPED - SMS authorization code verification has not been performed at all.</li>
 * </ul>
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public enum SmsAuthorizationResult {

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
