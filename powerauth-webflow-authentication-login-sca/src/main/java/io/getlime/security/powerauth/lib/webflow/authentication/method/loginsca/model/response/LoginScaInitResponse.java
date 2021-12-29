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
    private boolean clientCertificateAuthenticationAvailable;
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
     * Get whether authentication using client TLS certificate is available.
     * @return Whether authentication using client TLS certificate is available.
     */
    public boolean isClientCertificateAuthenticationAvailable() {
        return clientCertificateAuthenticationAvailable;
    }

    /**
     * Set whether authentication using client TLS certificate is available.
     * @param clientCertificateAuthenticationAvailable Whether authentication using client TLS certificate is available.
     */
    public void setClientCertificateAuthenticationAvailable(boolean clientCertificateAuthenticationAvailable) {
        this.clientCertificateAuthenticationAvailable = clientCertificateAuthenticationAvailable;
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
