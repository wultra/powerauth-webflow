/*
 * PowerAuth Web Flow and related software components
 * Copyright (C) 2019 Wultra s.r.o.
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
package io.getlime.security.powerauth.app.nextstep.converter;

import io.getlime.security.powerauth.app.nextstep.repository.model.entity.AuthenticationEntity;
import io.getlime.security.powerauth.lib.nextstep.model.entity.AuthenticationDetail;

/**
 * Converter for authentications.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class AuthenticationConverter {

    /**
     * Convert authentication from entity to detail.
     * @param authentication Authentication entity.
     * @return Authentication detail.
     */
    public AuthenticationDetail fromEntity(AuthenticationEntity authentication) {
        final AuthenticationDetail authenticationDetail = new AuthenticationDetail();
        authenticationDetail.setAuthenticationType(authentication.getAuthenticationType());
        if (authentication.getCredential() != null) {
            authenticationDetail.setCredentialName(authentication.getCredential().getCredentialDefinition().getName());
        }
        if (authentication.getOtp() != null) {
            authenticationDetail.setOtpName(authentication.getOtp().getOtpDefinition().getName());
        }
        authenticationDetail.setAuthenticationResult(authentication.getResult());
        authenticationDetail.setCredentialAuthenticationResult(authentication.getResultCredential());
        authenticationDetail.setOtpAuthenticationResult(authentication.getResultOtp());
        authenticationDetail.setTimestampCreated(authentication.getTimestampCreated());
        return authenticationDetail;
    }

}