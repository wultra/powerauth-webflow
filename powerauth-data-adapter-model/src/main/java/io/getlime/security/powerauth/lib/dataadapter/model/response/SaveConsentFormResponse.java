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
 * Response after saving the OAuth 2.1 consent form options.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class SaveConsentFormResponse {

    private boolean saveSucceeded;

    /**
     * Default constructor.
     */
    public SaveConsentFormResponse() {
    }

    /**
     * Constructor with information about save consent result.
     * @param saveSucceeded Whether consent was saved successfully.
     */
    public SaveConsentFormResponse(boolean saveSucceeded) {
        this.saveSucceeded = saveSucceeded;
    }

    /**
     * Get whether consent was saved successfully.
     * @return Whether consent was saved successfully.
     */
    public boolean isSaveSucceeded() {
        return saveSucceeded;
    }

    /**
     * Set whether cosent was saved successfully.
     * @param saveSucceeded Whether consent was saved successfully.
     */
    public void setSaveSucceeded(boolean saveSucceeded) {
        this.saveSucceeded = saveSucceeded;
    }
}
