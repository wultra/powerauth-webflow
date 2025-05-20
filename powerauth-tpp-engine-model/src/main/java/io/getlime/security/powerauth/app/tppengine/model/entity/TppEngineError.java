/*
 * PowerAuth Web Flow and related software components
 * Copyright (C) 2019 Wultra s.r.o.
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

package io.getlime.security.powerauth.app.tppengine.model.entity;

import io.getlime.core.rest.model.base.entity.Error;

/**
 *
 * Error model, used to represent error responses.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
public class TppEngineError extends Error {

    private String[] causes;

    /**
     * Response codes for different authentication failures.
     */
    public static class Code extends Error.Code {
        public static final String REMOTE_ERROR = "REMOTE_ERROR";
        public static final String TPP_ENGINE_CLIENT_ERROR = "TPP_ENGINE_CLIENT_ERROR";
        public static final String COMMUNICATION_ERROR = "COMMUNICATION_ERROR";
    }

    /**
     * Default constructor.
     */
    public TppEngineError() {
        super();
    }

    /**
     * Constructor accepting code and message.
     *
     * @param code    Error code.
     * @param message Error message.
     */
    public TppEngineError(String code, String message) {
        super(code, message);
    }

    /**
     * Set the causes of this error.
     * @param causes Array of causes for the error.
     */
    public void setCauses(String[] causes) {
        this.causes = causes;
    }

    /**
     * Get the array of causes for this error.
     * @return Array of causes for the error.
     */
    public String[] getCauses() {
        return causes;
    }
}
