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
package io.getlime.security.powerauth.lib.webflow.authentication.method.form.model.response;

import io.getlime.security.powerauth.lib.webflow.authentication.model.OrganizationDetail;

import java.util.ArrayList;
import java.util.List;

/**
 * Response object used for querying organizations.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class UsernamePasswordInitResponse {

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
