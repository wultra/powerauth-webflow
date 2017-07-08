/*
 * Copyright 2017 Lime - HighTech Solutions s.r.o.
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
 * Enum representing a result of a previous authentication step.
 *
 * @author Petr Dvorak, petr@lime-company.eu
 */
public enum AuthStepResult {

    /**
     * Authentication was successful, user completed the step and can proceed to the next step.
     */
    CONFIRMED,

    /**
     * User cancelled the authentication, user may be redirected to the fallback authentication method.
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
    AUTH_METHOD_FAILED
}
