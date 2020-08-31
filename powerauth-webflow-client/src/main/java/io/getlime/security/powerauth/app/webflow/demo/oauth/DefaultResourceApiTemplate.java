/*
 * Copyright 2017 Wultra s.r.o.
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
