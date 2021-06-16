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
