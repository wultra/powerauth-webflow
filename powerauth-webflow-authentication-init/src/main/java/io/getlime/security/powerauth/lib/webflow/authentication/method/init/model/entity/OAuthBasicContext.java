/*
 * Copyright 2019 Wultra s.r.o.
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

package io.getlime.security.powerauth.lib.webflow.authentication.method.init.model.entity;

/**
 * Object for containing the basic OAuth 2.0 dance context.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
public class OAuthBasicContext {

    /**
     * OAuth 2.0 client ID.
     */
    private String clientId;

    /**
     * OAuth 2.0 scopes.
     */
    private String[] scopes;

    /**
     * Get OAuth 2.0 client ID.
     * @return OAuth 2.0 client ID.
     */
    public String getClientId() {
        return clientId;
    }

    /**
     * Set OAuth 2.0 client ID.
     * @param clientId OAuth 2.0 client ID.
     */
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    /**
     * Get OAuth 2.0 scopes.
     * @return OAuth 2.0 scopes.
     */
    public String[] getScopes() {
        return scopes;
    }

    /**
     * Set OAuth 2.0 scopes.
     * @param scopes OAuth 2.0 scopes.
     */
    public void setScopes(String[] scopes) {
        this.scopes = scopes;
    }
}
