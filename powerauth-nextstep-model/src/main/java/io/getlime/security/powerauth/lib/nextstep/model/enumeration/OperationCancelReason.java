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
 * Enum representing reason for canceling the operation.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public enum OperationCancelReason {

    /**
     * Unknown reason.
     */
    UNKNOWN,

    /**
     * Operation data is incorrect.
     */
    INCORRECT_DATA,

    /**
     * Operation was not expected.
     */
    UNEXPECTED_OPERATION,

    /**
     * Activation method is not available (e.g. activation was blocked).
     */
    AUTH_METHOD_NOT_AVAILABLE,

    /**
     * Operation has been interrupted by another new operation or by closing browser tab / window.
     */
    INTERRUPTED_OPERATION,

    /**
     * Unexpected error occurred during execution of the operation.
     */
    UNEXPECTED_ERROR,

    /**
     * Operation has timed out.
     */
    TIMED_OUT_OPERATION;

    /**
     * Convert String value to OperationCancelReason enum value.
     * @param value String value of cancellation reason.
     * @return OperationCancelReason enum value.
     */
    public static OperationCancelReason fromString(String value) {
        // resolve value by case independent String comparison
        for (OperationCancelReason reason : OperationCancelReason.values()) {
            if (reason.name().equalsIgnoreCase(value)) {
                return reason;
            }
        }
        // graceful fallback for unknown values
        return UNKNOWN;
    }
}
