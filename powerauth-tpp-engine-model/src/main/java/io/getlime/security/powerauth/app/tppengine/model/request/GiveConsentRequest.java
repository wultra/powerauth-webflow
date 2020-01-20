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
