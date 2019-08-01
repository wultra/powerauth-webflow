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

/**
 * Response with information whether consent form is required.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class InitConsentFormResponse {

    private boolean shouldDisplayConsentForm;

    /**
     * Default constructor.
     */
    public InitConsentFormResponse() {
    }

    /**
     * Parameterized constructor.
     * @param shouldDisplayConsentForm Whether consent form should be displayed.
     */
    public InitConsentFormResponse(boolean shouldDisplayConsentForm) {
        this.shouldDisplayConsentForm = shouldDisplayConsentForm;
    }

    /**
     * Get whether consent form should be displayed.
     * @return Whether consent form should be displayed.
     */
    public boolean getShouldDisplayConsentForm() {
        return shouldDisplayConsentForm;
    }

    /**
     * Set whether consent form should be displayed.
     * @param shouldDisplayConsentForm Whether consent form should be displayed.
     */
    public void setShouldDisplayConsentForm(boolean shouldDisplayConsentForm) {
        this.shouldDisplayConsentForm = shouldDisplayConsentForm;
    }
}