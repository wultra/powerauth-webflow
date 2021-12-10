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

package io.getlime.security.powerauth.app.tppengine.model.request;

/**
 * Request for removing a consent (with provided ID) to third party provider (with given ID).
 *
 * @author Petr Dvorak, petr@wultra.com
 */
public class RemoveConsentRequest {

    /**
     * User ID.
     */
    private String userId;

    /**
     * Consent ID.
     */
    private String consentId;

    /**
     * TPP app client ID.
     */
    private String clientId;

    /**
     * (optional) External identification of the change.
     */
    private String externalId;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getConsentId() {
        return consentId;
    }

    public void setConsentId(String consentId) {
        this.consentId = consentId;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }
}