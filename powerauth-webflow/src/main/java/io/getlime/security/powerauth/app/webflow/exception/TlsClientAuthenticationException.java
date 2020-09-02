/*
 * Copyright 2020 Wultra s.r.o.
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
package io.getlime.security.powerauth.app.webflow.exception;

import io.getlime.security.powerauth.lib.webflow.authentication.exception.AuthStepException;

/**
 * Authentication exception used when TLS client authentication fails.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class TlsClientAuthenticationException extends AuthStepException {

    private static final long serialVersionUID = 210138878105518589L;

    /**
     * Constructor with error message.
     * @param message Error message.
     */
    public TlsClientAuthenticationException(String message) {
        super(message, "clientCertificate.failed");
    }
}
