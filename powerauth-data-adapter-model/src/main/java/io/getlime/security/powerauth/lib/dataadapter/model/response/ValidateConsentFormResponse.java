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

import io.getlime.security.powerauth.lib.dataadapter.model.entity.ConsentOptionValidationResult;

import java.util.ArrayList;
import java.util.List;

/**
 * Response with OAuth 2.0 consent form validation result.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class ValidateConsentFormResponse {

    private boolean consentValidationPassed;
    private String validationErrorMessage;
    private List<ConsentOptionValidationResult> optionValidationResults;

    /**
     * Default constructor.
     */
    public ValidateConsentFormResponse() {
        optionValidationResults = new ArrayList<>();
    }

    /**
     * Constructor with consent validation results.
     * @param consentValidationPassed Overall consent form validation result.
     * @param validationErrorMessage Localized HTML text which contains the error message heading above individual error messages.
     * @param optionValidationResults Consent form validation validation results.
     */
    public ValidateConsentFormResponse(boolean consentValidationPassed, String validationErrorMessage, List<ConsentOptionValidationResult> optionValidationResults) {
        this.consentValidationPassed = consentValidationPassed;
        this.validationErrorMessage = validationErrorMessage;
        this.optionValidationResults = optionValidationResults;
    }

    /**
     * Get overall consent form validation result.
     * @return Overall consent form validation result.
     */
    public boolean getConsentValidationPassed() {
        return consentValidationPassed;
    }

    /**
     * Set overall consent form validation result.
     * @param consentValidationPassed Overall consent form validation result.
     */
    public void setConsentValidationPassed(boolean consentValidationPassed) {
        this.consentValidationPassed = consentValidationPassed;
    }

    /**
     * Get localized HTML text which contains the error message heading above individual error messages.
     * @return Error message heading.
     */
    public String getValidationErrorMessage() {
        return validationErrorMessage;
    }

    /**
     * Set localized HTML text which contains the error message heading above individual error messages.
     * @param validationErrorMessage Error message heading.
     */
    public void setValidationErrorMessage(String validationErrorMessage) {
        this.validationErrorMessage = validationErrorMessage;
    }

    /**
     * Get validation results for individual consent form options.
     * @return Validation results for individual consent form options.
     */
    public List<ConsentOptionValidationResult> getOptionValidationResults() {
        return optionValidationResults;
    }

    /**
     * Set validation results for individual consent form options.
     * @param optionValidationResults Validation results for individual consent form options.
     */
    public void setOptionValidationResults(List<ConsentOptionValidationResult> optionValidationResults) {
        this.optionValidationResults = optionValidationResults;
    }
}