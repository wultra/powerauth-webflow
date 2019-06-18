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
package io.getlime.security.powerauth.lib.webflow.authentication.method.form.model.response;

import java.util.ArrayList;
import java.util.List;

/**
 * Response object used for querying organizations.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class PrepareLoginFormDataResponse {

    private List<OrganizationDetail> organizations = new ArrayList<>();

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

}
