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
     * Operation has been interrupted by another new operation.
     */
    INTERRUPTED_OPERATION,

    /**
     * Operation has timed out.
     */
    OPERATION_TIMED_OUT;

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
