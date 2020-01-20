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
    SUCCEEDED,
    FAILED,
    SKIPPED
}
