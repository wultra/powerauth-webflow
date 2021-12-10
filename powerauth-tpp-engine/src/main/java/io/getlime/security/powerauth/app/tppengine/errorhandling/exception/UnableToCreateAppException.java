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

package io.getlime.security.powerauth.app.tppengine.errorhandling.exception;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Petr Dvorak, petr@wultra.com
 */
public class UnableToCreateAppException extends Exception {

    private List<String> errors;

    public UnableToCreateAppException(List<String> errors) {
        this.errors = new ArrayList<>(errors);
    }

    public UnableToCreateAppException(String message, List<String> errors) {
        super(message);
        this.errors = new ArrayList<>(errors);
    }

    public List<String> getErrors() {
        return errors;
    }
}
