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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.provider.client.ClientDetailsUserDetailsService;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Collections;

/**
 * Default Spring Security configuration.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
@Configuration
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Value("${powerauth.webflow.security.cors.enabled:false}")
    private boolean corsConfigurationEnabled;

    @Value("${powerauth.webflow.security.cors.allowOrigin:*}")
    private String corsAllowOrigin;

    private final static String OAUTH2_TOKEN_REVOKE_ENDPOINT = "/oauth/token/revoke";
    private final static String OAUTH2_CLIENT_REALM_NAME = "oauth2/client";

    private final OAuth2AuthorizationServerConfiguration authConfig;

    /**
     * Configuration class constructor.
     * @param authConfig OAuth 2.0 authorization server configuration.
     */
    public SecurityConfiguration(OAuth2AuthorizationServerConfiguration authConfig) {
        this.authConfig = authConfig;
    }

    /**
     * Configure http security for OAuth 2.0 authentication, URL exceptions, CSRF tokens, etc.
     * @param http HTTP security.
     * @throws Exception Thrown when configuration fails.
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .csrf().ignoringAntMatchers("/api/auth/token/app/**", "/api/push/**", "/pa/**", OAUTH2_TOKEN_REVOKE_ENDPOINT)
                .and()
                    .antMatcher(OAUTH2_TOKEN_REVOKE_ENDPOINT).httpBasic().realmName(OAUTH2_CLIENT_REALM_NAME)
                .and()
                    .antMatcher("/**").authorizeRequests()
                    .antMatchers("/", "/authenticate", "/authenticate/**", "/oauth/error", "/api/**", "/pa/**", "/resources/**", "/ext-resources/**", "/websocket/**", "/v3/api-docs/**", "/swagger-resources/**", "/swagger-ui.html", "/swagger-ui/**", "/webjars/**", "/actuator/**", "/tls/client/**", "/signer/**").permitAll()
                    .anyRequest().fullyAuthenticated()
                .and()
                    .exceptionHandling()
                    .authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/authenticate"));
        http.cors();
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

    @Bean
    UserDetailsService clientUserDetailsService() {
        return new ClientDetailsUserDetailsService(authConfig.clientDetailsService());
    }
}
