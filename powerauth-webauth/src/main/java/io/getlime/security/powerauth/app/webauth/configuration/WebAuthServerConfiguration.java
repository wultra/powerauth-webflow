package io.getlime.security.powerauth.app.webauth.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for the Web Auth application, see default values in application.properties.
 *
 * @author Roman Strobl
 */
@Configuration
public class WebAuthServerConfiguration {

    /**
     * Credential server service URL.
     */
    @Value("${powerauth.credentials.service.url}")
    private String credentialServerServiceUrl;

    /**
     * Next step server service URL.
     */
    @Value("${powerauth.nextstep.service.url}")
    private String nextstepServiceUrl;

    /**
     * Dynamic URL for CSS stylesheet to allow external styling of Web Auth UI.
     */
    @Value("${powerauth.webauth.stylesheet.url}")
    private String stylesheetUrl;

    /**
     * Get the Credential server service URL.
     * @return
     */
    public String getCredentialServerServiceUrl() {
        return credentialServerServiceUrl;
    }

    /**
     * Get the Next step server service URL.
     * @return
     */
    public String getNextstepServiceUrl() {
        return nextstepServiceUrl;
    }

    /**
     * Get the dynamic URL for CSS stylesheet to allow external styling of Web Auth UI.
     * @return
     */
    public String getStylesheetUrl() {
        return stylesheetUrl;
    }

}
