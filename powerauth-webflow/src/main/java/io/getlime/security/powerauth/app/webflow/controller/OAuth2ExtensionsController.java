/*
 * PowerAuth Web Flow and related software components
 * Copyright (C) 2020 Wultra s.r.o.
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
                logger.debug("Removing access token for client ID: {}", clientId);
                if (principal != null && clientId.equals(principal.getName())) {
                    defaultTokenServices.revokeToken(tokenId);
                }
            }
        } catch (InvalidTokenException ex) {
            logger.warn("Trying to remove access token that does not exist: {}", tokenId, ex);
        }
    }
}