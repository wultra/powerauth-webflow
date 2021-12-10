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

package io.getlime.security.powerauth.lib.webflow.authentication.consent.exception;

import io.getlime.security.powerauth.lib.dataadapter.model.entity.ConsentOptionValidationResult;
import io.getlime.security.powerauth.lib.webflow.authentication.exception.AuthStepException;

import java.util.List;

/**
 * Consent validation failed exception.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class ConsentValidationFailedException extends AuthStepException {

    private String errorMessage;
    private List<ConsentOptionValidationResult> optionValidationResults;

    /**
     * Exception constructor.
     * @param message Exception message.
     * @param messageId Message ID for localization.
     */
    public ConsentValidationFailedException(String message, String messageId) {
        super(message, messageId);
    }

    /**
     * Get validaton error message.
     * @return Validation error message.
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * Set validation error message.
     * @param errorMessage Validation error message.
     */
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    /**
     * Get list of consent validation results.
     * @return List of consent validation results.
     */
    public List<ConsentOptionValidationResult> getOptionValidationResults() {
        return optionValidationResults;
    }

    /**
     * Set list of consent validation results.
     * @param optionValidationResults List of consent validation results.
     */
    public void setOptionValidationResults(List<ConsentOptionValidationResult> optionValidationResults) {
        this.optionValidationResults = optionValidationResults;
    }
}
