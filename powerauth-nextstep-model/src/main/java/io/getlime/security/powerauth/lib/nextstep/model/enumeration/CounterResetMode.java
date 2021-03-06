/*
 * Copyright 2021 Wultra s.r.o.
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
