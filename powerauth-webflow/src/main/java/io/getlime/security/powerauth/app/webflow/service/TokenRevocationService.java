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
package io.getlime.security.powerauth.app.webflow.service;

import io.getlime.security.powerauth.app.webflow.exception.InvalidTokenException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2RefreshToken;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Service used for token revocation.
 *
 * @author Roman Strobl, roman.strobl@wultra.com.
 */
@Service
@Slf4j
public class TokenRevocationService {

    private static final String TOKEN_TYPE_HINT_ACCESS_TOKEN = "access_token";
    private static final String TOKEN_TYPE_HINT_REFRESH_TOKEN = "refresh_token";

    private final TokenStore tokenStore;

    /**
     * Service constructor.
     * @param tokenStore Token store.
     */
    public TokenRevocationService(TokenStore tokenStore) {
        this.tokenStore = tokenStore;
    }

    /**
     * Revoke access token or refresh token.
     * @param token Access token or refresh token.
     * @param tokenTypeHint Optional hint specifying token type.
     * @param clientId OAuth 2.0 client ID.
     * @throws InvalidTokenException Thrown when token is invalid.
     */
    public void revokeToken(String token, String tokenTypeHint, String clientId) throws InvalidTokenException {
        boolean tokenRevoked = false;
        if (TOKEN_TYPE_HINT_ACCESS_TOKEN.equals(tokenTypeHint)) {
            tokenRevoked = revokeAccessToken(token, clientId);
        } else if (TOKEN_TYPE_HINT_REFRESH_TOKEN.equals(tokenTypeHint)) {
            tokenRevoked = revokeRefreshToken(token, clientId);
        }
        if (!tokenRevoked) {
            logger.debug("Token type hint is missing or invalid, attempting to revoke both access token and refresh token.");
            // Fallback logic, revoke all matching tokens
            tokenRevoked = revokeAccessToken(token, clientId) || revokeRefreshToken(token, clientId);
        }
        if (!tokenRevoked) {
            throw new InvalidTokenException("Token revocation failed");
        }
    }

    private boolean revokeAccessToken(String token, String clientId) {
        final AtomicBoolean tokenRevoked = new AtomicBoolean(false);
        logger.debug("Revoking access token: {} for client ID: {}", token, clientId);
        Optional.ofNullable(tokenStore.readAuthentication(token))
                .filter(auth -> clientId.equals(auth.getOAuth2Request().getClientId()))
                .ifPresent(t -> {
                    Optional<OAuth2AccessToken> accessTokenOptional = Optional.ofNullable(tokenStore.readAccessToken(token));
                    accessTokenOptional.ifPresent(accessToken -> {
                        Optional.ofNullable(accessToken.getRefreshToken()).ifPresent(tokenStore::removeRefreshToken);
                        tokenStore.removeAccessToken(accessToken);
                        tokenRevoked.set(true);
                    });
                });
        return tokenRevoked.get();
    }

    private boolean revokeRefreshToken(String token, String clientId) {
        final AtomicBoolean tokenRevoked = new AtomicBoolean(false);
        logger.debug("Revoking refresh token: {} for client ID: {}", token, clientId);
        Optional<OAuth2RefreshToken> refreshToken = Optional.ofNullable(tokenStore.readRefreshToken(token));
        refreshToken.ifPresent(auth -> Optional.ofNullable(tokenStore.readAuthenticationForRefreshToken(auth))
                .filter(a -> clientId.equals(a.getOAuth2Request().getClientId()))
                .ifPresent(t -> {
                    tokenStore.removeAccessTokenUsingRefreshToken(auth);
                    tokenStore.removeRefreshToken(auth);
                    tokenRevoked.set(true);
                }));
        return tokenRevoked.get();
    }

}