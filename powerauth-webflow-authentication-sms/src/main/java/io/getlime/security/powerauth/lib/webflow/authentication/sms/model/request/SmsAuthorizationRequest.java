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
package io.getlime.security.powerauth.lib.webflow.authentication.sms.model.request;

import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthInstrument;
import io.getlime.security.powerauth.lib.webflow.authentication.base.AuthStepRequest;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

/**
 * Request for SMS authorization.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class SmsAuthorizationRequest extends AuthStepRequest {

    @Pattern(regexp = "^[0-9]{4,8}$", message = "login.authenticationFailed")
    private String authCode;

    @Pattern(regexp = "^$|^.{4,128}$", message = "login.authenticationFailed")
    private String password;

    @Size(min = 1, max = 32000)
    private String certificate;

    @Size(min = 1, max = 100)
    private String signature;

    @NotNull(message = "error.invalidRequest")
    private List<AuthInstrument> authInstruments = new ArrayList<>();

    /**
     * Get authorization code from SMS message.
     * @return Authorization code from SMS message.
     */
    public String getAuthCode() {
        return authCode;
    }

    /**
     * Set authorization code from SMS message.
     * @param authCode Authorization code from SMS message.
     */
    public void setAuthCode(String authCode) {
        this.authCode = authCode;
    }

    /**
     * Get user password (optional).
     * @return User password.
     */
    public String getPassword() {
        return password;
    }

    /**
     * Set user password (optional).
     * @param password User password.
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Get certificate in PEM format.
     * @return Certificate in PEM format.
     */
    public String getCertificate() {
        return certificate;
    }

    /**
     * Set certificate in PEM format.
     * @param certificate Certificate in PEM format.
     */
    public void setCertificate(String certificate) {
        this.certificate = certificate;
    }

    /**
     * Get signature created by certificate.
     * @return Signature created by certificate.
     */
    public String getSignature() {
        return signature;
    }

    /**
     * Set signature created by certificate.
     * @param signature Signature created by certificate.
     */
    public void setSignature(String signature) {
        this.signature = signature;
    }

    @Override
    public List<AuthInstrument> getAuthInstruments() {
        return authInstruments;
    }

    /**
     * Set authorization instruments used in this request.
     * @param authInstruments Authorization instruments used in this request.
     */
    public void setAuthInstruments(List<AuthInstrument> authInstruments) {
        this.authInstruments = authInstruments;
    }
}
