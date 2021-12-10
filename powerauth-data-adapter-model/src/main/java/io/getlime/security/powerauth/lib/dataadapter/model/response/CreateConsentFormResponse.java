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

package io.getlime.security.powerauth.lib.dataadapter.model.response;

import io.getlime.security.powerauth.lib.dataadapter.model.entity.ConsentOption;

import java.util.ArrayList;
import java.util.List;

/**
 * Response with OAuth 2.0 consent form contents.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class CreateConsentFormResponse {

    private String consentHtml;
    private final List<ConsentOption> options;

    /**
     * Default constructor.
     */
    public CreateConsentFormResponse() {
        options = new ArrayList<>();
    }

    /**
     * Constructor with consent form details.
     * @param consentHtml Consent text in HTML.
     * @param options Consent options.
     */
    public CreateConsentFormResponse(String consentHtml, List<ConsentOption> options) {
        this.consentHtml = consentHtml;
        this.options = options;
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