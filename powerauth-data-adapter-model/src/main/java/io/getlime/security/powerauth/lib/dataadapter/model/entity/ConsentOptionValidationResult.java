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
