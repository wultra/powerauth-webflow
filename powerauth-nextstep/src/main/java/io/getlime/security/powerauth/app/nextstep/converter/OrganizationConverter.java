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
        GetOrganizationDetailResponse response = new GetOrganizationDetailResponse();
        response.setOrganizationId(organization.getOrganizationId());
        response.setDisplayNameKey(organization.getDisplayNameKey());
        response.setOrderNumber(organization.getOrderNumber());
        response.setDefault(organization.isDefault());
        response.setDefaultCredentialName(organization.getDefaultCredentialName());
        response.setDefaultOtpName(organization.getDefaultOtpName());
        return response;
    }
}
