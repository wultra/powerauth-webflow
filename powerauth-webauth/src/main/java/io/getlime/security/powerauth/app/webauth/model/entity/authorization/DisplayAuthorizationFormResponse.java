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
 * Models a display authorization form response sent to the client.
 *
 * @author Roman Strobl
 */
public class DisplayAuthorizationFormResponse extends WebSocketJsonMessage {

    private String operationId;

    /**
     * Empty constructor.
     */
    public DisplayAuthorizationFormResponse() {
    }

    /**
     * Constructor with both parameters for convenience.
     * @param sessionId websocket session id
     * @param operationId operation id
     */
    public DisplayAuthorizationFormResponse(String sessionId, String operationId) {
        this.action = WebAuthAction.DISPLAY_PAYMENT_AUTHORIZATION_FORM;
        this.sessionId = sessionId;
        this.operationId = operationId;
    }

    /**
     * Gets the operation id.
     * @return operation id
     */
    public String getOperationId() {
        return operationId;
    }

}
