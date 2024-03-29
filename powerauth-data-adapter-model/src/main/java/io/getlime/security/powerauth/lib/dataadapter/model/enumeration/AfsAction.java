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
 * AFS action type enumeration.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public enum AfsAction {

    /**
     * Triggered before the login form is created.
     */
    LOGIN_INIT,

    /**
     * Triggered when user performs authentication, both successful and failed.
     */
    LOGIN_AUTH,

    /**
     * Triggered when user completes an operation or the operation is terminated for any reason.
     */
    LOGOUT,

    /**
     * Triggered before the approval form is created.
     */
    APPROVAL_INIT,

    /**
     * Triggered when the approval form is authorized.
     */
    APPROVAL_AUTH

}
