/*
 * PowerAuth Web Flow and related software components
 * Copyright (C) 2021 Wultra s.r.o.
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

        /**
         * Error caused by the client.
         */
        public static final String NEXT_STEP_CLIENT_ERROR = "NEXT_STEP_CLIENT_ERROR";

        /**
         * Error caused by a remote error.
         */
        public static final String REMOTE_ERROR = "REMOTE_ERROR";

        /**
         * Communication error.
         */
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