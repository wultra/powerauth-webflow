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
 * Response after saving the OAuth 2.0 consent form options.
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
