/*
 * Copyright 2017 Lime - HighTech Solutions s.r.o.
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

package io.getlime.security.powerauth.lib.webauth.authentication.configuration;

import io.getlime.security.powerauth.lib.credentials.client.CredentialStoreClient;
import io.getlime.security.powerauth.lib.nextstep.client.NextStepClient;
import io.getlime.security.powerauth.lib.webauth.authentication.service.SSLConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Basic configuration class, used to configure clients to Next Step service and Credential Store.
 *
 * @author Petr Dvorak, petr@lime-company.eu
 */
@Configuration
public class WebAuthServicesConfiguration {

    private SSLConfigurationService sslConfigurationService;

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
     * Whether invalid SSL certificates should be accepted.
     */
    @Value("${powerauth.service.ssl.acceptInvalidSslCertificate}")
    private boolean acceptInvalidSslCertificate;

    @Autowired
    public WebAuthServicesConfiguration(SSLConfigurationService sslConfigurationService) {
        this.sslConfigurationService = sslConfigurationService;
    }

    /**
     * Default credential store client.
     * @return Credential store client.
     */
    @Bean
    public CredentialStoreClient defaultCredentialStoreClient() {
        CredentialStoreClient client = new CredentialStoreClient(credentialServerServiceUrl);
        // whether invalid SSL certificates should be accepted
        if (acceptInvalidSslCertificate) {
            sslConfigurationService.trustAllCertificates();
        }
        return client;
    }

    /**
     * Default Next Step service client.
     * @return Next Step service client.
     */
    @Bean
    public NextStepClient defaultNextStepClient() {
        NextStepClient client = new NextStepClient(nextstepServiceUrl);
        // whether invalid SSL certificates should be accepted
        if (acceptInvalidSslCertificate) {
            sslConfigurationService.trustAllCertificates();
        }
        return client;
    }

}
