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

package io.getlime.security.powerauth.lib.tpp.engine.client;

import io.getlime.security.powerauth.app.tppengine.model.entity.TppEngineError;

/**
 * Exception thrown from the data adapter in case of an error.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
public class TppEngineClientException extends Exception {

    /**
     * TPP engine error.
     */
    private TppEngineError error;

    /**
     * Default constructor.
     */
    public TppEngineClientException() {
    }

    /**
     * Constructor with cause.
     * @param cause Exception cause.
     */
    public TppEngineClientException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructor with cause and error.
     * @param cause Exception cause.
     * @param error Data adapter error.
     */
    public TppEngineClientException(Throwable cause, TppEngineError error) {
        super(cause);
        this.error = error;
    }

    /**
     * Get data adapter error.
     * @return Data adapter error.
     */
    public TppEngineError getError() {
        return error;
    }
}