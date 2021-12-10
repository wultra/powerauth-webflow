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
 * Enum representing a result of a previous authentication step.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
public enum AuthStepResult {

    /**
     * Authentication was successful, user completed the step and can proceed to the next step.
     */
    CONFIRMED,

    /**
     * User canceled the authentication.
     */
    CANCELED,

    /**
     * Authentication failed, user cannot proceed to the next step.
     */
    AUTH_FAILED,

    /**
     * Authentication method failed completely, user cannot proceed to the next step and this authentication method
     * should no longer be used.
     */
    AUTH_METHOD_FAILED,

    /**
     * Authentication method for the next authentication step was chosen by the user.
     */
    AUTH_METHOD_CHOSEN,

    /**
     * User requested downgrade of the authentication method.
     */
    AUTH_METHOD_DOWNGRADE

}
