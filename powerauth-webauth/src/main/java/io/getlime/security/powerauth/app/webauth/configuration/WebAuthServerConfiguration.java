package io.getlime.security.powerauth.app.webauth.configuration;

import io.getlime.security.powerauth.lib.credentials.client.CredentialStoreClient;
import io.getlime.security.powerauth.lib.nextstep.client.NextStepClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for the Web Auth application, see default values in application.properties.
 *
 * @author Roman Strobl
 */
@Configuration
@ComponentScan(basePackages = {"io.getlime.security.powerauth"})
public class WebAuthServerConfiguration {

    /**
     * Dynamic URL for CSS stylesheet to allow external styling of Web Auth UI.
     */
    @Value("${powerauth.webauth.page.stylesheet.url}")
    private String stylesheetUrl;

    /**
     * Dynamic page title
     */
    @Value("${powerauth.webauth.page.title}")
    private String pageTitle;

    /**
     * Get the dynamic URL for CSS stylesheet to allow external styling of Web Auth UI.
     * @return Dynamic URL for CSS stylesheet.
     */
    public String getStylesheetUrl() {
        return stylesheetUrl;
    }

    /**
     * Get dynamic page title.
     * @return Page title.
     */
    public String getPageTitle() {
        return pageTitle;
    }
}
