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

import io.getlime.core.rest.model.base.entity.Error;

/**
 * Error related to consent.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
public class ConsentError extends Error {

    private static final String code = "CONSENT_ERROR";

    public ConsentError() {
        super();
        this.setCode(code);
    }

    public ConsentError(String message) {
        super(code, message);
    }
}
