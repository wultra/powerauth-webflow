/*
 * Copyright 2019 Wultra s.r.o.
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

package io.getlime.security.powerauth.app.tppengine.model.enumeration;

/**
 * Enum representing a status change of a consent.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
public enum ConsentChange {

    /**
     * Consent was granted.
     */
    APPROVE,

    /**
     * Consent was prolonged.
     */
    PROLONG,

    /**
     * Consent was rejected.
     */
    REJECT

}
