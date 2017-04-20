/*
 * Copyright 2017 Lime - HighTech Solutions s.r.o.
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

package io.getlime.security.powerauth.app.webauth.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.endpoint.AuthorizationEndpoint;
import org.springframework.security.oauth2.provider.endpoint.DefaultRedirectResolver;

/**
 * Configuration class for OAuth 2.0 Authorization Service.
 *
 * @author Petr Dvorak, petr@lime-company.eu
 */
@Configuration
@EnableAuthorizationServer
public class OAuth2AuthorizationServerConfiguration extends AuthorizationServerConfigurerAdapter {

    @Autowired
    public void configureAuthorizationEndpoint(AuthorizationEndpoint authorizationEndpoint) {
        // WORKAROUND: Cancel the session just before the redirect
        DefaultRedirectResolver redirectResolver = new DefaultRedirectResolver() {
            @Override public String resolveRedirect(String requestedRedirect, ClientDetails client) throws OAuth2Exception {
                SecurityContextHolder.clearContext();
                return super.resolveRedirect(requestedRedirect, client);
            }
        };
        redirectResolver.setMatchPorts(false);
        authorizationEndpoint.setRedirectResolver(redirectResolver);
    }

    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        clients.inMemory()
                .withClient("foo").secret("bar")
                .authorizedGrantTypes("authorization_code")
                .scopes("profile").autoApprove(".*");
    }

}
