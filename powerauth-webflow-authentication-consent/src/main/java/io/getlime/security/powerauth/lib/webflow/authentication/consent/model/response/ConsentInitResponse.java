package io.getlime.security.powerauth.lib.webflow.authentication.consent.model.response;

import io.getlime.security.powerauth.lib.dataadapter.model.entity.ConsentOption;
import io.getlime.security.powerauth.lib.webflow.authentication.base.AuthStepResponse;

import java.util.ArrayList;
import java.util.List;

/**
 * Response for consent initialization.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class ConsentInitResponse extends AuthStepResponse {

    private String consentHtml;
    private List<ConsentOption> options;

    /**
     * Default constructor.
     */
    public ConsentInitResponse() {
        options = new ArrayList<>();
    }

    /**
     * Constructor with consent form details.
     * @param consentHtml Consent text in HTML.
     * @param options Consent options.
     */
    public ConsentInitResponse(String consentHtml, List<ConsentOption> options) {
        this.consentHtml = consentHtml;
        this.options = options;
    }

    /**
     * Get consent text in HTML.
     * @return Consent text in HTML.
     */
    public String getConsentHtml() {
        return consentHtml;
    }

    /**
     * Set consent text in HTML.
     * @param consentHtml Consent text in HTML.
     */
    public void setConsentHtml(String consentHtml) {
        this.consentHtml = consentHtml;
    }

    /**
     * Get consent options.
     * @return Consent options.
     */
    public List<ConsentOption> getOptions() {
        return options;
    }
}
