/*
 * Copyright 2019 Wultra s.r.o.
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
        AuthenticationDetail authenticationDetail = new AuthenticationDetail();
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