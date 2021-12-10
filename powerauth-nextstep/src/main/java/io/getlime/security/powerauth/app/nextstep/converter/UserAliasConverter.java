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

import com.fasterxml.jackson.core.JsonProcessingException;
import io.getlime.security.powerauth.app.nextstep.repository.model.entity.UserAliasEntity;
import io.getlime.security.powerauth.lib.nextstep.model.entity.UserAliasDetail;

import java.util.Map;

/**
 * Converter for user aliases.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class UserAliasConverter {

    private final ExtrasConverter extrasConverter = new ExtrasConverter();

    /**
     * Convert user alias entity to detail.
     * @param alias User alias entity.
     * @return User alias detail.
     */
    public UserAliasDetail fromEntity(UserAliasEntity alias) throws JsonProcessingException {
        final UserAliasDetail aliasDetail = new UserAliasDetail();
        aliasDetail.setAliasName(alias.getName());
        aliasDetail.setAliasValue(alias.getValue());
        aliasDetail.setUserAliasStatus(alias.getStatus());
        aliasDetail.setTimestampCreated(alias.getTimestampCreated());
        aliasDetail.setTimestampLastUpdated(alias.getTimestampLastUpdated());
        if (alias.getExtras() != null) {
            final Map<String, Object> extras = extrasConverter.fromString(alias.getExtras());
            aliasDetail.getExtras().putAll(extras);
        }
        return aliasDetail;
    }

}