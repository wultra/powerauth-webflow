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
package io.getlime.security.powerauth.app.webauth.model.entity;

/**
 * Parent class for all Web Socket messages.
 *
 * @author Roman Strobl
 */
public class WebSocketJsonMessage {

    /**
     * Supported actions for different use cases.
     *
     * TODO - split actions to requests and responses.
     */
    public enum WebAuthAction {
        REGISTER,
        REGISTRATION_CONFIRM,
        DISPLAY_LOGIN_FORM,
        LOGIN_CONFIRM,
        LOGIN_CANCEL,
        DISPLAY_PAYMENT_INFO,
        PAYMENT_CONFIRM,
        PAYMENT_CANCEL,
        DISPLAY_PAYMENT_AUTHORIZATION_FORM,
        PAYMENT_AUTHORIZATION_CONFIRM,
        PAYMENT_AUTHORIZATION_CANCEL,
        DISPLAY_MESSAGE,
        TERMINATE,
        TERMINATE_REDIRECT
    }

    /**
     * Action to perform.
     */
    protected WebAuthAction action;
    /**
     * Websocket sessionId.
     */
    protected String sessionId;

    /**
     * Gets the action to perform.
     * @return action to perform
     */
    public WebAuthAction getAction() {
        return action;
    }

    /**
     * Gets the websocket sessionId.
     * @return websocket sessionId.
     */
    public String getSessionId() {
        return sessionId;
    }

}
