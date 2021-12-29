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

import io.getlime.security.powerauth.app.webflow.demo.controller.CustomConnectController;
import io.getlime.security.powerauth.app.webflow.demo.oauth.DefaultApiConnectionFactory;
import io.getlime.security.powerauth.app.webflow.demo.oauth.DefaultApiServiceProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.social.UserIdSource;
import org.springframework.social.config.annotation.ConnectionFactoryConfigurer;
import org.springframework.social.config.annotation.EnableSocial;
import org.springframework.social.config.annotation.SocialConfigurerAdapter;
import org.springframework.social.connect.ConnectionFactoryLocator;
import org.springframework.social.connect.ConnectionRepository;
import org.springframework.social.connect.UsersConnectionRepository;
import org.springframework.social.connect.mem.InMemoryUsersConnectionRepository;
import org.springframework.social.connect.web.ConnectController;
import org.springframework.social.security.AuthenticationNameUserIdSource;

/**
 * OAuth 2.0 Demo Client configuration.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
@Configuration
@EnableSocial
public class OAuth2ConnectConfiguration extends SocialConfigurerAdapter {


    @Autowired
    private WebFlowServiceConfiguration webFlowConfig;

    @Override
    public void addConnectionFactories(ConnectionFactoryConfigurer config, Environment env) {
        DefaultApiServiceProvider provider = new DefaultApiServiceProvider(
                webFlowConfig.getClientId(),
                webFlowConfig.getClientSecret(),
                webFlowConfig.getWebFlowOAuthAuthorizeUrl(),
                webFlowConfig.getWebFlowOAuthTokenUrl(),
                webFlowConfig.getWebFlowServiceUrl()
        );
        DefaultApiConnectionFactory factory = new DefaultApiConnectionFactory<>("demo", provider);
        config.addConnectionFactory(factory);
    }

    @Override
    public UsersConnectionRepository getUsersConnectionRepository(ConnectionFactoryLocator connectionFactoryLocator) {
        return new InMemoryUsersConnectionRepository(connectionFactoryLocator);
    }

    @Override
    public UserIdSource getUserIdSource() {
        return new AuthenticationNameUserIdSource();
    }

    @Bean
    public ConnectController connectController(ConnectionFactoryLocator connectionFactoryLocator, ConnectionRepository connectionRepository) {
        return new CustomConnectController(connectionFactoryLocator, connectionRepository);
    }


}

