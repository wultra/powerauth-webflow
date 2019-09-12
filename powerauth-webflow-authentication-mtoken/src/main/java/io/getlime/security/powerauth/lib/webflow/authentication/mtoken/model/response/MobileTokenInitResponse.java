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
package io.getlime.security.powerauth.lib.webflow.authentication.mtoken.model.response;

import io.getlime.security.powerauth.lib.webflow.authentication.base.AuthStepResponse;

/**
 * Response to the init step of mobile token authentication.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class MobileTokenInitResponse extends AuthStepResponse {

    private boolean offlineModeAvailable;
    private boolean smsFallbackAvailable;
    private String username;


    /**
     * Whether offline mode is available.
     * @return True if offline mode is available.
     */
    public boolean isOfflineModeAvailable() {
        return offlineModeAvailable;
    }

    /**
     * Set whether offline mode is available.
     * @param offlineModeAvailable True if offline mode is available.
     */
    public void setOfflineModeAvailable(boolean offlineModeAvailable) {
        this.offlineModeAvailable = offlineModeAvailable;
    }

    /**
     * Get whether fallback to SMS authorization is enabled from mobile token.
     * @return Whether fallback to SMS authorization is enabled from mobile token.
     */
    public boolean isSmsFallbackAvailable() {
        return smsFallbackAvailable;
    }

    /**
     * Set whether fallback to SMS authorization is enabled from mobile token.
     * @param smsFallbackAvailable Whether fallback to SMS authorization is enabled from mobile token.
     */
    public void setSmsFallbackAvailable(boolean smsFallbackAvailable) {
        this.smsFallbackAvailable = smsFallbackAvailable;
    }

    /**
     * Get username for LOGIN_SCA authentication method.
     * @return Username for LOGIN_SCA authentication method.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Set username for LOGIN_SCA authentication method.
     * @param username Username for LOGIN_SCA authentication method.
     */
    public void setUsername(String username) {
        this.username = username;
    }
}
