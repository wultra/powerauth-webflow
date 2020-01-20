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
package io.getlime.security.powerauth.lib.webflow.authentication.model.response;

import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthResult;

/**
 * Authorization response object for sending result of an authorization via WebSockets.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class WebSocketAuthorizationResponse {

    private String webSocketId;
    private AuthResult authResult;

    public String getWebSocketId() {
        return webSocketId;
    }

    public void setWebSocketId(String webSocketId) {
        this.webSocketId = webSocketId;
    }

    public AuthResult getAuthResult() {
        return authResult;
    }

    public void setAuthResult(AuthResult authResult) {
        this.authResult = authResult;
    }

}
