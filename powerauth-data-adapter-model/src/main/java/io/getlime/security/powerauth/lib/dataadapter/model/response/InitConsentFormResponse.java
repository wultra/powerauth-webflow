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