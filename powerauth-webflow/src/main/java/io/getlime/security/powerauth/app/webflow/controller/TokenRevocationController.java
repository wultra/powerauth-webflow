/*
 * PowerAuth Web Flow and related software components
 * Copyright (C) 2022 Wultra s.r.o.
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

import io.getlime.security.powerauth.app.webflow.exception.InvalidTokenException;
import io.getlime.security.powerauth.app.webflow.service.TokenRevocationAuthenticationService;
import io.getlime.security.powerauth.app.webflow.service.TokenRevocationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for OAuth 2.0 Token Revocation, see: https://www.rfc-editor.org/rfc/rfc7009
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@RestController
@Slf4j
public class TokenRevocationController {

    private final TokenRevocationAuthenticationService tokenRevocationAuthenticationService;
    private final TokenRevocationService tokenRevocationService;

    /**
     * Controller constructor.
     * @param tokenRevocationAuthenticationService Authentication service for token revocation.
     * @param tokenRevocationService Token revocation service.
     */
    @Autowired
    public TokenRevocationController(TokenRevocationAuthenticationService tokenRevocationAuthenticationService, TokenRevocationService tokenRevocationService) {
        this.tokenRevocationAuthenticationService = tokenRevocationAuthenticationService;
        this.tokenRevocationService = tokenRevocationService;
    }

    /**
     * Endpoint used for token revocation, see: https://www.rfc-editor.org/rfc/rfc7009
     * @param user Authenticated user.
     * @param token OAuth 2.0 access token or refresh token for revocation.
     * @param tokenTypeHint Optional hint whether token is an access token or a refresh token.
     * @throws InvalidTokenException Thrown when token is invalid.
     */
    @PostMapping("/token/revoke")
    @ResponseBody
    public void revokeToken(@AuthenticationPrincipal User user, @RequestParam("token") String token, @RequestParam("token_type_hint") String tokenTypeHint) throws InvalidTokenException {
        logger.info("Revoking token: {}, token type: {}", token, tokenTypeHint);
        final String clientId = tokenRevocationAuthenticationService.checkAuthentication(user);
        tokenRevocationService.revokeToken(token, tokenTypeHint, clientId);
    }
}