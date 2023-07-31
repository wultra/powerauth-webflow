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

package io.getlime.security.powerauth.app.webflow.configuration;

import com.google.common.collect.ImmutableList;
import io.getlime.security.powerauth.lib.webflow.authentication.service.AuthenticationManagementService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AuthorizationCodeRequestAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Objects;

/**
 * Default Spring Security configuration.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(SecurityConfiguration.class);

    @Value("${powerauth.webflow.security.cors.allowOrigin:*}")
    private String corsAllowOrigin;

    @Value("${powerauth.webflow.service.oauth2.introspection.uri}")
    private String introspectionUri;

    @Value("${powerauth.webflow.service.oauth2.introspection.clientId}")
    private String clientId;

    @Value("${powerauth.webflow.service.oauth2.introspection.clientSecret}")
    private String clientSecret;

    private final SecurityContextRepository securityContextRepository;
    private final AuthenticationManagementService authenticationManagementService;

    @Autowired
    public SecurityConfiguration(SecurityContextRepository securityContextRepository, AuthenticationManagementService authenticationManagementService) {
        this.securityContextRepository = securityContextRepository;
        this.authenticationManagementService = authenticationManagementService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {
        final OAuth2AuthorizationServerConfigurer authorizationServerConfigurer = new OAuth2AuthorizationServerConfigurer();
        authorizationServerConfigurer
                .authorizationEndpoint(authorizationEndpoint ->
                        authorizationEndpoint
                                .authorizationResponseHandler((request, response, authentication) -> {
                                    // Clear the security context just before the redirect
                                    logger.info("Clearing security context after successful OAuth 2.1 authorization");
                                    authenticationManagementService.clearContext();

                                    // Redirect user to the original application.
                                    logger.info("Redirecting user back to the original application");
                                    redirectToOriginalApplication(request, response, authentication);
                                })
                );
        http
                // Apply OAuth 2.1 authorization server configuration
                .apply(authorizationServerConfigurer);
        return http
                // Accept access tokens for user info endpoints in resource server, use token introspection
                .oauth2ResourceServer((oauth2) -> oauth2
                        .opaqueToken((opaque) -> opaque
                                .introspectionUri(this.introspectionUri)
                                .introspectionClientCredentials(this.clientId, this.clientSecret)
                        )
                )
                // Configure securityContextRepository for session managemet, see: https://docs.spring.io/spring-security/reference/migration/servlet/session-management.html
                .securityContext((securityContext) -> securityContext
                        .securityContextRepository(securityContextRepository)
                )
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/api/auth/token/app/**", "/api/push/**", "/pa/**", "/oauth2/**")
                        .ignoringRequestMatchers(authorizationServerConfigurer.getEndpointsMatcher()))
                .authorizeHttpRequests((requests) -> requests
                        .requestMatchers("/", "/authenticate", "/authenticate/**", "/oauth2/error", "/api/**", "/pa/**", "/resources/**", "/ext-resources/**", "/websocket/**", "/v3/api-docs/**", "/swagger-resources/**", "/swagger-ui.html", "/swagger-ui/**", "/webjars/**", "/actuator/**", "/tls/client/**", "/signer/**", "/favicon.ico").permitAll()
                        // Authenticate OAuth 2.1 endpoints
                        .requestMatchers(authorizationServerConfigurer.getEndpointsMatcher()).fullyAuthenticated()
                        // Resource server endpoints
                        .requestMatchers("/api/secure/**").fullyAuthenticated()
                        .anyRequest().fullyAuthenticated()
                )
                // Redirect to the login page when not authenticated from the authorization endpoint
                .exceptionHandling((exceptions) -> exceptions
                        .authenticationEntryPoint(
                                new LoginUrlAuthenticationEntryPoint("/authenticate"))
                )
                .cors(Customizer.withDefaults())
                .build();
    }

    /**
     * Perform redirect back to the original application.
     */
    private void redirectToOriginalApplication(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        final OAuth2AuthorizationCodeRequestAuthenticationToken authorizationCodeRequestAuthentication = (OAuth2AuthorizationCodeRequestAuthenticationToken) authentication;
        final UriComponentsBuilder uriBuilder = UriComponentsBuilder
                .fromUriString(Objects.requireNonNull(authorizationCodeRequestAuthentication.getRedirectUri()))
                .queryParam(OAuth2ParameterNames.CODE, Objects.requireNonNull(authorizationCodeRequestAuthentication.getAuthorizationCode()).getTokenValue())
                .queryParam(OAuth2ParameterNames.STATE, UriUtils.encode(Objects.requireNonNull(authorizationCodeRequestAuthentication.getState()), StandardCharsets.UTF_8));
        final String redirectUri = uriBuilder.build(true).toUriString();
        new DefaultRedirectStrategy().sendRedirect(request, response, redirectUri);
    }

    /**
     * Configure CORS to allow client TLS certificate verification from a different port.
     * @return CORS configuration source.
     */
    @ConditionalOnProperty(name = "powerauth.webflow.security.cors.enabled", havingValue = "true")
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        // Configuration of CORS for client TLS certificate validation which can be requested from another host/port
        final CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Collections.singletonList(corsAllowOrigin));
        configuration.setAllowedMethods(ImmutableList.of("GET", "POST", "OPTIONS"));
        configuration.setAllowCredentials(true);
        configuration.setAllowedHeaders(ImmutableList.of("Content-Type", "X-CSRF-Token"));
        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/tls/client/login", configuration);
        source.registerCorsConfiguration("/tls/client/approve", configuration);
        return source;
    }

}
