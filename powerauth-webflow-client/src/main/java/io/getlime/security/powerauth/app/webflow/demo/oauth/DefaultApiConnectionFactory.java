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

package io.getlime.security.powerauth.app.webflow.demo.oauth;

import org.springframework.social.connect.support.OAuth2ConnectionFactory;
import org.springframework.social.oauth2.AbstractOAuth2ServiceProvider;

/**
 * Default connection factory for OAuth 2.0 protected service.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
public class DefaultApiConnectionFactory<T extends DefaultApiBinding> extends OAuth2ConnectionFactory<T> {

    public DefaultApiConnectionFactory(String providerId, AbstractOAuth2ServiceProvider<T> provider) {
        super(providerId, provider, new DefaultApiAdapter<T>());
    }
}
