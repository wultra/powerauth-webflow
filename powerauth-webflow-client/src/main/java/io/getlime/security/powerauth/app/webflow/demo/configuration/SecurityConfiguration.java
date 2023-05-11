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

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Default Spring Security configuration.
 *
 * @author Petr Dvorak, petr@wultra.com
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    private final OAuth2AuthorizedClientRepository authorizedClientRepository;
    private final OAuth2AuthorizedClientService authorizedClientService;
    private final ClientRegistrationRepository clientRegistrationRepository;

    @Autowired
    public SecurityConfiguration(OAuth2AuthorizedClientRepository authorizedClientRepository, OAuth2AuthorizedClientService authorizedClientService, ClientRegistrationRepository clientRegistrationRepository) {
        this.authorizedClientRepository = authorizedClientRepository;
        this.authorizedClientService = authorizedClientService;
        this.clientRegistrationRepository = clientRegistrationRepository;
    }

    @Bean
    public SecurityFilterChain filterChain(final HttpSecurity http) throws Exception {
        return http
                .httpBasic().disable()
                .authorizeHttpRequests(requests -> requests
                        .requestMatchers("/", "/home", "/css/**", "/js/**", "/images/**").permitAll()
                        .anyRequest().fullyAuthenticated()
                )
                .oauth2Login(login -> login
                    .loginProcessingUrl("/connect/demo")
                    .authorizedClientRepository(authorizedClientRepository)
                    .authorizedClientService(authorizedClientService)
                    .clientRegistrationRepository(clientRegistrationRepository)
                    .authorizationEndpoint()
                    .authorizationRequestResolver(new CustomAuthorizationRequestResolver(this.clientRegistrationRepository)))
               .logout(logout -> logout
                    .logoutUrl("/logout")
                    .logoutSuccessUrl("/")
                    .permitAll()
                )
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        // Error message is already shown in Web Flow
                        .authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/"))
                )
                .build();
    }

    /**
     * Customization of authorization attributes - scope and operation_id.
     */
    public static class CustomAuthorizationRequestResolver implements OAuth2AuthorizationRequestResolver {
        private final OAuth2AuthorizationRequestResolver defaultAuthorizationRequestResolver;

        public CustomAuthorizationRequestResolver(ClientRegistrationRepository clientRegistrationRepository) {
            this.defaultAuthorizationRequestResolver = new DefaultOAuth2AuthorizationRequestResolver(
                    clientRegistrationRepository, "/oauth2/authorization");
        }

        @Override
        public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
            OAuth2AuthorizationRequest authorizationRequest = this.defaultAuthorizationRequestResolver.resolve(request);
            return authorizationRequest != null ? customAuthorizationRequest(request, authorizationRequest) : null;
        }

        @Override
        public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String clientRegistrationId) {
            OAuth2AuthorizationRequest authorizationRequest = this.defaultAuthorizationRequestResolver.resolve(request, clientRegistrationId);
            return authorizationRequest != null ? customAuthorizationRequest(request, authorizationRequest) : null;
        }

        private OAuth2AuthorizationRequest customAuthorizationRequest(HttpServletRequest request, OAuth2AuthorizationRequest authorizationRequest) {
            final Map<String, Object> additionalParameters = new LinkedHashMap<>(authorizationRequest.getAdditionalParameters());
            if (request.getParameter("scope") != null) {
                additionalParameters.put("scope", request.getParameter("scope"));
            }
            if (request.getParameter("operation_id") != null) {
                additionalParameters.put("operation_id", request.getParameter("operation_id"));
            }
            return OAuth2AuthorizationRequest.from(authorizationRequest).additionalParameters(additionalParameters).build();
        }
    }
}
