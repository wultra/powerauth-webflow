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