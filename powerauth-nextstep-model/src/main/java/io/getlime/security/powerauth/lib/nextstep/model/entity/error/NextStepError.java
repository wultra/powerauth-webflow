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
package io.getlime.security.powerauth.lib.nextstep.model.entity.error;

import io.getlime.core.rest.model.base.entity.Error;

import java.io.Serializable;

/**
 * Base error class for Next Step client.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class NextStepError extends Error implements Serializable {

    private static final long serialVersionUID = -4275815737896000891L;

    /**
     * Response codes for different failures.
     */
    public class Code extends Error.Code {
        public static final String NEXT_STEP_CLIENT_ERROR = "NEXT_STEP_CLIENT_ERROR";
        public static final String REMOTE_ERROR = "REMOTE_ERROR";
        public static final String COMMUNICATION_ERROR = "COMMUNICATION_ERROR";
    }

    /**
     * Default constructor.
     */
    public NextStepError() {
        super();
    }

    /**
     * Constructor accepting code and message.
     *
     * @param code    Error code.
     * @param message Error message.
     */
    public NextStepError(String code, String message) {
        super(code, message);
    }
}