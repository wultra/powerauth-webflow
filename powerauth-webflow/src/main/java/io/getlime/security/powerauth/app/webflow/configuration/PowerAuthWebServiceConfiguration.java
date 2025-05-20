/*
 * PowerAuth Web Flow and related software components
 * Copyright (C) 2018 Wultra s.r.o.
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
package io.getlime.security.powerauth.app.webflow.configuration;

import com.wultra.core.rest.client.base.RestClientConfiguration;
import com.wultra.security.powerauth.client.PowerAuthClient;
import com.wultra.security.powerauth.client.model.error.PowerAuthClientException;
import com.wultra.security.powerauth.rest.client.PowerAuthRestClient;
import com.wultra.security.powerauth.rest.client.PowerAuthRestClientConfiguration;
import com.wultra.push.client.PushServerClient;
import com.wultra.push.client.PushServerClientException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Configuration for the PowerAuth Server connector.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
@Configuration
@ComponentScan(basePackages = {"io.getlime.security.powerauth"})
@Slf4j
public class PowerAuthWebServiceConfiguration {

    @Value("${powerauth.service.url}")
    private String powerAuthRestUrl;

    @Value("${powerauth.service.restClientConfig.responseTimeout}")
    private Duration powerAuthServiceTimeout;

    @Value("${powerauth.service.restClientConfig.maxIdleTime}")
    private Duration powerAuthServiceMaxIdleTime;

    @Value("${powerauth.service.security.clientToken}")
    private String clientToken;

    @Value("${powerauth.service.security.clientSecret}")
    private String clientSecret;

    @Value("${powerauth.service.ssl.acceptInvalidSslCertificate}")
    private boolean acceptInvalidSslCertificate;

    /**
     * Initialize PowerAuth REST client.
     * @return PowerAuth REST client.
     */
    @Bean
    public PowerAuthClient powerAuthClient() throws PowerAuthClientException {
        final PowerAuthRestClientConfiguration config = new PowerAuthRestClientConfiguration();
        config.setPowerAuthClientToken(clientToken);
        config.setPowerAuthClientSecret(clientSecret);
        config.setAcceptInvalidSslCertificate(acceptInvalidSslCertificate);
        config.setResponseTimeout(powerAuthServiceTimeout);
        config.setMaxIdleTime(powerAuthServiceMaxIdleTime);
        return new PowerAuthRestClient(powerAuthRestUrl, config);
    }

    /**
     * Initialize PowerAuth Push server client.
     * @return Push server client.
     */
    @Bean
    public PushServerClient pushServerClient(final PushServiceConfigProperties pushServiceProperties) throws PushServerClientException {
        final String url = pushServiceProperties.getUrl();
        logger.info("Configuring PushServerClient for URL: {}", url);
        final RestClientConfiguration restClientConfig = pushServiceProperties.getRestClientConfig();
        restClientConfig.setBaseUrl(url);
        return new PushServerClient(restClientConfig);
    }

}