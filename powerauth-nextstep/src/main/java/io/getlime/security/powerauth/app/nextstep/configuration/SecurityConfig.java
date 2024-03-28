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
package io.getlime.security.powerauth.app.nextstep.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import static org.springframework.security.config.Customizer.withDefaults;

/**
 * Default Spring Security configuration.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
@Configuration
@EnableWebSecurity
@Slf4j
public class SecurityConfig {

    @Value("${powerauth.nextstep.security.auth.type:NONE}")
    private AuthType authType;

    /**
     * Configures HTTP security.
     *
     * @param http HTTP security.
     * @throws Exception Thrown when configuration fails.
     */
    @Bean
    public SecurityFilterChain filterChain(final HttpSecurity http) throws Exception {
        if (authType == AuthType.OIDC) {
            logger.info("Initializing OIDC authentication.");
            http.authorizeHttpRequests(authorize -> authorize
                            .requestMatchers(
                                    new AntPathRequestMatcher("/api/service/status"),
                                    new AntPathRequestMatcher("/actuator/**")).permitAll()
                            .anyRequest().fullyAuthenticated())
                    .oauth2ResourceServer(oauth2 -> oauth2.jwt(withDefaults()));
        } else {
            logger.info("No authentication configured");
            http.httpBasic(AbstractHttpConfigurer::disable);
        }

        return http
                .csrf(AbstractHttpConfigurer::disable)
                .build();
    }

    enum AuthType {
        NONE,

        /**
         * OpenID Connect.
         */
        OIDC
    }

}
