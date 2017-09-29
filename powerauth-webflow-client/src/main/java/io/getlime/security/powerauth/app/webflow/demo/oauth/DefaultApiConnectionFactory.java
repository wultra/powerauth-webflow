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

import org.springframework.social.connect.support.OAuth2ConnectionFactory;
import org.springframework.social.oauth2.AbstractOAuth2ServiceProvider;

/**
 * Default connection factory for OAuth 2.0 protected service.
 *
 * @author Petr Dvorak, petr@lime-company.eu
 */
public class DefaultApiConnectionFactory<T extends DefaultApiBinding> extends OAuth2ConnectionFactory<T> {

    public DefaultApiConnectionFactory(String providerId, AbstractOAuth2ServiceProvider<T> provider) {
        super(providerId, provider, new DefaultApiAdapter<T>());
    }
}
