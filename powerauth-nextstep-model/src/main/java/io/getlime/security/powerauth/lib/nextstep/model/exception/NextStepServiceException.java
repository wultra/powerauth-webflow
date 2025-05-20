/*
 * PowerAuth Web Flow and related software components
 * Copyright (C) 2017 Wultra s.r.o.
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

package io.getlime.security.powerauth.lib.nextstep.model.exception;

import com.wultra.core.rest.model.base.entity.Error;

/**
 * Exception for failures in Next Step service processing.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
public class NextStepServiceException extends Exception {

    /**
     * Error detail.
     */
    private Error error;

    /**
     * Constructor with error message.
     * @param message Error message.
     */
    public NextStepServiceException(String message) {
        super(message);
    }

    /**
     * Constructor with cause.
     * @param cause Original exception.
     */
    public NextStepServiceException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructor with cause and error details.
     * @param cause Original exception.
     * @param error Object with error information.
     */
    public NextStepServiceException(Throwable cause, Error error) {
        super(cause);
        this.error = error;
    }

    /**
     * Get error detail information.
     * @return Error detail information.
     */
    public Error getError() {
        return error;
    }
}
