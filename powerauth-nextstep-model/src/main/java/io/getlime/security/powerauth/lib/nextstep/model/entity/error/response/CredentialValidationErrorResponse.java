/*
 * PowerAuth Web Flow and related software components
 * Copyright (C) 2025 Wultra s.r.o.
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

package io.getlime.security.powerauth.lib.nextstep.model.entity.error.response;

import io.getlime.core.rest.model.base.response.ObjectResponse;
import io.getlime.security.powerauth.lib.nextstep.model.entity.error.CredentialValidationError;
import jakarta.validation.constraints.NotNull;

/**
 * Error response wrapping the {@link CredentialValidationError}.
 *
 * @author Jan Pesek, jan.pesek@wultra.com
 */
public class CredentialValidationErrorResponse extends ObjectResponse<CredentialValidationError> {

    public CredentialValidationErrorResponse(@NotNull final CredentialValidationError responseObject) {
        super("ERROR", responseObject);
    }

}
