package io.getlime.security.powerauth.app.webauth.authentication.configuration;

import io.getlime.security.powerauth.lib.credentials.client.CredentialStoreClient;
import io.getlime.security.powerauth.lib.nextstep.client.NextStepClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Petr Dvorak, petr@lime-company.eu
 */
@Configuration
public class WebAuthServicesConfiguration {

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

    @Bean
    public CredentialStoreClient defaultCredentialStoreClient() {
        return new CredentialStoreClient(credentialServerServiceUrl);
    }

    @Bean
    public NextStepClient defaultNextStepClient() {
        return new NextStepClient(nextstepServiceUrl);
    }

}
