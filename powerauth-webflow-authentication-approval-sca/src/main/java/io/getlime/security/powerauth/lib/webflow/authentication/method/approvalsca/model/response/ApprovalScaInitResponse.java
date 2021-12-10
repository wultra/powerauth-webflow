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
package io.getlime.security.powerauth.lib.webflow.authentication.method.approvalsca.model.response;

/**
 * Response object used for initializing operation approval.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class ApprovalScaInitResponse {

    private boolean clientCertificateUsed;
    private String clientCertificateVerificationUrl;

    /**
     * Default constructor.
     */
    public ApprovalScaInitResponse() {
    }

    /**
     * Constructor with all parameters.
     * @param clientCertificateUsed Whether client certificate is used for authentication.
     * @param clientCertificateVerificationUrl Client certificate verification URL.
     */
    public ApprovalScaInitResponse(boolean clientCertificateUsed, String clientCertificateVerificationUrl) {
        this.clientCertificateUsed = clientCertificateUsed;
        this.clientCertificateVerificationUrl = clientCertificateVerificationUrl;
    }

    /**
     * Get whether client TLS certificate is used for authentication.
     * @return Whether client TLS certificate is used for authentication.
     */
    public boolean isClientCertificateUsed() {
        return clientCertificateUsed;
    }

    /**
     * Set whether client TLS certificate is used for authentication.
     * @param clientCertificateUsed Whether client TLS certificate is used for authentication.
     */
    public void setClientCertificateUsed(boolean clientCertificateUsed) {
        this.clientCertificateUsed = clientCertificateUsed;
    }

    /**
     * Get client TLS certificate verification URL.
     * @return Client TLS certificate verification URL.
     */
    public String getClientCertificateVerificationUrl() {
        return clientCertificateVerificationUrl;
    }

    /**
     * Set client TLS certificate verification URL.
     * @param clientCertificateVerificationUrl Client TLS certificate verification URL.
     */
    public void setClientCertificateVerificationUrl(String clientCertificateVerificationUrl) {
        this.clientCertificateVerificationUrl = clientCertificateVerificationUrl;
    }
}
