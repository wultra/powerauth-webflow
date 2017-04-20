/*
 * Copyright 2017 Lime - HighTech Solutions s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.getlime.security.powerauth.lib.webauth.authentication.method.init.model.response;

import io.getlime.security.powerauth.lib.webauth.authentication.base.AuthStepResponse;

/**
 * Confirm registration response sent to the client.
 *
 * Basically, this class represents the newly created operation that is about to be confirmed.
 *
 * @author Roman Strobl
 * @author Petr Dvorak, petr@lime-company.eu
 */
public class InitOperationResponse extends AuthStepResponse {

    private String operationId;

    /**
     * Get operation ID.
     * @return Operation ID.
     */
    public String getOperationId() {
        return operationId;
    }

    /**
     * Set operation ID.
     * @param operationId Operation ID.
     */
    public void setOperationId(String operationId) {
        this.operationId = operationId;
    }

}
