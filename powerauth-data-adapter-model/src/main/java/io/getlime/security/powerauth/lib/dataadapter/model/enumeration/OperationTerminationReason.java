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
 * Reason why the operation was terminated.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public enum OperationTerminationReason {

    /**
     * Operation was completed successfully.
     */
    DONE,

    /**
     * Operation failed due to an error.
     */
    FAILED,

    /**
     * Operation has timed out and failed due to the timeout.
     */
    TIMED_OUT,

    /**
     * Operation was interrupted e.g. by closing the browser window or by a newer operation.
     */
    INTERRUPTED

}
