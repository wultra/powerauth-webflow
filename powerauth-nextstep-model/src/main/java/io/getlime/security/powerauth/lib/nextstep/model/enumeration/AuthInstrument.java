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
package io.getlime.security.powerauth.lib.nextstep.model.enumeration;

/**
 * Authentication instruments used for authentication / authorization during authentication steps.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public enum AuthInstrument {

    /**
     * Password is used for step authentication / authorization.
     */
    PASSWORD,

    /**
     * SMS authorization code is used for step authentication / authorization.
     */
    SMS_KEY,

    /**
     * PowerAuth mobile token application is used for step authentication / authorization.
     */
    POWERAUTH_TOKEN,

    /**
     * Hardware token is used for step authentication / authorization.
     */
    HW_TOKEN,

    /**
     * TLS client certificate is used for authentication / authorization.
     */
    CLIENT_CERTIFICATE

}
