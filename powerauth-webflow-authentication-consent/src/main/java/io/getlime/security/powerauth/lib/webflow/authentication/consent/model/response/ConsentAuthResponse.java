package io.getlime.security.powerauth.lib.webflow.authentication.consent.model.response;

import io.getlime.security.powerauth.lib.dataadapter.model.entity.ConsentOptionValidationResult;
import io.getlime.security.powerauth.lib.webflow.authentication.base.AuthStepResponse;

import java.util.ArrayList;
import java.util.List;

/**
 * Response for consent authentication.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class ConsentAuthResponse extends AuthStepResponse {

    private boolean consentValidationPassed;
    private String validationErrorMessage;
    private List<ConsentOptionValidationResult> optionValidationResults;

    /**
     * Default constructor.
     */
    public ConsentAuthResponse() {
        optionValidationResults = new ArrayList<>();
    }

    /**
     * Constructor with consent validation results.
     * @param consentValidationPassed Overall consent form validation result.
     * @param validationErrorMessage Localized HTML text which contains the error message heading above individual error messages.
     * @param optionValidationResults Consent form validation validation results.
     */
    public ConsentAuthResponse(boolean consentValidationPassed, String validationErrorMessage, List<ConsentOptionValidationResult> optionValidationResults) {
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
