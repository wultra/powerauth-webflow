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

package io.getlime.security.powerauth.app.webauth.demo.configuration;

import io.getlime.security.powerauth.app.webauth.demo.controller.CustomConnectController;
import io.getlime.security.powerauth.app.webauth.demo.oauth.DefaultApiConnectionFactory;
import io.getlime.security.powerauth.app.webauth.demo.oauth.DefaultApiServiceProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.social.UserIdSource;
import org.springframework.social.config.annotation.ConnectionFactoryConfigurer;
import org.springframework.social.config.annotation.EnableSocial;
import org.springframework.social.config.annotation.SocialConfigurerAdapter;
import org.springframework.social.connect.ConnectionFactoryLocator;
import org.springframework.social.connect.ConnectionRepository;
import org.springframework.social.connect.UsersConnectionRepository;
import org.springframework.social.connect.jdbc.JdbcUsersConnectionRepository;
import org.springframework.social.connect.web.ConnectController;
import org.springframework.social.security.AuthenticationNameUserIdSource;

import javax.sql.DataSource;

/**
 * @author Petr Dvorak, petr@lime-company.eu
 */
@Configuration
@EnableSocial
public class OAuth2ConnectConfiguration extends SocialConfigurerAdapter {

    @Autowired
    private DataSource dataSource;

    @Autowired
    WebAuthServiceConfiguration webAuthConfig;

    @Override
    public void addConnectionFactories(ConnectionFactoryConfigurer config, Environment env) {
        DefaultApiServiceProvider provider = new DefaultApiServiceProvider(
                webAuthConfig.getClientId(),
                webAuthConfig.getClientSecret(),
                webAuthConfig.getWebAuthOAuthAuthorizeUrl(),
                webAuthConfig.getWebAuthOAuthTokenUrl(),
                webAuthConfig.getWebAuthServiceUrl()
        );
        DefaultApiConnectionFactory factory = new DefaultApiConnectionFactory("demo", provider);
        config.addConnectionFactory(factory);
    }

    @Override
    public UsersConnectionRepository getUsersConnectionRepository(ConnectionFactoryLocator connectionFactoryLocator) {
        return new JdbcUsersConnectionRepository(dataSource,  connectionFactoryLocator, Encryptors.noOpText());
    }

    @Override
    public UserIdSource getUserIdSource() {
        return new AuthenticationNameUserIdSource();
    }

    @Bean
    public ConnectController connectController(ConnectionFactoryLocator connectionFactoryLocator, ConnectionRepository connectionRepository) {
        CustomConnectController connectController = new CustomConnectController(connectionFactoryLocator, connectionRepository);
        return connectController;
    }



}

