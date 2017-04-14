package io.getlime.security.powerauth.app.webauth.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * @author Roman Strobl
 */
@Configuration
public class WebAuthServerConfiguration {

    @Value("${powerauth.credentialServer.service.url}")
    private String credentialServerServiceUrl;

    @Value("${powerauth.nextstep.service.url}")
    private String nextstepServiceUrl;

    public String getCredentialServerServiceUrl() {
        return credentialServerServiceUrl;
    }

    public String getNextstepServiceUrl() {
        return nextstepServiceUrl;
    }

}
