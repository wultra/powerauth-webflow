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
package io.getlime.security.powerauth.app.nextstep.converter;

import io.getlime.security.powerauth.app.nextstep.repository.model.entity.OrganizationEntity;
import io.getlime.security.powerauth.lib.nextstep.model.response.GetOrganizationDetailResponse;

/**
 * Converter for organization entity.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class OrganizationConverter {

    /**
     * Convert organization entity to get organization response.
     * @param organization Organization entity.
     * @return Get organization response.
     */
    public GetOrganizationDetailResponse fromOrganizationEntity(OrganizationEntity organization) {
        final GetOrganizationDetailResponse response = new GetOrganizationDetailResponse();
        response.setOrganizationId(organization.getOrganizationId());
        response.setDisplayNameKey(organization.getDisplayNameKey());
        response.setOrderNumber(organization.getOrderNumber());
        response.setDefault(organization.isDefault());
        response.setDefaultCredentialName(organization.getDefaultCredentialName());
        response.setDefaultOtpName(organization.getDefaultOtpName());
        return response;
    }
}
