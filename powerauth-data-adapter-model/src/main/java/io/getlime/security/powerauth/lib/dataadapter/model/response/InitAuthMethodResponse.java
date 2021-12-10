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

package io.getlime.security.powerauth.lib.dataadapter.model.response;

import io.getlime.security.powerauth.lib.dataadapter.model.enumeration.CertificateAuthenticationMode;

/**
 * Response for initialization of an authentication method.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class InitAuthMethodResponse {

    private CertificateAuthenticationMode certificateAuthenticationMode;
    private String certificateVerificationUrl;

    /**
     * Default constructor.
     */
    public InitAuthMethodResponse() {
    }

    /**
     * Constructor with certificate authentication mode parameter.
     * @param certificateAuthenticationMode Certificate authentication mode.
     */
    public InitAuthMethodResponse(CertificateAuthenticationMode certificateAuthenticationMode) {
        this.certificateAuthenticationMode = certificateAuthenticationMode;
    }

    /**
     * Constructor with all parameters.
     * @param certificateAuthenticationMode Certificate authentication mode.
     * @param certificateVerificationUrl Certificate verification URL.
     */
    public InitAuthMethodResponse(CertificateAuthenticationMode certificateAuthenticationMode, String certificateVerificationUrl) {
        this.certificateAuthenticationMode = certificateAuthenticationMode;
        this.certificateVerificationUrl = certificateVerificationUrl;
    }

    /**
     * Get the certificate verification mode.
     * @return Certificate verification mode.
     */
    public CertificateAuthenticationMode getCertificateAuthenticationMode() {
        return certificateAuthenticationMode;
    }

    /**
     * Set the certificate verification mode.
     * @param certificateAuthenticationMode Certificate verification mode.
     */
    public void setCertificateAuthenticationMode(CertificateAuthenticationMode certificateAuthenticationMode) {
        this.certificateAuthenticationMode = certificateAuthenticationMode;
    }

    /**
     * Get the certificate verification URL.
     * @return Certificate verification URL.
     */
    public String getCertificateVerificationUrl() {
        return certificateVerificationUrl;
    }

    /**
     * Set the certificate verification URL.
     * @param certificateVerificationUrl Certificate verification URL.
     */
    public void setCertificateVerificationUrl(String certificateVerificationUrl) {
        this.certificateVerificationUrl = certificateVerificationUrl;
    }
}