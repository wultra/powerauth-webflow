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

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * Demo application configuration.
 *
 * @author Petr Dvorak, petr@lime-company.eu
 */
@Configuration
public class WebAuthServiceConfiguration {

    @Value("${powerauth.webauth.service.url}")
    private String webAuthServiceUrl;

    @Value("${powerauth.webauth.service.oauth.authorizeUrl}")
    private String webAuthOAuthAuthorizeUrl;

    @Value("${powerauth.webauth.service.oauth.tokenUrl}")
    private String webAuthOAuthTokenUrl;

    @Value("${powerauth.webauth.service.oauth.clientId}")
    private String clientId;

    @Value("${powerauth.webauth.service.oauth.clientSecret}")
    private String clientSecret;

    public String getWebAuthServiceUrl() {
        return webAuthServiceUrl;
    }

    public String getWebAuthOAuthAuthorizeUrl() {
        return webAuthOAuthAuthorizeUrl;
    }

    public String getWebAuthOAuthTokenUrl() {
        return webAuthOAuthTokenUrl;
    }

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }
}
