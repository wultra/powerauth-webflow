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
package io.getlime.security.powerauth.lib.webflow.authentication.method.loginsca.model.response;

import io.getlime.security.powerauth.lib.webflow.authentication.model.OrganizationDetail;

import java.util.ArrayList;
import java.util.List;

/**
 * Response object used for querying login form data.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class LoginScaInitResponse {

    private List<OrganizationDetail> organizations = new ArrayList<>();
    private boolean userAlreadyKnown;
    private boolean mobileTokenEnabled;
    private boolean clientCertificateAuthenticationEnabled;
    private String clientCertificateVerificationUrl;

    /**
     * Get organizations.
     * @return Organizations.
     */
    public List<OrganizationDetail> getOrganizations() {
        return organizations;
    }

    /**
     * Set organizations.
     * @param organizations Organizations.
     */
    public void setOrganizations(List<OrganizationDetail> organizations) {
        this.organizations = organizations;
    }

    /**
     * Add an organization.
     * @param organization Organization.
     */
    public void addOrganization(OrganizationDetail organization) {
        organizations.add(organization);
    }

    /**
     * Get whether user ID is already known, used during page refresh.
     * @return Whether user ID is already known.
     */
    public boolean isUserAlreadyKnown() {
        return userAlreadyKnown;
    }

    /**
     * Set whether user ID is already known, used during page refresh.
     * @param userAlreadyKnown Whether user ID is already known.
     */
    public void setUserAlreadyKnown(boolean userAlreadyKnown) {
        this.userAlreadyKnown = userAlreadyKnown;
    }

    /**
     * Get whether mobile token is enabled.
     * @return Whether mobile token is enabled.
     */
    public boolean isMobileTokenEnabled() {
        return mobileTokenEnabled;
    }

    /**
     * Set whether mobile token is enabled.
     * @param mobileTokenEnabled Whether mobile token is enabled.
     */
    public void setMobileTokenEnabled(boolean mobileTokenEnabled) {
        this.mobileTokenEnabled = mobileTokenEnabled;
    }

    /**
     * Get whether authentication using client TLS certificate is enabled.
     * @return Whether authentication using client TLS certificate is enabled.
     */
    public boolean isClientCertificateAuthenticationEnabled() {
        return clientCertificateAuthenticationEnabled;
    }

    /**
     * Set whether authentication using client TLS certificate is enabled.
     * @param clientCertificateAuthenticationEnabled Whether authentication using client TLS certificate is enabled.
     */
    public void setClientCertificateAuthenticationEnabled(boolean clientCertificateAuthenticationEnabled) {
        this.clientCertificateAuthenticationEnabled = clientCertificateAuthenticationEnabled;
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
