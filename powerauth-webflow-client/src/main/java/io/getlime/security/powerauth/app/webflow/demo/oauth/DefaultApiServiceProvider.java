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

package io.getlime.security.powerauth.app.webflow.demo.oauth;

import org.springframework.social.oauth2.AbstractOAuth2ServiceProvider;
import org.springframework.social.oauth2.OAuth2Template;

/**
 * Default service provider for demo service.
 *
 * @author Petr Dvorak, petr@lime-company.eu
 */
public class DefaultApiServiceProvider extends AbstractOAuth2ServiceProvider<DefaultApiBinding> {

    protected String serviceUrl;

    public DefaultApiServiceProvider(String clientId, String clientSecret, String urlAuth, String urlToken, String serviceUrl) {
        super(getOAuth2Template(clientId, clientSecret, urlAuth, urlToken));
        this.serviceUrl = serviceUrl;
    }

    private static OAuth2Template getOAuth2Template(String clientId, String clientSecret, String urlAuth, String urlToken) {
        return new DefaultAuthApiTemplate(clientId, clientSecret, urlAuth, urlToken);
    }

    @Override
    public DefaultApiBinding getApi(String accessToken) {
        return new DefaultResourceApiTemplate(accessToken, serviceUrl);
    }

}
