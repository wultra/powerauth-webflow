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
