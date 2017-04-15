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
package io.getlime.security.powerauth.app.webauth.model.entity.registration;

import io.getlime.security.powerauth.app.webauth.model.entity.WebSocketJsonMessage;

/**
 * Models the session termination response sent to the client.
 *
 * @author Roman Strobl
 */
public class TerminateSessionResponse extends WebSocketJsonMessage {

    /**
     * Empty constructor.
     */
    public TerminateSessionResponse() {
    }

    /**
     * Constructor with sessionId parameter for convenience.
     * @param sessionId websocket sessionId
     */
    public TerminateSessionResponse(String sessionId) {
        this.action = WebAuthAction.TERMINATE;
        this.sessionId = sessionId;
    }

}
