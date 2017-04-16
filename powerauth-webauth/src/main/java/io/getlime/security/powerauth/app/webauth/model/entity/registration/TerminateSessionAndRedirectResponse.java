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
 * Models a session termination response with redirect sent to the client.
 *
 * @author Roman Strobl
 */
public class TerminateSessionAndRedirectResponse extends WebSocketJsonMessage {

    private String redirectUrl;
    private int delay;

    /**
     * Empty constructor.
     */
    public TerminateSessionAndRedirectResponse() {
    }

    /**
     * Constructor with parameters for convenience.
     * @param sessionId websocket sessionId
     * @param redirectUrl URL where to redirect the user after session is terminated
     * @param delay delay of the redirect in seconds, use 0 for an immediate redirect
     */
    public TerminateSessionAndRedirectResponse(String sessionId, String redirectUrl, int delay) {
        this.action = WebAuthAction.TERMINATE_REDIRECT;
        this.sessionId = sessionId;
        this.redirectUrl = redirectUrl;
        this.delay = delay;
    }

    /**
     * Gets the redirect URL.
     *
     * @return redirect URL
     */
    public String getRedirectUrl() {
        return redirectUrl;
    }

    /**
     * Gets the redirect delay.
     * @return delay of the redirect in seconds
     */
    public int getDelay() {
        return delay;
    }

}
