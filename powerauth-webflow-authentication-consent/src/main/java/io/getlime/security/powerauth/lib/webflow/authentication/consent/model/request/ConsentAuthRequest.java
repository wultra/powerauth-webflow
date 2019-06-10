package io.getlime.security.powerauth.lib.webflow.authentication.consent.model.request;

import io.getlime.security.powerauth.lib.dataadapter.model.entity.ConsentOption;
import io.getlime.security.powerauth.lib.webflow.authentication.base.AuthStepRequest;

import java.util.List;

/**
 * Request for OAuth 2.0 consent.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class ConsentAuthRequest extends AuthStepRequest {

    private List<ConsentOption> options;

    public ConsentAuthRequest() {
    }

    public ConsentAuthRequest(List<ConsentOption> options) {
        this.options = options;
    }

    public List<ConsentOption> getOptions() {
        return options;
    }
}
