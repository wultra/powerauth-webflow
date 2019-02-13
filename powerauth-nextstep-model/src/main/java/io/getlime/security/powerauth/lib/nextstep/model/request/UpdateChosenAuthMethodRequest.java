/*
 * Copyright 2017 Wultra s.r.o.
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
package io.getlime.security.powerauth.lib.nextstep.model.request;

import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthMethod;

/**
 * Request object used for updating chosen authentication method.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class UpdateChosenAuthMethodRequest {

    private String operationId;
    private AuthMethod chosenAuthMethod;

    /**
     * Default constructor.
     */
    public UpdateChosenAuthMethodRequest() {
    }

    /**
     * Constructor with operation ID and chosen authentication method.
     * @param operationId Operation ID.
     * @param chosenAuthMethod Chosen authentication method.
     */
    public UpdateChosenAuthMethodRequest(String operationId, AuthMethod chosenAuthMethod) {
        this.operationId = operationId;
        this.chosenAuthMethod = chosenAuthMethod;
    }

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

    /**
     * Get chosen authentication method.
     * @return Chosen authentication method.
     */
    public AuthMethod getChosenAuthMethod() {
        return chosenAuthMethod;
    }

    /**
     * Set chosen authentication method.
     * @param chosenAuthMethod Chosen authentication method.
     */
    public void setChosenAuthMethod(AuthMethod chosenAuthMethod) {
        this.chosenAuthMethod = chosenAuthMethod;
    }
}
