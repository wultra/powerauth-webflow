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

import org.springframework.social.oauth2.AbstractOAuth2ServiceProvider;
import org.springframework.social.oauth2.OAuth2Template;

/**
 * Default service provider for demo service.
 *
 * @author Petr Dvorak, petr@wultra.com
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
