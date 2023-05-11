/*
 * PowerAuth Web Flow and related software components
 * Copyright (C) 2023 Wultra s.r.o.
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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.InMemoryOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.AuthenticatedPrincipalOAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;

import java.util.Collections;

/**
 * OAuth 2.1 Demo Client configuration.
 *
 * @author Petr Dvorak, petr@wultra.com
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Configuration
public class OAuth2ClientConfiguration {

    private final WebFlowServiceConfiguration webFlowConfig;

    @Autowired
    public OAuth2ClientConfiguration(WebFlowServiceConfiguration webFlowConfig) {
        this.webFlowConfig = webFlowConfig;
    }

    /**
     * Configuration of client registration repository.
     * @return Client registration repository.
     */
    @Bean
    public InMemoryClientRegistrationRepository clientRegistrationRepository() {
        final ClientRegistration clientRegistration = ClientRegistration
                .withRegistrationId(webFlowConfig.getRegistrationId())
                .clientId(webFlowConfig.getClientId())
                .clientSecret(webFlowConfig.getClientSecret())
                .clientName(webFlowConfig.getClientName())
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizationUri(webFlowConfig.getAuthorizationUri())
                .tokenUri(webFlowConfig.getTokenUri())
                .userInfoUri(webFlowConfig.getUserInfoUri())
                .redirectUri(webFlowConfig.getRedirectUri())
                .userNameAttributeName(webFlowConfig.getUserNameAttributeName())
                .build();
        return new InMemoryClientRegistrationRepository(Collections.singletonList(clientRegistration));
    }

    /**
     * Configuration of authorized client service.
     * @param clientRegistrationRepository Client registration repository.
     * @return Authorized client service.
     */
    @Bean
    public OAuth2AuthorizedClientService authorizedClientService(
            ClientRegistrationRepository clientRegistrationRepository) {
        return new InMemoryOAuth2AuthorizedClientService(clientRegistrationRepository);
    }

    /**
     * Configuration of authorized client repository.
     * @param authorizedClientService Authorized client service.
     * @return Authorized client repository.
     */
    @Bean
    public OAuth2AuthorizedClientRepository authorizedClientRepository(
            OAuth2AuthorizedClientService authorizedClientService) {
        return new AuthenticatedPrincipalOAuth2AuthorizedClientRepository(authorizedClientService);
    }

}

