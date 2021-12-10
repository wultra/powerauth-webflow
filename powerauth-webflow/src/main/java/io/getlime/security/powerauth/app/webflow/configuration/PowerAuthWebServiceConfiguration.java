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

import com.wultra.security.powerauth.client.PowerAuthClient;
import com.wultra.security.powerauth.client.model.error.PowerAuthClientException;
import com.wultra.security.powerauth.rest.client.PowerAuthRestClient;
import com.wultra.security.powerauth.rest.client.PowerAuthRestClientConfiguration;
import io.getlime.push.client.PushServerClient;
import io.getlime.push.client.PushServerClientException;
import io.getlime.security.powerauth.lib.webflow.authentication.service.SSLConfigurationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for the PowerAuth 2.0 Server connector.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
@Configuration
@ComponentScan(basePackages = {"io.getlime.security.powerauth"})
public class PowerAuthWebServiceConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(PowerAuthWebServiceConfiguration.class);

    private SSLConfigurationService sslConfigurationService;

    @Autowired
    private com.fasterxml.jackson.databind.ObjectMapper mapper;

    @Value("${powerauth.service.url}")
    private String powerAuthRestUrl;

    @Value("${powerauth.push.service.url}")
    private String powerAuthPushServiceUrl;

    @Value("${powerauth.service.security.clientToken}")
    private String clientToken;

    @Value("${powerauth.service.security.clientSecret}")
    private String clientSecret;

    @Value("${powerauth.service.ssl.acceptInvalidSslCertificate}")
    private boolean acceptInvalidSslCertificate;

    /**
     * Configuration constructor.
     * @param sslConfigurationService SSL configuration service.
     */
    @Autowired
    public PowerAuthWebServiceConfiguration(SSLConfigurationService sslConfigurationService) {
        this.sslConfigurationService = sslConfigurationService;
    }

    /**
     * Initialize PowerAuth REST client.
     * @return PowerAuth REST client.
     */
    @Bean
    public PowerAuthClient powerAuthClient() {
        PowerAuthRestClientConfiguration config = new PowerAuthRestClientConfiguration();
        config.setPowerAuthClientToken(clientToken);
        config.setPowerAuthClientSecret(clientSecret);
        config.setAcceptInvalidSslCertificate(acceptInvalidSslCertificate);
        try {
            return new PowerAuthRestClient(powerAuthRestUrl, config);
        } catch (PowerAuthClientException ex) {
            logger.error(ex.getMessage(), ex);
            return null;
        }
    }

    /**
     * Initialize PowerAuth 2.0 Push server client.
     * @return Push server client.
     */
    @Bean
    public PushServerClient pushServerClient() {
        try {
            return new PushServerClient(powerAuthPushServiceUrl);
        } catch (PushServerClientException ex) {
            logger.error(ex.getMessage(), ex);
            return null;
        }
    }

}