/*
 * PowerAuth Web Flow and related software components
 * Copyright (C) 2019 Wultra s.r.o.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
     * App Identifier, OAuth 2.1 Client ID.
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
     * OAuth 2.1 scopes of this application.
     */
    private String[] scopes;

    /**
     * OAuth 2.1 redirect URLs.
     */
    private String[] redirectUris;

    /**
     * OAuth 2.1 client secret.
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
