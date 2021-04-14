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

import io.getlime.security.powerauth.app.nextstep.repository.model.entity.RoleEntity;
import io.getlime.security.powerauth.lib.nextstep.model.entity.RoleDetail;

/**
 * Converter for roles.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class RoleConverter {

    /**
     * Convert role entity to detail.
     * @param role Role entity.
     * @return Role detail.
     */
    public RoleDetail fromEntity(RoleEntity role) {
        final RoleDetail roleDetail = new RoleDetail();
        roleDetail.setRoleName(role.getName());
        roleDetail.setDescription(role.getDescription());
        roleDetail.setTimestampCreated(role.getTimestampCreated());
        roleDetail.setTimestampLastUpdated(role.getTimestampLastUpdated());
        return roleDetail;
    }

}