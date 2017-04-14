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
 * @author Roman Strobl
 */
public class DisplayLoginFormResponse extends WebSocketJsonMessage {

    private String operationId;
    private boolean showCaptcha;
    private String message;

    public DisplayLoginFormResponse() {
    }

    public DisplayLoginFormResponse(String sessionId, String operationId, String message, boolean showCaptcha) {
        this.action = WebAuthAction.DISPLAY_LOGIN_FORM;
        this.sessionId = sessionId;
        this.operationId = operationId;
        this.message = message;
        this.showCaptcha = showCaptcha;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getOperationId() {
        return operationId;
    }

    public boolean getShowCaptcha() {
        return showCaptcha;
    }

    public String getMessage() {
        return message;
    }

}
