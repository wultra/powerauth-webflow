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

package io.getlime.security.powerauth.lib.webflow.authentication.method.operation.model.request;

import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthMethod;
import io.getlime.security.powerauth.lib.webflow.authentication.base.AuthStepRequest;

/**
 * Request to set chosen authentication method.
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class UpdateOperationChosenAuthMethodRequest extends AuthStepRequest {

    private AuthMethod chosenAuthMethod;

    /**
     * Default constructor.
     */
    public UpdateOperationChosenAuthMethodRequest() {
    }

    /**
     * Constructor with chosen authentication method.
     * @param chosenAuthMethod Chosen authentication method.
     */
    public UpdateOperationChosenAuthMethodRequest(AuthMethod chosenAuthMethod) {
        this.chosenAuthMethod = chosenAuthMethod;
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
