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

package io.getlime.security.powerauth.lib.dataadapter.model.entity;

/**
 * Result of validation of option in OAuth 2.0 consent form.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class ConsentOptionValidationResult {

    private String id;
    private boolean validationPassed;
    private String errorMessage;

    /**
     * Default constructor.
     */
    public ConsentOptionValidationResult() {
    }

    /**
     * Constructor with option details.
     * @param id Option identifier.
     * @param validationPassed Result of the validation, use false value for failed validation.
     * @param errorMessage Localized HTML text of message to display for option when validation fails.
     */
    public ConsentOptionValidationResult(String id, boolean validationPassed, String errorMessage) {
        this.id = id;
        this.validationPassed = validationPassed;
        this.errorMessage = errorMessage;
    }

    /**
     * Get option identifier.
     * @return Option identifier.
     */
    public String getId() {
        return id;
    }

    /**
     * Set option identifier.
     * @param id Option identifier.
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Get result of option validation.
     * @return Result of option validation.
     */
    public boolean getValidationPassed() {
        return validationPassed;
    }

    /**
     * Set result of option validation.
     * @param validationPassed Result of option validation.
     */
    public void setValidationPassed(boolean validationPassed) {
        this.validationPassed = validationPassed;
    }

    /**
     * Get localized HTML text of message to display for option when validation fails.
     * @return Localized HTML text of message to display for option when validation fails.
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * Set localized HTML text of message to display for option when validation fails.
     * @param errorMessage Localized HTML text of message to display for option when validation fails.
     */
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
