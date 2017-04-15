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
package io.getlime.security.powerauth.app.webauth.model.entity.messages;

import io.getlime.security.powerauth.app.webauth.model.entity.WebSocketJsonMessage;

/**
 * Models a display message response sent to the client.
 *
 * @author Roman Strobl
 */
public class DisplayMessageResponse extends WebSocketJsonMessage {

    private WebAuthMessageType messageType;
    private String text;

    public DisplayMessageResponse() {
    }

    public DisplayMessageResponse(String sessionId, WebAuthMessageType messageType, String text) {
        this.action = WebAuthAction.DISPLAY_MESSAGE;
        this.sessionId = sessionId;
        this.messageType = messageType;
        this.text = text;
    }

    public WebAuthMessageType getMessageType() {
        return messageType;
    }

    public String getText() {
        return text;
    }

}
