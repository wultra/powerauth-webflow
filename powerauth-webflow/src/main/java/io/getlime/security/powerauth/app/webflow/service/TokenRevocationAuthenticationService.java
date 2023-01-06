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

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

/**
 * Client authentication service used for verifying authentication using client_id and client_secret.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Service
@Slf4j
public class TokenRevocationAuthenticationService {

    /**
     * Check that client was authenticated using OAuth 2.0 client authentication.
     * @param userDetails User details.
     * @return OAuth 2.0 client ID.
     */
    public String checkAuthentication(UserDetails userDetails) {
        if (userDetails == null) {
            throw new InsufficientAuthenticationException("Missing authentication during token revocation");
        }
        if (userDetails.isEnabled() && userDetails.isAccountNonExpired() && userDetails.isAccountNonLocked() && userDetails.isCredentialsNonExpired()) {
            final String clientId = getClientId(userDetails);
            if (clientId == null) {
                throw new InsufficientAuthenticationException("Invalid client ID during token revocation");
            }
            // TODO - add authorities into TPP registry and check the authority for token revocation
            logger.debug("Authentication check for token revocation succeeded, client ID: {}", clientId);
            return clientId;
        }
        throw new InsufficientAuthenticationException("Invalid authentication during token revocation");
    }

    /**
     * @param userDetails User details.
     * @return OAuth 2.0 client ID.
     */
    private String getClientId(UserDetails userDetails) {
        return userDetails.getUsername();
    }

}
