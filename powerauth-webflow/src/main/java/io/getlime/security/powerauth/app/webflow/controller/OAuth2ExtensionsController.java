/*
 * Copyright 2020 Wultra s.r.o.
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

package io.getlime.security.powerauth.app.webflow.controller;

import org.springframework.security.oauth2.provider.endpoint.FrameworkEndpoint;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * Controller extending the default OAuth 2.0 support in Spring Boot by additional token
 * revocation method. See https://www.baeldung.com/logout-spring-security-oauth for details.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
@FrameworkEndpoint
public class OAuth2ExtensionsController {

    private static final String OAUTH_AUTHORIZATION_HEADER = "Authorization";
    private static final String OAUTH_BEARER_PREFIX = "Bearer ";

    @Resource(name = "tokenServices")
    AuthorizationServerTokenServices tokenServices;

    @RequestMapping(value = "/oauth/token", method = RequestMethod.DELETE)
    @ResponseBody
    public void revokeToken(HttpServletRequest request) {
        String authorization = request.getHeader(OAUTH_AUTHORIZATION_HEADER);
        if (authorization != null && authorization.startsWith(OAUTH_BEARER_PREFIX)){
            String tokenId = authorization.substring(OAUTH_BEARER_PREFIX.length());
            if (tokenServices instanceof DefaultTokenServices) {
                ((DefaultTokenServices)tokenServices).revokeToken(tokenId);
            }
        }
    }
}