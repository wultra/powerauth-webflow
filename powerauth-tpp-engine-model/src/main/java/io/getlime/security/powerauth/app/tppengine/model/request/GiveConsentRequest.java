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

import java.util.Map;

/**
 * Request for giving a consent (with provided ID) to third party provider (with given ID).
 *
 * @author Petr Dvorak, petr@wultra.com
 */
public class GiveConsentRequest {

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
     * (optional) External ID, usually associated with operation ID.
     */
    private String externalId;

    /**
     * Consent parameters, to be filled into the consent text.
     */
    private Map<String, String> parameters;

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

    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }
}
