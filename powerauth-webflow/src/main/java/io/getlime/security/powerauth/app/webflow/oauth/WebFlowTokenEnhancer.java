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
    private static final String ORGANIZATION_ID = "organization_id";

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
