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
package io.getlime.security.powerauth.lib.nextstep.model.enumeration;

/**
 * Enum representing modes for resetting soft failed attempt counters.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public enum CounterResetMode {

    /**
     * Reset soft failed attempt counters for credentials with BLOCKED_TEMPORARY status, change status to ACTIVE.
     */
    RESET_BLOCKED_TEMPORARY,

    /**
     * Reset soft failed attempt counters for credentials with ACTIVE and BLOCKED_TEMPORARY statuses, change status to ACTIVE if required.
     */
    RESET_ACTIVE_AND_BLOCKED_TEMPORARY,

}
