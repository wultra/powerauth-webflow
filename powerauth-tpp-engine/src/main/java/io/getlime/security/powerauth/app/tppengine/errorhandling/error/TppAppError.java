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

package io.getlime.security.powerauth.app.tppengine.errorhandling.error;

import com.wultra.core.rest.model.base.entity.Error;

import java.util.ArrayList;
import java.util.List;

/**
 * Error related to consent.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
public class TppAppError extends Error {

    private static final String code = "TPP_APP_ERROR";

    private List<String> causes;

    public TppAppError() {
        super();
        this.setCode(code);
    }

    public TppAppError(String message) {
        super(code, message);
    }

    public TppAppError(String message, List<String> causes) {
        super(code, message);
        this.causes = new ArrayList<>(causes);
    }

    public List<String> getCauses() {
        return causes;
    }
}
