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

import io.getlime.security.powerauth.app.webflow.demo.model.UserResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.social.oauth2.AbstractOAuth2ApiBinding;
import org.springframework.social.support.URIBuilder;

import java.net.URI;

/**
 * API template used for connecting to the actual OAuth 2.0 protected resources.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
public class DefaultResourceApiTemplate extends AbstractOAuth2ApiBinding implements DefaultApiBinding {

    private String API_URL_BASE;

    private static final Logger logger = LoggerFactory.getLogger(DefaultResourceApiTemplate.class);

    private URI buildUri(String path) {
        return URIBuilder.fromUri(API_URL_BASE + path).build();
    }

    public DefaultResourceApiTemplate(String accessToken, String baseUrl) {
        super(accessToken);
        logger.debug("OAuth 2.0 Access Token is: {}", accessToken);
        this.API_URL_BASE = baseUrl;
    }

    @Override
    public UserResponse getProfile() {
        return getRestTemplate().getForObject(buildUri("/api/secure/profile/me"), UserResponse.class);
    }

}
