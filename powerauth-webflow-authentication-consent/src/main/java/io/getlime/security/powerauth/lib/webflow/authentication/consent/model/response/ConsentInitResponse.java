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
package io.getlime.security.powerauth.lib.webflow.authentication.consent.model.response;

import io.getlime.security.powerauth.lib.dataadapter.model.entity.ConsentOption;
import io.getlime.security.powerauth.lib.webflow.authentication.base.AuthStepResponse;

import java.util.ArrayList;
import java.util.List;

/**
 * Response for consent initialization.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class ConsentInitResponse extends AuthStepResponse {

    private boolean shouldDisplayConsent;
    private String consentHtml;
    private List<ConsentOption> options;

    /**
     * Default constructor.
     */
    public ConsentInitResponse() {
        options = new ArrayList<>();
    }

    /**
     * Constructor with consent form details.
     * @param shouldDisplayConsent Whether consent form should be displayed
     * @param consentHtml Consent text in HTML.
     * @param options Consent options.
     */
    public ConsentInitResponse(boolean shouldDisplayConsent, String consentHtml, List<ConsentOption> options) {
        this.shouldDisplayConsent = shouldDisplayConsent;
        this.consentHtml = consentHtml;
        this.options = options;
    }

    /**
     * Get whether consent form should be displayed.
     * @return Whether consent form should be displayed.
     */
    public boolean getShouldDisplayConsent() {
        return shouldDisplayConsent;
    }

    /**
     * Set whether consent form should be displayed.
     * @param shouldDisplayConsent Whether consent form should be displayed.
     */
    public void setShouldDisplayConsent(boolean shouldDisplayConsent) {
        this.shouldDisplayConsent = shouldDisplayConsent;
    }

    /**
     * Get consent text in HTML.
     * @return Consent text in HTML.
     */
    public String getConsentHtml() {
        return consentHtml;
    }

    /**
     * Set consent text in HTML.
     * @param consentHtml Consent text in HTML.
     */
    public void setConsentHtml(String consentHtml) {
        this.consentHtml = consentHtml;
    }

    /**
     * Get consent options.
     * @return Consent options.
     */
    public List<ConsentOption> getOptions() {
        return options;
    }
}
