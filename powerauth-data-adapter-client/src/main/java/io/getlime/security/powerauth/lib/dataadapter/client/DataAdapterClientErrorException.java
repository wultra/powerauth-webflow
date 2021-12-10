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

package io.getlime.security.powerauth.lib.dataadapter.client;

import io.getlime.security.powerauth.lib.dataadapter.model.entity.DataAdapterError;

/**
 * Exception thrown from the data adapter in case of an error.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
public class DataAdapterClientErrorException extends Exception {

    private DataAdapterError error;

    /**
     * Default constructor.
     */
    public DataAdapterClientErrorException() {
    }

    /**
     * Constructor with cause.
     * @param cause Exception cause.
     */
    public DataAdapterClientErrorException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructor with cause and error.
     * @param cause Exception cause.
     * @param error Data adapter error.
     */
    public DataAdapterClientErrorException(Throwable cause, DataAdapterError error) {
        super(cause);
        this.error = error;
    }

    /**
     * Get data adapter error.
     * @return Data adapter error.
     */
    public DataAdapterError getError() {
        return error;
    }
}
