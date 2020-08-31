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

package io.getlime.security.powerauth.app.webflow.configuration;

import com.google.common.collect.ImmutableList;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
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

    /**
     * Configure http security for OAuth 2.0 authentication, URL exceptions, CSRF tokens, etc.
     * @param http HTTP security.
     * @throws Exception Thrown when configuration fails.
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .httpBasic().disable()
                .csrf().ignoringAntMatchers("/api/auth/token/app/**", "/api/push/**", "/pa/**").and()
                .antMatcher("/**").authorizeRequests()
                .antMatchers("/", "/authenticate", "/authenticate/**", "/oauth/error", "/api/**", "/pa/**", "/resources/**", "/ext-resources/**", "/websocket/**", "/v2/api-docs", "/swagger-resources/**", "/swagger-ui.html", "/webjars/**", "/actuator/**", "/tls/client/**").permitAll()
                .anyRequest().authenticated()
                .and().exceptionHandling()
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
}
