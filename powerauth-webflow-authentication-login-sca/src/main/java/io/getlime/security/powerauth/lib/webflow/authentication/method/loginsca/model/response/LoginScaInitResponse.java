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

    public boolean isUserAlreadyKnown() {
        return userAlreadyKnown;
    }

    public void setUserAlreadyKnown(boolean userAlreadyKnown) {
        this.userAlreadyKnown = userAlreadyKnown;
    }

    public boolean isMobileTokenEnabled() {
        return mobileTokenEnabled;
    }

    public void setMobileTokenEnabled(boolean mobileTokenEnabled) {
        this.mobileTokenEnabled = mobileTokenEnabled;
    }
}
