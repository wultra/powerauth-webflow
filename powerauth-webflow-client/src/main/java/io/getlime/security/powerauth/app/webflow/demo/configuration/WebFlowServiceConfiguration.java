/*
 * PowerAuth Web Flow and related software components
 * Copyright (C) 2017 Wultra s.r.o.
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

package io.getlime.security.powerauth.app.webflow.demo.configuration;

import com.fasterxml.jackson.databind.DeserializationFeature;
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

    /**
     * OAuth 2.1 client registration ID.
     */
    @Value("${powerauth.webflow.service.oauth2.client.registrationId}")
    private String registrationId;

    /**
     * OAuth 2.1 client ID.
     */
    @Value("${powerauth.webflow.service.oauth2.client.id}")
    private String clientId;

    /**
     * OAuth 2.1 client secret.
     */
    @Value("${powerauth.webflow.service.oauth2.client.secret}")
    private String clientSecret;

    /**
     * OAuth 2.1 client name.
     */
    @Value("${powerauth.webflow.service.oauth2.client.name}")
    private String clientName;

    /**
     * OAuth 2.1 authorization URI.
     */
    @Value("${powerauth.webflow.service.oauth2.client.authorizationUri}")
    private String authorizationUri;

    /**
     * OAuth 2.1 token URI.
     */
    @Value("${powerauth.webflow.service.oauth2.client.tokenUri}")
    private String tokenUri;

    /**
     * OAuth 2.1 user info URI.
     */
    @Value("${powerauth.webflow.service.oauth2.client.userInfoUri}")
    private String userInfoUri;

    /**
     * OAuth 2.1 redirect URI.
     */
    @Value("${powerauth.webflow.service.oauth2.client.redirectUri}")
    private String redirectUri;

    /**
     * OAuth 2.1 username attribute name for user info response.
     */
    @Value("${powerauth.webflow.service.oauth2.client.userNameAttributeName}")
    private String userNameAttributeName;

    /**
     * Next Step service URL.
     */
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
     * Get OAuth 2.1 client registration ID.
     * @return OAuth 2.1 client registration ID.
     */
    public String getRegistrationId() {
        return registrationId;
    }

    /**
     * Get OAuth 2.1 client ID.
     * @return OAuth 2.1 client ID.
     */
    public String getClientId() {
        return clientId;
    }

    /**
     * Get OAuth 2.1 client secret.
     * @return OAuth 2.1 client secret.
     */
    public String getClientSecret() {
        return clientSecret;
    }

    /**
     * Get OAuth 2.1 client name.
     * @return OAuth 2.1 client name.
     */
    public String getClientName() {
        return clientName;
    }

    /**
     * Get OAuth 2.1 authorization URI.
     * @return OAuth 2.1 authorization URI.
     */
    public String getAuthorizationUri() {
        return authorizationUri;
    }

    /**
     * Get OAuth 2.1 token URI.
     * @return OAuth 2.1 token URI.
     */
    public String getTokenUri() {
        return tokenUri;
    }

    /**
     * Get OAuth 2.1 user info URI.
     * @return OAuth 2.1 user info URI.
     */
    public String getUserInfoUri() {
        return userInfoUri;
    }

    /**
     * Get OAuth 2.1 redirect URI.
     * @return OAuth 2.1 redirect URI.
     */
    public String getRedirectUri() {
        return redirectUri;
    }

    /**
     * Get OAuth 2.1 username attribute name for user info response.
     * @return OAuth 2.1 username attribute name.
     */
    public String getUserNameAttributeName() {
        return userNameAttributeName;
    }

    /**
     * Default Next Step service client.
     *
     * @return Next Step service client.
     */
    @Bean
    public NextStepClient defaultNextStepClient() {
        final RestClientConfiguration.JacksonConfiguration jacksonConfiguration = new RestClientConfiguration.JacksonConfiguration();
        jacksonConfiguration.getSerialization().put(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        jacksonConfiguration.getDeserialization().put(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        RestClientConfiguration restClientConfiguration = new RestClientConfiguration();
        restClientConfiguration.setBaseUrl(nextstepServiceUrl);
        restClientConfiguration.setAcceptInvalidSslCertificate(acceptInvalidSslCertificate);
        restClientConfiguration.setJacksonConfiguration(jacksonConfiguration);
        try {
            return new NextStepClient(restClientConfiguration);
        } catch (NextStepClientException ex) {
            logger.error(ex.getMessage(), ex);
            return null;
        }
    }

}
