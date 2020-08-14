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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.oauth2.provider.endpoint.FrameworkEndpoint;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.security.Principal;

/**
 * Controller extending the default OAuth 2.0 support in Spring Boot by additional token
 * revocation method. See https://www.baeldung.com/logout-spring-security-oauth for details.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
@FrameworkEndpoint
public class OAuth2ExtensionsController {

    private static final Logger logger = LoggerFactory.getLogger(OAuth2ExtensionsController.class);

    @Resource(name = "tokenServices")
    private AuthorizationServerTokenServices tokenServices;

    @RequestMapping(value = "/oauth/token", method = RequestMethod.DELETE)
    @ResponseBody
    public void revokeToken(@AuthenticationPrincipal Principal principal, @RequestParam("token") String tokenId) {
        try {
            if (tokenServices instanceof DefaultTokenServices) {
                final DefaultTokenServices defaultTokenServices = (DefaultTokenServices) tokenServices;
                final String clientId = defaultTokenServices.getClientId(tokenId);
                logger.info("Removing access token: {}, for client ID: {}", tokenId, clientId);
                if (principal != null && clientId.equals(principal.getName())) {
                    defaultTokenServices.revokeToken(tokenId);
                }
            }
        } catch (InvalidTokenException ex) {
            logger.warn("Trying to remove access token that does not exist: {}", tokenId, ex);
        }
    }
}