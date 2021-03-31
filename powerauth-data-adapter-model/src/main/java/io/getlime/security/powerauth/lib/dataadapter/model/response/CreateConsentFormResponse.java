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