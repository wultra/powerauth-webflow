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
package io.getlime.security.powerauth.app.webauth.model.entity.authentication;

import io.getlime.security.powerauth.app.webauth.model.entity.WebSocketJsonMessage;

/**
 * Model for a display login from response sent to the client.
 *
 * @author Roman Strobl
 */
public class DisplayLoginFormResponse extends WebSocketJsonMessage {

    private String operationId;
    private boolean showCaptcha;
    private String message;

    /**
     * Empty constructor.
     */
    public DisplayLoginFormResponse() {
    }

    /**
     * Constructor with all parameters for convenience.
     * @param sessionId websocket sessionId
     * @param operationId operation id
     * @param message message to display to the user
     * @param showCaptcha should the UI show captcha
     */
    public DisplayLoginFormResponse(String sessionId, String operationId, String message, boolean showCaptcha) {
        this.action = WebAuthAction.DISPLAY_LOGIN_FORM;
        this.sessionId = sessionId;
        this.operationId = operationId;
        this.message = message;
        this.showCaptcha = showCaptcha;
    }

    /**
     * Sets the message displayed to the user.
     * @param message message to set.
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Gets the operation id.
     * @return operation id
     */
    public String getOperationId() {
        return operationId;
    }

    /**
     * Gets whether to show the captcha.
     * @return whether to show captcha
     */
    public boolean getShowCaptcha() {
        return showCaptcha;
    }

    /**
     * Gets the message to be displayed to the user.
     * @return message to show
     */
    public String getMessage() {
        return message;
    }

}
