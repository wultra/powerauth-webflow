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
