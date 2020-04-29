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
