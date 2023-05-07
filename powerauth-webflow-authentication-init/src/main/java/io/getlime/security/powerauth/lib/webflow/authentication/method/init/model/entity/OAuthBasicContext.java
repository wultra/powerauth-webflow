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

package io.getlime.security.powerauth.lib.webflow.authentication.method.init.model.entity;

/**
 * Object for containing the basic OAuth 2.1 dance context.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
public class OAuthBasicContext {

    /**
     * OAuth 2.1 client ID.
     */
    private String clientId;

    /**
     * OAuth 2.1 scopes.
     */
    private String[] scopes;

    /**
     * Get OAuth 2.1 client ID.
     * @return OAuth 2.1 client ID.
     */
    public String getClientId() {
        return clientId;
    }

    /**
     * Set OAuth 2.1 client ID.
     * @param clientId OAuth 2.1 client ID.
     */
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    /**
     * Get OAuth 2.1 scopes.
     * @return OAuth 2.1 scopes.
     */
    public String[] getScopes() {
        return scopes;
    }

    /**
     * Set OAuth 2.1 scopes.
     * @param scopes OAuth 2.1 scopes.
     */
    public void setScopes(String[] scopes) {
        this.scopes = scopes;
    }
}
