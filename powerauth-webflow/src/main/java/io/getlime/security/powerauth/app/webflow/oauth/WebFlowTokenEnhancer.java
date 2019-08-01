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
package io.getlime.security.powerauth.app.webflow.oauth;

import io.getlime.security.powerauth.lib.webflow.authentication.security.UserOperationAuthentication;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;

import java.util.HashMap;
import java.util.Map;

/**
 * OAuth 2.0 Token Enhancer that adds user browser language and Strong
 * Customer Authentication flag.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
public class WebFlowTokenEnhancer implements TokenEnhancer {

    private static final String LANGUAGE = "language";
    private static final String SCA = "sca";
    private static final String ORGANIZATION_ID = "organizationId";

    /**
     * Enhance access tokens with additional information.
     *
     * @param accessToken Access token.
     * @param authentication OAuth 2.0 authentication.
     * @return Enhanced OAuth 2.0 access token.
     */
    @Override
    public OAuth2AccessToken enhance(OAuth2AccessToken accessToken, OAuth2Authentication authentication) {
        DefaultOAuth2AccessToken token = ((DefaultOAuth2AccessToken) accessToken);
        UserOperationAuthentication auth = (UserOperationAuthentication) authentication.getUserAuthentication();
        Map<String, Object> extras = new HashMap<>();
        extras.put(LANGUAGE, auth.getLanguage());
        extras.put(SCA, auth.isStrongAuthentication());
        extras.put(ORGANIZATION_ID, auth.getOrganizationId());
        token.setAdditionalInformation(extras);
        return token;
    }

}
