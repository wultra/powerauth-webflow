/*
 * Copyright 2017 Wultra s.r.o.
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

package io.getlime.security.powerauth.app.webflow.demo.configuration;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.wultra.core.rest.client.base.RestClientConfiguration;
import io.getlime.security.powerauth.lib.nextstep.client.NextStepClient;
import io.getlime.security.powerauth.lib.nextstep.client.NextStepClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Demo application configuration.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
@Configuration
public class WebFlowServiceConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(WebFlowServiceConfiguration.class);

    @Value("${powerauth.webflow.service.url}")
    private String webFlowServiceUrl;

    @Value("${powerauth.webflow.service.oauth.authorizeUrl}")
    private String webFlowOAuthAuthorizeUrl;

    @Value("${powerauth.webflow.service.oauth.tokenUrl}")
    private String webFlowOAuthTokenUrl;

    @Value("${powerauth.webflow.service.oauth.clientId}")
    private String clientId;

    @Value("${powerauth.webflow.service.oauth.clientSecret}")
    private String clientSecret;

    @Value("${powerauth.nextstep.service.url}")
    private String nextstepServiceUrl;

    /**
     * Application name.
     */
    @Value("${powerauth.webflow.client.service.applicationName}")
    private String applicationName;

    /**
     * Application display name.
     */
    @Value("${powerauth.webflow.client.service.applicationDisplayName}")
    private String applicationDisplayName;

    /**
     * Application environment.
     */
    @Value("${powerauth.webflow.client.service.applicationEnvironment}")
    private String applicationEnvironment;

    /**
     * Whether invalid SSL certificates should be accepted.
     */
    @Value("${powerauth.service.ssl.acceptInvalidSslCertificate}")
    private boolean acceptInvalidSslCertificate;

    public String getWebFlowServiceUrl() {
        return webFlowServiceUrl;
    }

    public String getWebFlowOAuthAuthorizeUrl() {
        return webFlowOAuthAuthorizeUrl;
    }

    public String getWebFlowOAuthTokenUrl() {
        return webFlowOAuthTokenUrl;
    }

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    /**
     * Get application name.
     * @return Application name.
     */
    public String getApplicationName() {
        return applicationName;
    }

    /**
     * Get application display name.
     * @return Application display name.
     */
    public String getApplicationDisplayName() {
        return applicationDisplayName;
    }

    /**
     * Get application environment.
     * @return Application environment.
     */
    public String getApplicationEnvironment() {
        return applicationEnvironment;
    }

    /**
     * Construct object mapper with default configuration which allows sending empty objects and allows unknown properties.
     * @return Constructed object mapper.
     */
    private ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper;
    }

    /**
     * Default Next Step service client.
     *
     * @return Next Step service client.
     */
    @Bean
    public NextStepClient defaultNextStepClient() {
        RestClientConfiguration restClientConfiguration = new RestClientConfiguration();
        restClientConfiguration.setBaseUrl(nextstepServiceUrl);
        restClientConfiguration.setAcceptInvalidSslCertificate(acceptInvalidSslCertificate);
        restClientConfiguration.setObjectMapper(objectMapper());
        try {
            return new NextStepClient(restClientConfiguration);
        } catch (NextStepClientException ex) {
            logger.error(ex.getMessage(), ex);
            return null;
        }
    }

}
