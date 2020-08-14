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

package io.getlime.security.powerauth.app.tppengine.model.response;

import io.getlime.security.powerauth.app.tppengine.model.entity.TppInfo;

/**
 * Response object representing TPP app details.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
public class TppAppDetailResponse {

    /**
     * App Identifier, OAuth 2.0 Client ID.
     */
    private String clientId;

    /**
     * App Name.
     */
    private String name;

    /**
     * App Description
     */
    private String description;

    /**
     * App Type.
     */
    private String appType;

    /**
     * OAuth 2.0 scopes of this application.
     */
    private String[] scopes;

    /**
     * OAuth 2.0 redirect URLs.
     */
    private String[] redirectUris;

    /**
     * OAuth 2.0 client secret.
     */
    private String clientSecret;

    /**
     * TPP information.
     */
    private TppInfo tpp;

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAppType() {
        return appType;
    }

    public void setAppType(String appType) {
        this.appType = appType;
    }

    public String[] getScopes() {
        return scopes;
    }

    public void setScopes(String[] scopes) {
        this.scopes = scopes;
    }

    public String[] getRedirectUris() {
        return redirectUris;
    }

    public void setRedirectUris(String[] redirectUris) {
        this.redirectUris = redirectUris;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public TppInfo getTpp() {
        return tpp;
    }

    public void setTpp(TppInfo tpp) {
        this.tpp = tpp;
    }
}
