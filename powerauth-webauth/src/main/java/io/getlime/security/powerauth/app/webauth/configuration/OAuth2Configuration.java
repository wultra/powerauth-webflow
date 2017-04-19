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
public class OAuth2Configuration extends AuthorizationServerConfigurerAdapter {

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
