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

import io.getlime.security.powerauth.lib.webflow.authentication.security.UserOperationAuthentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.authorization.client.JdbcRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.token.*;

/**
 * Configuration class for OAuth 2.1 Authorization Service.
 *
 * @author Petr Dvorak, petr@wultra.com
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Configuration
public class AuthorizationServerConfiguration {

    private static final String LANGUAGE = "language";
    private static final String SCA = "sca";
    private static final String ORGANIZATION_ID = "organization_id";

    private final JdbcTemplate jdbcTemplate;

    /**
     * Configuration class constructor.
     * @param jdbcTemplate JDBC template.
     */
    @Autowired
    public AuthorizationServerConfiguration(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Configure JDBC registered client repository.
     * @return Registered client repository.
     */
    @Bean
    public RegisteredClientRepository registeredClientRepository() {
        return new JdbcRegisteredClientRepository(jdbcTemplate);
    }

    /**
     * Configure authorization server settings.
     * @return Authorization server settings.
     */
    @Bean
    public AuthorizationServerSettings authorizationServerSettings() {
        return AuthorizationServerSettings.builder().build();
    }

    /**
     * Encode passwords with bcrypt.
     * @return Password encoder for bcrypt.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configuration of token generator.
     * @return Token generator.
     */
    @Bean
    public OAuth2TokenGenerator<?> tokenGenerator() {
        OAuth2AccessTokenGenerator accessTokenGenerator = new OAuth2AccessTokenGenerator();
        accessTokenGenerator.setAccessTokenCustomizer(accessTokenCustomizer());
        OAuth2RefreshTokenGenerator refreshTokenGenerator = new OAuth2RefreshTokenGenerator();
        return new DelegatingOAuth2TokenGenerator(accessTokenGenerator, refreshTokenGenerator);
    }

    /**
     * Access token claim customizer.
     * @return Token customizer.
     */
    @Bean
    public OAuth2TokenCustomizer<OAuth2TokenClaimsContext> accessTokenCustomizer() {
        return context -> {
            // Add custom claims for OAuth 2.1 token introspection endpoint
            final UserOperationAuthentication auth = context.getPrincipal();
            context.getClaims().claims(
                    claims -> {
                        claims.put(LANGUAGE, auth.getLanguage());
                        claims.put(SCA, auth.isStrongAuthentication());
                        claims.put(ORGANIZATION_ID, auth.getOrganizationId());
                    }
            );
        };
    }
}
