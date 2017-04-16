/*
 * Copyright 2016 Lime - HighTech Solutions s.r.o.
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
package io.getlime.security.powerauth.app.webauth.model.entity.authorization;

import io.getlime.security.powerauth.app.webauth.model.entity.WebSocketJsonMessage;

/**
 * Models an authorization request from client. Authorization code is optional.
 *
 * @author Roman Strobl
 */
public class AuthorizationRequest extends WebSocketJsonMessage {

    private String operationId;
    private String authorizationCode;

    /**
     * Empty constructor.
     */
    public AuthorizationRequest() {
    }

    /**
     * Constructor with both parameters for convenience.
     * @param operationId operation id
     * @param authorizationCode authorization code for this operation
     */
    public AuthorizationRequest(String operationId, String authorizationCode) {
        this.action = WebAuthAction.PAYMENT_AUTHORIZATION_CONFIRM;
        this.operationId = operationId;
        this.authorizationCode = authorizationCode;
    }

    /**
     * Gets the operation id.
     * @return operation id
     */
    public String getOperationId() {
        return operationId;
    }

    /**
     * Gets the authorization code.
     * @return authorization code
     */
    public String getAuthorizationCode() {
        return authorizationCode;
    }

}
