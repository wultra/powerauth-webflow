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

import io.getlime.security.powerauth.app.webflow.oauth.WebFlowTokenEnhancer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.client.JdbcClientDetailsService;
import org.springframework.security.oauth2.provider.code.AuthorizationCodeServices;
import org.springframework.security.oauth2.provider.code.JdbcAuthorizationCodeServices;
import org.springframework.security.oauth2.provider.endpoint.AuthorizationEndpoint;
import org.springframework.security.oauth2.provider.endpoint.DefaultRedirectResolver;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JdbcTokenStore;

import javax.sql.DataSource;

/**
 * Configuration class for OAuth 2.0 Authorization Service.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
@Configuration
@EnableAuthorizationServer
public class OAuth2AuthorizationServerConfiguration extends AuthorizationServerConfigurerAdapter {

    private DataSource dataSource;

    /**
     * Configuration class constructor.
     * @param dataSource Data source.
     */
    @Autowired
    public OAuth2AuthorizationServerConfiguration(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Client details service which stores client data in JDBC data source.
     * @return Client details service.
     */
    @Bean("jdbcClientsDetailService")
    public ClientDetailsService clientDetailsService() {
        // client data is stored in JDBC data source (table oauth_client_details)
        return new JdbcClientDetailsService(dataSource);
    }

    /**
     * Authorization code services which stores authorization codes in JDBC data source.
     * @return Authorization code services.
     */
    @Bean
    protected AuthorizationCodeServices authorizationCodeServices() {
        // authorization codes are stored in JDBC data source (table oauth_code)
        return new JdbcAuthorizationCodeServices(dataSource);
    }

    /**
     * Token store which stores tokens in JDBC data source.
     * @return Token store.
     */
    @Bean
    public TokenStore tokenStore() {
        // tokens are stored in JDBC data source (tables oauth_access_token and oauth_refresh_token)
        return new JdbcTokenStore(dataSource);
    }

    /**
     * Custom Web Flow token enhancer.
     * @return Token enhancer.
     */
    @Bean
    public TokenEnhancer tokenEnhancer() {
        return new WebFlowTokenEnhancer();
    }

    /**
     * Initializes token services.
     * @return Initialized token services.
     */
    @Bean
    @Primary
    public AuthorizationServerTokenServices tokenServices() {
        DefaultTokenServices tokenServices = new DefaultTokenServices();
        tokenServices.setTokenStore(tokenStore());
        tokenServices.setTokenEnhancer(tokenEnhancer());
        return tokenServices;
    }

    /**
     * Configures authorization endpoint.
     * @param authorizationEndpoint Authorization endpoint.
     */
    @Autowired
    public void configureAuthorizationEndpoint(AuthorizationEndpoint authorizationEndpoint) {
        // WORKAROUND: Cancel the session just before the redirect
        DefaultRedirectResolver redirectResolver = new DefaultRedirectResolver() {
            @Override
            public String resolveRedirect(String requestedRedirect, ClientDetails client) throws OAuth2Exception {
                SecurityContextHolder.clearContext();
                return super.resolveRedirect(requestedRedirect, client);
            }
        };
        redirectResolver.setMatchPorts(false);
        authorizationEndpoint.setRedirectResolver(redirectResolver);
    }

    /**
     * Configures authorization server endpoints - mainly storage for OAuth 2.0.
     * @param endpoints Endpoints.
     * @throws Exception Thrown when configuration fails.
     */
    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints)
            throws Exception {
        endpoints.authorizationCodeServices(authorizationCodeServices())
                .tokenEnhancer(tokenEnhancer())
                .tokenStore(tokenStore())
                .approvalStoreDisabled();
    }

    /**
     * Configures client details service.
     * @param clients Client details configurer.
     * @throws Exception Thrown when configuration fails.
     */
    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        // get client configuration from JDBC data source
        clients.withClientDetails(clientDetailsService());
    }

    /**
     * Configures authorization server security - allows form authentication.
     * @param security Authorization server security.
     * @throws Exception Thrown when configuration fails.
     */
    @Override
    public void configure(AuthorizationServerSecurityConfigurer security) throws Exception {
        security.allowFormAuthenticationForClients();
    }

    /**
     * Encode passwords with bcrypt.
     * @return Password encoder for bcrypt.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
