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
package io.getlime.security.powerauth.lib.webflow.authentication.method.init.model.response;

import io.getlime.security.powerauth.lib.webflow.authentication.base.AuthStepResponse;

/**
 * Confirm registration response sent to the client.
 * <p>
 * Basically, this class represents the newly created operation that is about to be confirmed.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 * @author Petr Dvorak, petr@wultra.com
 */
public class InitOperationResponse extends AuthStepResponse {

    private String operationHash;

    public String getOperationHash() {
        return operationHash;
    }

    public void setOperationHash(String operationHash) {
        this.operationHash = operationHash;
    }
}
