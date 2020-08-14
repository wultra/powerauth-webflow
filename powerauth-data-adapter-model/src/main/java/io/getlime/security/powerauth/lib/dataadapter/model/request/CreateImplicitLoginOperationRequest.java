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

package io.getlime.security.powerauth.lib.dataadapter.model.request;

/**
 * Request object for creating an operation based on OAuth 2.0 context.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
public class CreateImplicitLoginOperationRequest {

    /**
     * OAuth 2.0 Client ID
     */
    private String clientId;

    /**
     * OAuth 2.0 Scopes
     */
    private String[] scopes;

    /**
     * Default constructor
     */
    public CreateImplicitLoginOperationRequest() {
    }

    /**
     * Full constructor.
     * @param clientId OAuth 2.0 Client ID.
     * @param scopes OAuth 2.0 Scopes.
     */
    public CreateImplicitLoginOperationRequest(String clientId, String[] scopes) {
        this.clientId = clientId;
        this.scopes = scopes;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String[] getScopes() {
        return scopes;
    }

    public void setScopes(String[] scopes) {
        this.scopes = scopes;
    }
}
