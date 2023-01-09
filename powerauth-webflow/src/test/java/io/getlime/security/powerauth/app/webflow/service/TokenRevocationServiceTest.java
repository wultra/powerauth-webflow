/*
 * PowerAuth Web Flow and related software components
 * Copyright (C) 2023 Wultra s.r.o.
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
import io.getlime.security.powerauth.lib.webflow.authentication.security.UserOperationAuthentication;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2RefreshToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.Serializable;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Token revocation service tests.
 *
 * @author Roman Strobl, roman.strobl@wultra.com.
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application.properties")
@Sql(scripts = "/db_schema.sql")
public class TokenRevocationServiceTest {

    private final AuthorizationServerTokenServices tokenServices;
    private final TokenStore tokenStore;
    private final TokenRevocationService tokenRevocationService;

    @Autowired
    public TokenRevocationServiceTest(AuthorizationServerTokenServices tokenServices, TokenStore tokenStore, TokenRevocationService tokenRevocationService) {
        this.tokenServices = tokenServices;
        this.tokenStore = tokenStore;
        this.tokenRevocationService = tokenRevocationService;
    }

    @Test
    void revokeAccessTokenTest() throws InvalidTokenException {
        final OAuth2AccessToken token = tokenServices.createAccessToken(createAuthentication());
        assertNotNull(token);
        assertNotNull(token.getRefreshToken());
        final OAuth2AccessToken accessTokenExtracted = tokenStore.readAccessToken(token.toString());
        assertEquals(token, accessTokenExtracted);
        final OAuth2RefreshToken refreshTokenExtracted = tokenStore.readRefreshToken(token.getRefreshToken().toString());
        assertEquals(token.getRefreshToken(), refreshTokenExtracted);
        tokenRevocationService.revokeToken(token.toString(), "access_token", "testclient");
        final OAuth2AccessToken accessTokenRemoved = tokenStore.readAccessToken(token.toString());
        assertNull(accessTokenRemoved);
        final OAuth2RefreshToken refreshTokenRemoved = tokenStore.readRefreshToken(token.getRefreshToken().toString());
        assertNull(refreshTokenRemoved);
    }

    @Test
    void revokeRefreshTokenTest() throws InvalidTokenException {
        final OAuth2AccessToken token = tokenServices.createAccessToken(createAuthentication());
        assertNotNull(token);
        assertNotNull(token.getRefreshToken());
        final OAuth2AccessToken accessTokenExtracted = tokenStore.readAccessToken(token.toString());
        assertEquals(token, accessTokenExtracted);
        final OAuth2RefreshToken refreshTokenExtracted = tokenStore.readRefreshToken(token.getRefreshToken().toString());
        assertEquals(token.getRefreshToken(), refreshTokenExtracted);
        tokenRevocationService.revokeToken(token.getRefreshToken().toString(), "refresh_token", "testclient");
        final OAuth2AccessToken accessTokenRemoved = tokenStore.readAccessToken(token.toString());
        assertNull(accessTokenRemoved);
        final OAuth2RefreshToken refreshTokenRemoved = tokenStore.readRefreshToken(token.getRefreshToken().toString());
        assertNull(refreshTokenRemoved);
    }

    @Test
    void revokeAccessTokenTestInvalidClientId() {
        final OAuth2AccessToken token = tokenServices.createAccessToken(createAuthentication());
        assertNotNull(token);
        assertNotNull(token.getRefreshToken());
        final OAuth2AccessToken accessTokenExtracted = tokenStore.readAccessToken(token.toString());
        assertEquals(token, accessTokenExtracted);
        final OAuth2RefreshToken refreshTokenExtracted = tokenStore.readRefreshToken(token.getRefreshToken().toString());
        assertEquals(token.getRefreshToken(), refreshTokenExtracted);
        assertThrows(InvalidTokenException.class, () -> {
            tokenRevocationService.revokeToken(token.toString(), "access_token", "testclient2");
        });
    }

    @Test
    void revokeAccessTokenTestMissingHint() throws InvalidTokenException {
        final OAuth2AccessToken token = tokenServices.createAccessToken(createAuthentication());
        assertNotNull(token);
        assertNotNull(token.getRefreshToken());
        tokenRevocationService.revokeToken(token.toString(), null, "testclient");
        final OAuth2AccessToken accessTokenRemoved = tokenStore.readAccessToken(token.toString());
        assertNull(accessTokenRemoved);
        final OAuth2RefreshToken refreshTokenRemoved = tokenStore.readRefreshToken(token.getRefreshToken().toString());
        assertNull(refreshTokenRemoved);
    }

    @Test
    void revokeRefreshTokenTestMissingHint() throws InvalidTokenException {
        final OAuth2AccessToken token = tokenServices.createAccessToken(createAuthentication());
        assertNotNull(token);
        assertNotNull(token.getRefreshToken());
        tokenRevocationService.revokeToken(token.getRefreshToken().toString(), null, "testclient");
        final OAuth2AccessToken accessTokenRemoved = tokenStore.readAccessToken(token.toString());
        assertNull(accessTokenRemoved);
        final OAuth2RefreshToken refreshTokenRemoved = tokenStore.readRefreshToken(token.getRefreshToken().toString());
        assertNull(refreshTokenRemoved);
    }

    @Test
    void revokeAccessTokenTestInvalidHint() throws InvalidTokenException {
        final OAuth2AccessToken token = tokenServices.createAccessToken(createAuthentication());
        assertNotNull(token);
        assertNotNull(token.getRefreshToken());
        tokenRevocationService.revokeToken(token.toString(), "foo", "testclient");
        final OAuth2AccessToken accessTokenRemoved = tokenStore.readAccessToken(token.toString());
        assertNull(accessTokenRemoved);
        final OAuth2RefreshToken refreshTokenRemoved = tokenStore.readRefreshToken(token.getRefreshToken().toString());
        assertNull(refreshTokenRemoved);
    }

    @Test
    void revokeRefreshTokenTestSwitchedHint() throws InvalidTokenException {
        final OAuth2AccessToken token = tokenServices.createAccessToken(createAuthentication());
        assertNotNull(token);
        assertNotNull(token.getRefreshToken());
        tokenRevocationService.revokeToken(token.getRefreshToken().toString(), "access_token", "testclient");
        final OAuth2AccessToken accessTokenRemoved = tokenStore.readAccessToken(token.toString());
        assertNull(accessTokenRemoved);
        final OAuth2RefreshToken refreshTokenRemoved = tokenStore.readRefreshToken(token.getRefreshToken().toString());
        assertNull(refreshTokenRemoved);
    }

    @Test
    void revokeTokenNonExistent() {
        assertThrows(InvalidTokenException.class, () -> {
            tokenRevocationService.revokeToken("SSAoMe1WjlokyFhOvjO6lcXNM6A", "access_token", "testclient");
        });
    }

    private OAuth2Authentication createAuthentication() {
        return new OAuth2Authentication(
                createOAuth2Request(null, "testclient", null, false,
                        new LinkedHashSet<>(Arrays.asList("read", "write")), null, null, null, null),
                new UserOperationAuthentication(UUID.randomUUID().toString(), "testuser", "TEST"));
    }

    public static OAuth2Request createOAuth2Request(Map<String, String> requestParameters, String clientId,
                                                    Collection<? extends GrantedAuthority> authorities, boolean approved, Collection<String> scope,
                                                    Set<String> resourceIds, String redirectUri, Set<String> responseTypes,
                                                    Map<String, Serializable> extensionProperties) {
        return new OAuth2Request(requestParameters, clientId, authorities, approved, scope == null
                ? null
                : new LinkedHashSet<>(scope), resourceIds, redirectUri, responseTypes, extensionProperties);
    }

}