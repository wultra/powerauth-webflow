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
package io.getlime.security.powerauth.lib.webflow.authentication.mtoken.model.request;

import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthInstrument;
import io.getlime.security.powerauth.lib.webflow.authentication.base.AuthStepRequest;

import java.util.Collections;
import java.util.List;

/**
 * Request for QR code based authentication in offline mode for mobile token.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class QrCodeAuthenticationRequest extends AuthStepRequest {

    private String activationId;
    private String authCode;
    private String nonce;

    /**
     * Get activation ID.
     * @return Activation ID.
     */
    public String getActivationId() {
        return activationId;
    }

    /**
     * Set activation ID.
     * @param activationId Activation ID.
     */
    public void setActivationId(String activationId) {
        this.activationId = activationId;
    }

    /**
     * Get authorization code.
     * @return Authorization code.
     */
    public String getAuthCode() {
        return authCode;
    }

    /**
     * Set authorization code.
     * @param authCode Authorization code.
     */
    public void setAuthCode(String authCode) {
        this.authCode = authCode;
    }

    /**
     * Get nonce.
     * @return Nonce.
     */
    public String getNonce() {
        return nonce;
    }

    /**
     * Set nonce.
     * @param nonce Nonce.
     */
    public void setNonce(String nonce) {
        this.nonce = nonce;
    }

    @Override
    public List<AuthInstrument> getAuthInstruments() {
        return Collections.singletonList(AuthInstrument.POWERAUTH_TOKEN);
    }
}
