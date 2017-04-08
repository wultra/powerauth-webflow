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

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Roman Strobl
 */
public class WebSocketJsonMessage {

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

    protected WebAuthAction action;
    protected String sessionId;

    private ObjectMapper mapper = new ObjectMapper();

    public WebAuthAction getAction() {
        return action;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String toJson() {
        try {
            return mapper.writeValueAsString(this);
        } catch (Exception ex) {
            return null;
        }
    }

    @Override
    public String toString() {
        return toJson();
    }
}
